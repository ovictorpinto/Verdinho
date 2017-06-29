package com.github.ovictorpinto.verdinho.notificacao;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.ponto.ProcessoLoadLinhasPonto;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Date;

import br.com.tcsistemas.common.date.DataHelper;
import br.com.tcsistemas.common.string.StringHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ProximidadePontoReceiver extends BroadcastReceiver {
    
    private static String TAG = "ProximidadeReceiver";
    
    private GoogleApiClient mGoogleApiClient;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        
        int idPonto = intent.getIntExtra(PontoTO.PARAM_ID, -1);
        
        FenceState fenceState = FenceState.extract(intent);
        String action = fenceState.getFenceKey();
        Log.d(TAG, action + " " + fenceState.getCurrentState());
        if (fenceState.getCurrentState() != FenceState.TRUE) {
            return;
        }
        if (action.startsWith("FANCE_IN_")) {//dentro do raio. Atualiza a notificação
            Log.d(TAG, "Dentro...");
            PontoTO pontoTO = new PontoDAO(context).findByPK(String.valueOf(idPonto)).getPontoTO();
            AsyncTaskCompat.executeParallel(new LoadLinhas(context, pontoTO));
        } else if (action.startsWith("FANCE_OUT_")) {//saiu do raio, remove e notificação
            Log.d(TAG, "Saiu...");
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(idPonto);
        }
    }
    
    private class LoadLinhas extends ProcessoLoadLinhasPonto {
        
        public LoadLinhas(Context context, PontoTO pontoTO) {
            super(context, pontoTO);
            Log.d(TAG, "Iniciando pesquisa…");
        }
        
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_stat_name)
                                                                                         .setContentTitle(pontoTO.getDescricao())
                                                                                         .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                                                                         .setDefaults(Notification.DEFAULT_ALL)
                                                                                         .setContentText("Próximos em " + pontoTO
                                                                                                 .getDescricao());
            
            // Sets an ID for the notification
            int mNotificationId = pontoTO.getIdPonto();
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle("Próximos ônibus no ponto: ");
            // Moves events into the expanded layout
            
            for (Estimativa estimativa : retornoLinhasPonto.getEstimativas()) {
                //TODO task está montando o header da listagem.
                if (estimativa.getItinerarioId() == null) {
                    continue;
                }
                LinhaTO linha = mapLinhas.get(estimativa.getItinerarioId());
                
                String hora = estimativa.getHorarioText(context, retornoLinhasPonto.getHorarioDoServidor());
                String horario = DataHelper.format(new Date(estimativa.getHorarioNaOrigem()), "HH:mm");
                inboxStyle.addLine(StringHelper
                        .mergeSeparator(" - ", linha.getIdentificadorLinhaFiltrado(), linha.getBandeira(), hora, horario));
            }
            // Moves the expanded layout object into the notification object.
            mBuilder.setStyle(inboxStyle);
            Notification notification = mBuilder.build();
            try {
                
                mNotifyMgr.notify(mNotificationId, notification);
            } catch (SecurityException e) {
                // Some phones throw an exception for unapproved vibration
                notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                mNotifyMgr.notify(mNotificationId, notification);
            }
        }
    }
}
