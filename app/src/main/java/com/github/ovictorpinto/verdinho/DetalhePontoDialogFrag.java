package com.github.ovictorpinto.verdinho;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
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
    
    private View viewPrincipal;
    private ImageView buttonFavoritos;
    private PontoTO pontoTO;
    private MapView mapView;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        pontoTO = (PontoTO) getArguments().getSerializable(PontoTO.PARAM);
        assert pontoTO != null;
        
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//remove título
        getDialog().getWindow()
                   .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //remove fundo do dialog e obedece o shape
        getDialog().setCanceledOnTouchOutside(true);
        //        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        
        viewPrincipal = inflater.inflate(R.layout.ly_detalhe_ponto_dialog, null);
        TextView textViewPonto = (TextView) viewPrincipal.findViewById(R.id.textview_ponto);
        textViewPonto.setText(getString(R.string.ponto_n_, pontoTO.getIdentificador()));
        
        TextView textViewReferencia = (TextView) viewPrincipal.findViewById(R.id.textview_referencia);
        textViewReferencia.setText(pontoTO.getDescricao());
        
        View button = viewPrincipal.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), PontoDetalheActivity.class);
                i.putExtra(PontoTO.PARAM, pontoTO);
                startActivity(i);
                //depois de abrir a nova tela fecha o popup
                dismiss();
            }
        });
        
        buttonFavoritos = (ImageView) viewPrincipal.findViewById(R.id.button_favoritos);
        
        fillButton();
        buttonFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PontoFavoritoDAO dao = new PontoFavoritoDAO(getActivity());
                PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
                if (banco == null) {
                    dao.create(new PontoFavoritoPO(pontoTO));
                    Toast.makeText(getActivity(), R.string.ponto_adicionado, Toast.LENGTH_SHORT).show();
                } else {
                    dao.removeByPK(new PontoFavoritoPO(pontoTO));
                    Toast.makeText(getActivity(), R.string.ponto_removido, Toast.LENGTH_SHORT).show();
                }
                fillButton();
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
            }
        });
        
        //        MapFragment fragment = new MapFragment();
        //        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //        transaction.add(R.id.mapa_popup, fragment).commit();
        mapView = (MapView) viewPrincipal.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // Gets to GoogleMap from the MapView and does initialization stuff
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                if (ActivityCompat
                        .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                        .PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
                
                //        GoogleMapOptions.zOrderOnTop(true);
                // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
                MapsInitializer.initialize(getActivity().getApplicationContext());
                
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
    
    private void fillButton() {
        PontoFavoritoDAO dao = new PontoFavoritoDAO(getActivity());
        PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
        if (banco != null) {
            buttonFavoritos.setImageResource(R.drawable.favorito_tabbar_marcado);
        } else {
            buttonFavoritos.setImageResource(R.drawable.favorito_tabbar_desmarcado);
        }
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
}