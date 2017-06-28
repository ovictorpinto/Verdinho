package com.github.ovictorpinto.verdinho.notificacao;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.google.android.gms.awareness.fence.FenceState;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ProximidadePontoReceiver extends BroadcastReceiver {
    
    private static String TAG = "ProximidadeReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        FenceState fenceState = FenceState.extract(intent);
        int idPonto = intent.getIntExtra(PontoTO.PARAM_ID, -1);
        PontoTO pontoTO = new PontoDAO(context).findByPK(String.valueOf(idPonto)).getPontoTO();
        String texto = null;
        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                texto = "Entrando em " + pontoTO.getDescricao();
                break;
            case FenceState.FALSE:
                texto = "Saindo de " + pontoTO.getDescricao();
                break;
            default:
        }
        showNotification(texto, context, pontoTO);
    }
    
    private void showNotification(String eventtext, Context ctx, PontoTO pontoTO) {
        
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_stat_name)
                                                                                 .setContentTitle(pontoTO.getDescricao())
                                                                                 .setContentText(eventtext);
        
        // Sets an ID for the notification
        int mNotificationId = pontoTO.getIdPonto();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
