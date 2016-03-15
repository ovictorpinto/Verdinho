package com.github.ovictorpinto.verdinho;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoDetalharPontos;
import com.github.ovictorpinto.verdinho.retorno.RetornoPesquisarPontos;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;
import br.com.tcsistemas.common.net.HttpHelper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener {
    
    private static final String TAG = "MainActivity";
    private static final String OPCAO = "opcaoSelecionada";
    private static final int PERMISSION_GPS_REQUEST_CODE = 201;

    public static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private ProcessoLoadPontos processo;

    private ImageView buttonFavorito;
    private ImageView buttonSobre;

    //0 favorito, 1 mapa, 2 sobre
    private int opcao = 1;

    private GoogleMap mMap;
    private Map<String, PontoTO> mapPontos = new HashMap<>();

    private MapFragment mapFragment;
    private Fragment sobreFragment;
    private Fragment favoritoFragment;

    private LatLng devicePosition;
    private Set<Integer> setFavoritos;
    private BroadcastReceiver favoriteReceive;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.ly_main);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean loaded = preferences.getBoolean(Constantes.pref_loaded, false);

        if (!loaded) {
            processo = new ProcessoLoadPontos();
            processo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        favoritoFragment = getFragmentManager().findFragmentById(R.id.fragment_favoritos);
        sobreFragment = getFragmentManager().findFragmentById(R.id.fragment_sobre);

        View buttonMapa = findViewById(R.id.image_mapa);
        buttonMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMapa();
            }
        });
        buttonFavorito = (ImageView) findViewById(R.id.button_favoritos);
        buttonFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFavorito();
            }
        });

        buttonSobre = (ImageView) findViewById(R.id.button_sobre);
        buttonSobre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSobre();
            }
        });

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable != ConnectionResult.SUCCESS) {//caso nao esteja disponivel aparece o botao para baixar
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 1);
            errorDialog.show();
            return;
        }

        fillFavoritos();
        favoriteReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                fillFavoritos();
                fillMarkers();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(favoriteReceive, new IntentFilter(Constantes.actionUpdatePontoFavorito));

        mapFragment.getMapAsync(this);

        clickMapa();

        if (savedInstanceState != null) {
            switch (savedInstanceState.getInt(OPCAO)) {
                case 0:
                    clickFavorito();
                    break;
                case 2:
                    clickSobre();
                    break;
                default:
                    clickMapa();
            }
        }
    }

    private void fillFavoritos() {
        PontoFavoritoDAO favoritoDAO = new PontoFavoritoDAO(this);
        List<PontoPO> allFavoritos = favoritoDAO.findAllFavoritos();
        setFavoritos = new HashSet<>();
        for (int i = 0; i < allFavoritos.size(); i++) {
            PontoPO po = allFavoritos.get(i);
            setFavoritos.add(po.getPontoTO().getIdPonto());
        }
    }

    private void clickMapa() {
        setTitle(R.string.app_name);
        opcao = 1;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(sobreFragment).hide(favoritoFragment).show(mapFragment);
        transaction.commit();
        buttonSobre.setImageResource(R.drawable.info_tabbar_desmarcado);
        buttonFavorito.setImageResource(R.drawable.favorito_tabbar_desmarcado);
    }

    private void clickSobre() {
        setTitle(R.string.informacoes);
        opcao = 2;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.show(sobreFragment).hide(favoritoFragment).hide(mapFragment);
        transaction.commit();
        buttonSobre.setImageResource(R.drawable.info_tabbar_marcado);
        buttonFavorito.setImageResource(R.drawable.favorito_tabbar_desmarcado);
    }

    private void clickFavorito() {
        setTitle(R.string.favoritos);
        opcao = 0;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.hide(sobreFragment).show(favoritoFragment).hide(mapFragment);
        transaction.commit();
        buttonSobre.setImageResource(R.drawable.info_tabbar_desmarcado);
        buttonFavorito.setImageResource(R.drawable.favorito_tabbar_marcado);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(OPCAO, opcao);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processo != null) {
            processo.cancel(true);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(favoriteReceive);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LogHelper.log(TAG, "Mapa pronto");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                fillMarkers();
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                PontoTO clicado = mapPontos.get(marker.getId());

                Bundle bundle = new Bundle();
                bundle.putSerializable(PontoTO.PARAM, clicado);

                DialogFragment frag = new DetalhePontoDialogFrag();
                frag.setArguments(bundle);
                frag.show(getFragmentManager(), DetalhePontoDialogFrag.TAG_FRAG);

                return true;
            }
        });
        fillMarkers();
    }

    private void fillMarkers() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        mMap.clear();
        mapPontos = new HashMap<>();
        PontoDAO dao = new PontoDAO(this);
        List<PontoPO> list = dao.findByRegiao(bounds);
        LogHelper.log(TAG, "Encontrados " + list.size());

        for (int i = 0; i < list.size(); i++) {
            PontoPO pontoPO = list.get(i);
            PontoTO to = pontoPO.getPontoTO();

            int pin = setFavoritos.contains(to.getIdPonto()) ? R.drawable.pin_favorito : R.drawable.pin;

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(pin));
            markerOptions.position(new LatLng(to.getLatitude(), to.getLongitude()));

            Marker marker = mMap.addMarker(markerOptions);
            mapPontos.put(marker.getId(), to);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                                                            .addApi(LocationServices.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LogHelper.log(TAG, "Conectou no fused");

        boolean hasPermission = (ContextCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            getLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, getString(R.string.explicacao_gps_sem_permissao), Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat
                        .requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GPS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
                break;
        }
    }

    private void getLocation() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            LogHelper.log(TAG, "Encontrou última posição!");
            devicePosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLng(devicePosition);
            mMap.animateCamera(update, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    fillMarkers();
                }

                @Override
                public void onCancel() {

                }
            });
        } else {
            Toast.makeText(this, R.string.location_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogHelper.log(TAG, "onConnectionSuspended " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        LogHelper.log(TAG, "onConnectionFailed");
        Toast.makeText(this, "Localização não disponível", Toast.LENGTH_SHORT).show();
    }

    private class ProcessoLoadPontos extends AsyncTask<Void, String, Boolean> {

        private final String TAG = "ProcessoLoadPontos";
        protected Context context;
        private FragmentManager fragmentManager;

        public ProcessoLoadPontos() {
            this.context = MainActivity.this;
            this.fragmentManager = getFragmentManager();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                if (FragmentExtended.isOnline(context)) {
                    try {

                        String url = Constantes.listarPontos;
                        String urlParam = "{\"envelope\":[-40.2558446019482, -20.3411474261535, -40.3615017219324, -20.1865857661999]}";
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);

                        String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);

                        RetornoPesquisarPontos retornoPesquisarPontos = mapper.readValue(retorno, RetornoPesquisarPontos.class);
                        LogHelper.log(TAG, retornoPesquisarPontos.getPontosDeParada().size() + " item(s)");

                        url = Constantes.detalharPontos;
                        urlParam = "{\"listaIds\": " + retornoPesquisarPontos.getPontosDeParada().toString() + " }";
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);

                        retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);

                        RetornoDetalharPontos retornoDetalharPontos = mapper.readValue(retorno, RetornoDetalharPontos.class);
                        LogHelper.log(TAG, retornoDetalharPontos.getPontosDeParada().size() + " item(s)");

                        List<PontoTO> pontosDeParada = retornoDetalharPontos.getPontosDeParada();
                        PontoDAO dao = new PontoDAO(context);
                        dao.removeAll();
                        for (int i = 0; i < pontosDeParada.size(); i++) {
                            PontoTO pontoTO = pontosDeParada.get(i);
                            dao.create(new PontoPO(pontoTO));
                        }
                        return true;

                    } catch (UnknownHostException e) {
                        LogHelper.log(e);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (!isCancelled()) {
                if (!success) {
                    //abrir uma nova janela de erro
                    AlertDialogFragmentV11 alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor);
                    fragmentManager.beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss();
                } else {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(Constantes.pref_loaded, true);
                    editor.apply();
                    fillMarkers();
                }
            }
            processo = null;
        }

    }

}