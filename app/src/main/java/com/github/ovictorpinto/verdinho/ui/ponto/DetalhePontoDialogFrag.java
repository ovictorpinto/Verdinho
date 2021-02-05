package com.github.ovictorpinto.verdinho.ui.ponto;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.github.ovictorpinto.verdinho.util.AwarenessHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetalhePontoDialogFrag extends DialogFragment {
    
    public static final String TAG_FRAG = "fragmentConfirmacaoConsultaFrag";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    
    private View viewPrincipal;
    private ImageView buttonFavoritos;
    private PontoTO pontoTO;
    private MapView mapView;
    private AnalyticsHelper analyticsHelper;
    private String ORIGEM = "ponto_dialog";
    private PontoDialogoListener pontoDialogoListener;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        analyticsHelper = new AnalyticsHelper(getActivity());
        
        pontoTO = (PontoTO) getArguments().getSerializable(PontoTO.PARAM);
        assert pontoTO != null;
        
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//remove título
        getDialog().getWindow()
                   .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //remove fundo do dialog e obedece o shape
        getDialog().setCanceledOnTouchOutside(true);
        
        viewPrincipal = inflater.inflate(R.layout.ly_detalhe_ponto_dialog, null);
        TextView textViewPonto = viewPrincipal.findViewById(R.id.textview_ponto);
        
        textViewPonto.setText(pontoTO.getNomeApresentacao(getActivity()));
        
        TextView textViewReferencia = viewPrincipal.findViewById(R.id.textview_referencia);
        textViewReferencia.setText(pontoTO.getDescricao());
        
        View button = viewPrincipal.findViewById(R.id.button);
        button.setOnClickListener(v -> {
            analyticsHelper.selecionouPonto(pontoTO, ORIGEM);
            Intent i = new Intent(getActivity(), PontoDetalheActivity.class);
            i.putExtra(PontoTO.PARAM, pontoTO);
            startActivity(i);
            //depois de abrir a nova tela fecha o popup
            dismiss();
        });
        
        View buttonDestino = viewPrincipal.findViewById(R.id.button_destino);
        buttonDestino.setOnClickListener(view -> {
            Fragment targetFragment = getTargetFragment();
            if (targetFragment instanceof PontoDialogoListener) {
                pontoDialogoListener = (PontoDialogoListener) targetFragment;
                pontoDialogoListener.onDestino(pontoTO);
            }
            dismiss();
        });
        
        buttonFavoritos = viewPrincipal.findViewById(R.id.button_favoritos);
        
        fillFavButton();
        buttonFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PontoFavoritoDAO dao = new PontoFavoritoDAO(getActivity());
                PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
                if (banco == null) {
                    analyticsHelper.favoritou(pontoTO, ORIGEM);
                    dao.create(new PontoFavoritoPO(pontoTO));
                    Toast.makeText(getActivity(), R.string.ponto_adicionado, Toast.LENGTH_SHORT).show();
                } else {
                    analyticsHelper.removeuFavoritou(pontoTO, ORIGEM);
                    dao.removeByPK(new PontoFavoritoPO(pontoTO));
                    Toast.makeText(getActivity(), R.string.ponto_removido, Toast.LENGTH_SHORT).show();
                    new AwarenessHelper(getActivity()).removeFenda(pontoTO);
                }
                fillFavButton();
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
            }
        });
        
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity().getApplicationContext());
        
        mapView = viewPrincipal.findViewById(R.id.mapview);
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        //https://github.com/googlemaps/android-samples/blob/master/ApiDemos/app/src/main/java/com/example/mapdemo/RawMapViewDemoActivity
        // .java
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Gets to GoogleMap from the MapView and does initialization stuff
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setTrafficEnabled(true);
                if (ActivityCompat
                        .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                        .PERMISSION_GRANTED || ActivityCompat
                        .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager
                        .PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
                
                // Updates the location and zoom of the MapView
                CameraPosition cameraPosition = CameraPosition.builder().tilt(45).zoom(17)
                                                              .target(new LatLng(pontoTO.getLatitude(), pontoTO.getLongitude())).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                googleMap.moveCamera(cameraUpdate);
                
                //        int pin = setFavoritos.contains(to.getIdPonto()) ? R.drawable.pin_favorito : R.drawable.pin;
                
                MarkerOptions markerOptions = new MarkerOptions();
                //        markerOptions.icon(BitmapDescriptorFactory.fromResource(pin));
                markerOptions.position(new LatLng(pontoTO.getLatitude(), pontoTO.getLongitude()));
                googleMap.addMarker(markerOptions);
            }
        });
        
        return viewPrincipal;
    }
    
    private void fillFavButton() {
        PontoFavoritoDAO dao = new PontoFavoritoDAO(getActivity());
        PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
        if (banco != null) {
            buttonFavoritos.setImageResource(R.drawable.favorito_tabbar_marcado);
        } else {
            buttonFavoritos.setImageResource(R.drawable.favorito_tabbar_desmarcado);
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        
        mapView.onSaveInstanceState(mapViewBundle);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    
    public interface PontoDialogoListener {
        void onDestino(PontoTO pontoTO);
    }
}