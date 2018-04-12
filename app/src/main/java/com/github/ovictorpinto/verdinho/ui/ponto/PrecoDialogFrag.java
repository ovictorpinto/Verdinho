package com.github.ovictorpinto.verdinho.ui.ponto;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ovictorpinto.ConstantesEmpresa;
import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.NumberFormat;
import java.util.Locale;

import br.com.tcsistemas.common.string.StringHelper;

public class PrecoDialogFrag extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.legenda);
        return dialog;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        //        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//remove título
        getDialog().getWindow()
                   .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //remove fundo do dialog e obedece o shape
        getDialog().setCanceledOnTouchOutside(true);
        
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        config.setConfigSettings(firebaseRemoteConfigSettings);
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that
        // each fetch goes to the server. This should not be used in release
        // builds.
        if (config.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 60;
        }
        final View viewPrincipal = inflater.inflate(R.layout.ly_preco_dialog, null);
        config.fetch(cacheExpiration).addOnSuccessListener(aVoid -> {
            // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
            config.activateFetched();
            Locale brasil = new Locale("pt", "BR");
            NumberFormat currency = NumberFormat.getCurrencyInstance(brasil);
            double precoPassagem = config.getDouble(ConstantesEmpresa.remoteConfigPassagem);
            String dataReajuste = config.getString(ConstantesEmpresa.remoteConfigDataReajustePassagem);
            ((TextView) viewPrincipal.findViewById(R.id.textview_passagem)).setText(currency.format(precoPassagem));
            ((TextView) viewPrincipal.findViewById(R.id.textview_troco_5)).setText(currency.format(5 - precoPassagem));
            ((TextView) viewPrincipal.findViewById(R.id.textview_troco_10)).setText(currency.format(10 - precoPassagem));
            ((TextView) viewPrincipal.findViewById(R.id.textview_troco_20)).setText(currency.format(20 - precoPassagem));
            ((TextView) viewPrincipal.findViewById(R.id.textview_troco_50)).setText(currency.format(50 - precoPassagem));
            TextView textviewData = viewPrincipal.findViewById(R.id.textview_data_reajuste);
            textviewData.setText(getString(R.string.ultimo_reajuste__, dataReajuste));
            if (StringHelper.isNotBlank(dataReajuste)) {
                textviewData.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            // There has been an error fetching the config
            Log.w("preco", "Error fetching config: " + e.getMessage());
        });
        
        View button = viewPrincipal.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        
        return viewPrincipal;
    }
}