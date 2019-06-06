package com.github.ovictorpinto.verdinho.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.NumberFormat;
import java.util.Locale;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;
import br.com.mobilesaude.androidlib.widget.DialogCarregandoV11;
import br.com.tcsistemas.common.string.StringHelper;

public class MainActivity extends AppCompatActivity {

    public static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private ProcessoLoadPontos processo;

    private AnalyticsHelper analyticsHelper;
    private MapFragment mapFragment;
    private TabLayout tabLayout;
    private SharedPreferences sharedPreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        analyticsHelper = new AnalyticsHelper(this);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean loaded = preferences.getBoolean(Constantes.pref_loaded, false);

        if (!loaded) {
            processo = new ProcessoLoadPontos();
            processo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        tabLayout = findViewById(R.id.bottom_navigation);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_pin_drop_black_24dp), 0);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_star_black_24dp), 1);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_twitter), 2);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_settings_applications_black_24dp), 3);

        int ultima = sharedPreferences.getInt(Constantes.pref_last_aba, 0);
        escolheAba(ultima);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                sharedPreferences.edit().putInt(Constantes.pref_last_aba, tab.getPosition()).apply();
                escolheAba(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        exibeMensagemInicial();
    }

    private void exibeMensagemInicial() {
        String remoteConfigMensagem = "%s_mensagem_inicial";
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
        config.fetch(cacheExpiration).addOnSuccessListener(aVoid -> {
            config.activateFetched();
            String mensagem = config.getString(String.format(remoteConfigMensagem, BuildConfig.FLAVOR));
            if (StringHelper.isNotBlank(mensagem)) {
                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name).setMessage(mensagem).setPositiveButton(R.string.mobile_lib_ok_, null).show();
            }

        }).addOnFailureListener(e -> {
            // There has been an error fetching the config
            Log.w("preco", "Error fetching config: " + e.getMessage());
        });
    }

    private void escolheAba(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        tab.select();
        tab.getIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        switch (tab.getPosition()) {
            case 0:
                clickMapa();
                break;
            case 1:
                clickFavorito();
                break;
            case 2:
                clickTwitter();
                break;
            case 3:
                clickSobre();
                break;
        }
    }

    private void clickMapa() {
        analyticsHelper.clickMapa();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mapFragment = (MapFragment) getFragmentManager().findFragmentByTag("mapa");
        if (mapFragment == null) {
            mapFragment = new MapFragment();
        }
        transaction.replace(R.id.frame, mapFragment, "mapa");
        transaction.commit();
    }

    private void clickSobre() {
        analyticsHelper.clickSobre();
        setTitle(R.string.informacoes);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, new ConfiguracaoFragment()).commit();
    }

    private void clickTwitter() {
        analyticsHelper.clickTwitter();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, new TimelineFragment()).commit();
    }

    private void clickFavorito() {
        analyticsHelper.clickFavorito();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, new PontoFavoritoFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processo != null) {
            processo.cancel(true);
        }
    }

    private class ProcessoLoadPontos extends com.github.ovictorpinto.verdinho.ui.main.ProcessoLoadPontos {

        private FragmentManager fragmentManager;

        private DialogCarregandoV11 carregando = new DialogCarregandoV11();

        public ProcessoLoadPontos() {
            super(MainActivity.this);
            this.fragmentManager = getFragmentManager();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fragmentManager.beginTransaction().add(carregando, DialogCarregandoV11.FRAGMENT_ID).commitAllowingStateLoss();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (!isCancelled()) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        carregando.setMessage(values[0]);

                    }
                });
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (!isCancelled()) {
                DialogCarregandoV11 findFragmentByTag = (DialogCarregandoV11) fragmentManager
                        .findFragmentByTag(DialogCarregandoV11.FRAGMENT_ID);
                if (findFragmentByTag != null) {
                    findFragmentByTag.dismiss();
                }

                if (!success) {
                    //abrir uma nova janela de erro
                    AlertDialogFragmentV11 alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor);
                    fragmentManager.beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss();
                } else {
                    final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(Constantes.pref_loaded, true);
                    editor.apply();
                }
            }
            processo = null;
        }
    }

}