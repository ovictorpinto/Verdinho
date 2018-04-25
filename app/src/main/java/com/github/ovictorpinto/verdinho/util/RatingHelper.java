package com.github.ovictorpinto.verdinho.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.github.ovictorpinto.verdinho.R;

import java.util.Calendar;

/**
 * Created by victorpinto on 22/06/17. 
 */

public class RatingHelper {
    
    private final String PREF_QUANT_ABERTURA = "prefQuantidadeAbertura";
    private final String PREF_ULT_ABERTURA = "prefUltimaAbertura";
    private final String PREF_QTD_DIA = "prefQuantidadeDias";
    /**
     * Indica se o usuário ainda não decidiu (ou pouco uso ou selecionou mais tarde)
     */
    private final String PREF_PODE_AVALIAR = "prefPodeAvaliar";
    
    private final int MIN_ABERTURA = 10;
    private final int MIN_DIAS = 3;
    
    public static final int DELAY_ABERTURA_MILI = 10 * 1000;
    
    private SharedPreferences sharedPref;
    private Context context;
    
    public RatingHelper(Context context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public void count() {
        boolean podeAvaliar = sharedPref.getBoolean(PREF_PODE_AVALIAR, true);
        if (!podeAvaliar) {
            return;
        }
        //sempre atualiza a quantidade de abertura
        int qtAbertura = sharedPref.getInt(PREF_QUANT_ABERTURA, 0);
        qtAbertura++;
        
        //se abriu num dia diferente, marca como mais um dia de uso
        int ultimoDia = sharedPref.getInt(PREF_ULT_ABERTURA, 0);
        int qtDia = sharedPref.getInt(PREF_QTD_DIA, 0);
        int hoje = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        if (ultimoDia != hoje) {
            qtDia++;
        }
        
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_QTD_DIA, qtDia);
        editor.putInt(PREF_ULT_ABERTURA, hoje);
        editor.putInt(PREF_QUANT_ABERTURA, qtAbertura).apply();
    }
    
    public void show() {
        //já decidiu que não vai avaliar
        boolean podeAvaliar = sharedPref.getBoolean(PREF_PODE_AVALIAR, true);
        if (!podeAvaliar) {
            return;
        }
        
        int qtDia = sharedPref.getInt(PREF_QTD_DIA, 0);
        int qtAbertura = sharedPref.getInt(PREF_QUANT_ABERTURA, 0);
        
        //ainda não atingiu o uso mínimo
        if (qtAbertura < MIN_ABERTURA || qtDia < MIN_DIAS) {
            return;
        }
        
        AlertDialog.Builder alert = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog);
        alert.setCancelable(false);
        alert.setMessage(R.string.rating_message);
        alert.setPositiveButton(R.string.rating_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AnalyticsHelper(context).clickRatingSim();
                abreLoja();
            }
        });
        alert.setNeutralButton(R.string.rating_neutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AnalyticsHelper(context).clickRatingMaisTarde();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(PREF_QTD_DIA, 0);
                editor.putInt(PREF_ULT_ABERTURA, 0);
                editor.putInt(PREF_QUANT_ABERTURA, 0).apply();
            }
        });
        alert.setNegativeButton(R.string.rating_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AnalyticsHelper(context).clickRatingNao();
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(PREF_PODE_AVALIAR, false).apply();
            }
        });
        alert.create().show();
    }
    
    public void abreLoja() {
        //https://stackoverflow.com/questions/10816757/rate-this-app-link-in-google-play-store-app-on-the-phone
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Não foi possível abrir a loja", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(PREF_PODE_AVALIAR, false).apply();
    }
}
