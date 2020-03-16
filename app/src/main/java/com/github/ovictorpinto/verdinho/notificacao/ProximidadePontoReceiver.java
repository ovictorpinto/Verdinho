package com.github.ovictorpinto.verdinho.notificacao;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.ponto.ProcessoLoadLinhasPonto;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Random;

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
            PontoPO po = new PontoDAO(context).findByPK(String.valueOf(idPonto));
            if (po != null) {
                PontoTO pontoTO = po.getPontoTO();
                new LoadLinhas(context, pontoTO).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Crashlytics.log("Tentando mostrar um ponto inexistente no banco: " + idPonto);
            }
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
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!isCancelled() && success) {
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(R.drawable.ic_stat_name);
                mBuilder.setContentTitle(pontoTO.getDescricao());
                mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);
                }
                // Sets an ID for the notification
                int mNotificationId = pontoTO.getIdPonto();
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                inboxStyle.setBigContentTitle(context.getString(R.string.proximo_onibus__, pontoTO.getNomeApresentacao(context)));
                
                for (Estimativa estimativa : retornoLinhasPonto.getEstimativas()) {
                    //TODO task está montando o header da listagem.
                    if (estimativa.getItinerarioId() == null) {
                        continue;
                    }
                    LinhaTO linha = mapLinhas.get(estimativa.getItinerarioId());
                    
                    String hora = estimativa.getHorarioOrigemText(context, retornoLinhasPonto.getHorarioDoServidor());
                    String descricao = StringHelper.mergeSeparator(" - ", linha.getIdentificadorLinhaFiltrado(), hora, linha.getBandeira());
                    inboxStyle.addLine(descricao);
                }
                // Moves the expanded layout object into the notification object.
                mBuilder.setStyle(inboxStyle);
                
                Random random = new Random();
                int contentIntentRequestCode = random.nextInt();
                int deleteIntentRequestCode = random.nextInt();
                
                Intent contentIntent = new Intent(InteracaoProximidadeReceiver.ACTION_PUSH_OPEN);
                contentIntent.putExtra(PontoTO.PARAM, pontoTO);
                
                Intent deleteIntent = new Intent(InteracaoProximidadeReceiver.ACTION_PUSH_DELETE);
                deleteIntent.putExtra(PontoTO.PARAM, pontoTO);
                
                PendingIntent pContentIntent = PendingIntent
                        .getBroadcast(context, contentIntentRequestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent pDeleteIntent = PendingIntent
                        .getBroadcast(context, deleteIntentRequestCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                
                mBuilder.setContentIntent(pContentIntent);
                mBuilder.setDeleteIntent(pDeleteIntent);
                mBuilder.setAutoCancel(true);
                
                Notification notification = mBuilder.build();
                try {
                    
                    mNotifyMgr.notify(mNotificationId, notification);
                } catch (SecurityException e) {
                    // Some phones throw an exception for unapproved vibration
                    notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
                    mNotifyMgr.notify(mNotificationId, notification);
                }
                new AnalyticsHelper(context).exibiuNotificacaoProximidade(pontoTO);
            }
        }
    }
}
