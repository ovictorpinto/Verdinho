package com.github.ovictorpinto.verdinho.notificacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.google.android.gms.awareness.fence.FenceState;

public class ProximidadePontoReceiver extends BroadcastReceiver {
    
    private static String TAG = "ProximidadeReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        FenceState fenceState = FenceState.extract(intent);
        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                Log.i(TAG, "Entrando");
                int idPonto = intent.getIntExtra(PontoTO.PARAM_ID, -1);
                if (idPonto > -1) {
                    Toast.makeText(context, String.valueOf(idPonto), Toast.LENGTH_SHORT).show();
                }
                break;
            case FenceState.FALSE:
                Log.i(TAG, "Saindo");
            default:
        }
    }
}
