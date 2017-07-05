package com.github.ovictorpinto.verdinho.notificacao;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.ponto.PontoDetalheActivity;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;

/**
 * Created by User on 11/01/2017.
 */

public class InteracaoProximidadeReceiver extends WakefulBroadcastReceiver {
    
    private static final String TAG = "PushBroadcastReceiver";
    
    public static final String ACTION_PUSH_DELETE = BuildConfig.APPLICATION_ID + ".ACTION_PUSH_DELETE";
    public static final String ACTION_PUSH_OPEN = BuildConfig.APPLICATION_ID + ".ACTION_PUSH_OPEN";
    
    private AnalyticsHelper analyticsHelper;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        analyticsHelper = new AnalyticsHelper(context);
        
        if (intent.getAction().equals(ACTION_PUSH_OPEN)) {
            Log.d(TAG, "Action open");
            onPushOpen(context, intent);
        } else if (intent.getAction().equals(ACTION_PUSH_DELETE)) {
            Log.d(TAG, "Action delete");
            onPushDelete(context, intent);
        } else {
            Log.d(TAG, "Action desconhecida...");
        }
    }
    
    private void onPushOpen(Context context, Intent intent) {
        analyticsHelper.clicouNotificacaoProximidade();
        if (intent.hasExtra(PontoTO.PARAM)) {
            PontoTO pontoTO = (PontoTO) intent.getSerializableExtra(PontoTO.PARAM);
            Intent i = new Intent(context, PontoDetalheActivity.class);
            i.putExtra(PontoTO.PARAM, pontoTO);
            i.putExtras(intent.getExtras());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            analyticsHelper.selecionouPonto(pontoTO, "notificacao_proximidade");
        }
    }
    
    private void onPushDelete(Context context, Intent intent) {
        analyticsHelper.cancelaNotificacaoProximidade();
    }
}
