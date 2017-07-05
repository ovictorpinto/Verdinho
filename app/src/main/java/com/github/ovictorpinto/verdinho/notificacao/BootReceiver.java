package com.github.ovictorpinto.verdinho.notificacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.AwarenessHelper;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

/**
 * Registra as fendas a partir dos pontos favoritos do usuário
 */
public class BootReceiver extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener {
    
    private static final String TAG = "BootReceiver";
    private GoogleApiClient mGoogleApiClient;
    private Context context;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive Boot");
        this.context = context;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
            builder.addConnectionCallbacks(this);
            builder.addOnConnectionFailedListener(this);
            builder.addApi(Awareness.API);
            mGoogleApiClient = builder.build();
            mGoogleApiClient.connect();
        }
    }
    
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "Conectou");
        AwarenessHelper awarenessHelper = new AwarenessHelper(context);
        PontoFavoritoDAO dao = new PontoFavoritoDAO(context);
        List<PontoPO> allFavoritos = dao.findAllFavoritos();
        Log.w(TAG, "Pontos " + allFavoritos.size());
        for (PontoPO pontoPO : allFavoritos) {
            PontoTO pontoTO = pontoPO.getPontoTO();
            if (pontoTO.getNotificacao()) {
                awarenessHelper.criaFenda(pontoTO, mGoogleApiClient);
            }
        }
    }
    
    @Override
    public void onConnectionSuspended(int i) {
    }
    
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }
}
