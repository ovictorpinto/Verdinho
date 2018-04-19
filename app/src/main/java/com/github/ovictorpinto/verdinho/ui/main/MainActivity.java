package com.github.ovictorpinto.verdinho.ui.main;

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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ovictorpinto.ConstantesEmpresa;
import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoDetalharPontos;
import com.github.ovictorpinto.verdinho.retorno.RetornoPesquisarPontos;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;
import br.com.tcsistemas.common.net.HttpHelper;

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
    
    private class ProcessoLoadPontos extends AsyncTask<Void, String, Boolean> {
        
        private final String TAG = "ProcessoLoadPontos";
        protected Context context;
        private FragmentManager fragmentManager;
        
        public ProcessoLoadPontos() {
            this.context = MainActivity.this;
            this.fragmentManager = getFragmentManager();
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            
            try {
                if (FragmentExtended.isOnline(context)) {
                    try {
                        
                        String url = ConstantesEmpresa.listarPontos;
                        String urlParam = "{\"envelope\":" + ConstantesEmpresa.ENVELOPE + "}";
                        Map<String, String> headers = new ConstantesEmpresa(context).getHeaders();
                        
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);
                        
                        String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);
                        
                        RetornoPesquisarPontos retornoPesquisarPontos = mapper.readValue(retorno, RetornoPesquisarPontos.class);
                        LogHelper.log(TAG, retornoPesquisarPontos.getPontosDeParada().size() + " item(s)");
                        
                        url = ConstantesEmpresa.detalharPontos;
                        urlParam = "{\"listaIds\": " + retornoPesquisarPontos.getPontosDeParada().toString() + " }";
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);
                        
                        retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);
                        
                        RetornoDetalharPontos retornoDetalharPontos = mapper.readValue(retorno, RetornoDetalharPontos.class);
                        LogHelper.log(TAG, retornoDetalharPontos.getPontosDeParada().size() + " item(s)");
                        
                        List<PontoTO> pontosDeParada = retornoDetalharPontos.getPontosDeParada();
                        PontoDAO dao = new PontoDAO(context);
                        dao.removeAll();
                        for (int i = 0; i < pontosDeParada.size(); i++) {
                            PontoTO pontoTO = pontosDeParada.get(i);
                            dao.create(new PontoPO(pontoTO));
                        }
                        return true;
                        
                    } catch (UnknownHostException e) {
                        LogHelper.log(e);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(e);
            }
            return false;
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            
            if (!isCancelled()) {
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