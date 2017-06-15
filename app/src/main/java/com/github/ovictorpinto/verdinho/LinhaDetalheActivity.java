package com.github.ovictorpinto.verdinho;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.ovictorpinto.verdinho.persistencia.dao.LinhaFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.LinhaFavoritoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoLinhasPonto;
import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;
import br.com.tcsistemas.common.net.HttpHelper;

public class LinhaDetalheActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        OnStreetViewPanoramaReadyCallback {
    
    private Estimativa estimativa;
    private PontoTO pontoTO;
    private LinhaTO linhaTO;
    private ProcessoLoadDetalheLinha processo;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private AppBarLayout appBarLayout;
    private View progress;
    private View emptyView;
    private FloatingActionButton buttonFavorito;
    private BroadcastReceiver favoritoReceive;
    private SubtitleCollapsingToolbarLayout collapsingToolbarLayout;
    
    private Timer timerAtual = new Timer();
    private TimerTask task;
    private final Handler handler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_linha_detalhe);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        collapsingToolbarLayout = (SubtitleCollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        estimativa = (Estimativa) getIntent().getSerializableExtra(Estimativa.PARAM);
        pontoTO = (PontoTO) getIntent().getSerializableExtra(PontoTO.PARAM);
        linhaTO = (LinhaTO) getIntent().getSerializableExtra(LinhaTO.PARAM);
        
        final String subtitle = getString(R.string.ponto_n_linha_n_, pontoTO.getIdentificador(), linhaTO.getIdentificadorLinhaFiltrado());
        final String title = linhaTO.getBandeira();
        collapsingToolbarLayout.setTitle(title);
        collapsingToolbarLayout.setSubtitle(subtitle);
        
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        
        progress = findViewById(R.id.layout_progress);
        progress.setVisibility(View.VISIBLE);
        
        emptyView = findViewById(android.R.id.empty);
        //        ((ImageView) emptyView.findViewById(R.id.image)).setImageResource(R.drawable.error);
        //        ((TextView) emptyView.findViewById(R.id.textview_title)).setText(R.string.ponto_empty_title);
        //        ((TextView) emptyView.findViewById(R.id.textview_subtitle)).setText(R.string.ponto_empty_subtitle);
        
        buttonFavorito = (FloatingActionButton) findViewById(R.id.fab);
        buttonFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LinhaFavoritoDAO dao = new LinhaFavoritoDAO(LinhaDetalheActivity.this);
                LinhaFavoritoPO banco = dao.findByPK(linhaTO.getIdentificadorLinha());
                if (banco == null) {
                    dao.create(new LinhaFavoritoPO(linhaTO));
                    View.OnClickListener desfazerListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dao.removeByPK(new LinhaFavoritoPO(linhaTO));
                            LocalBroadcastManager.getInstance(LinhaDetalheActivity.this)
                                                 .sendBroadcast(new Intent(Constantes.actionUpdateLinhaFavorito));
                        }
                    };
                    Snackbar.make(view, R.string.linha_adicionado, Snackbar.LENGTH_SHORT).setAction(R.string.desfazer, desfazerListener)
                            .show();
                } else {
                    dao.removeByPK(new LinhaFavoritoPO(linhaTO));
                    View.OnClickListener desfazerListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dao.create(new LinhaFavoritoPO(linhaTO));
                            LocalBroadcastManager.getInstance(LinhaDetalheActivity.this)
                                                 .sendBroadcast(new Intent(Constantes.actionUpdateLinhaFavorito));
                        }
                    };
                    Snackbar.make(view, R.string.linha_removido, Snackbar.LENGTH_SHORT).setAction(R.string.desfazer, desfazerListener)
                            .show();
                }
                LocalBroadcastManager.getInstance(LinhaDetalheActivity.this)
                                     .sendBroadcast(new Intent(Constantes.actionUpdateLinhaFavorito));
            }
        });
        refresh();
        setButtonFavorito();
        favoritoReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setButtonFavorito();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(favoritoReceive, new IntentFilter(Constantes.actionUpdateLinhaFavorito));
        
        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        swipeRefresh.setRefreshing(true);
                        refresh();
                    }
                });
            }
        };
        
        timerAtual.schedule(task, PontoDetalheActivity.TIME_REFRESH_MILI, PontoDetalheActivity.TIME_REFRESH_MILI);
        
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
    }
    
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        panorama.setPosition(new LatLng(pontoTO.getLatitude(), pontoTO.getLongitude()));
    }
    
    private void setButtonFavorito() {
        LinhaFavoritoDAO dao = new LinhaFavoritoDAO(this);
        LinhaFavoritoPO banco = dao.findByPK(linhaTO.getIdentificadorLinha());
        if (banco != null) {
            buttonFavorito.setImageResource(R.drawable.ic_remove_favorito);
        } else {
            buttonFavorito.setImageResource(R.drawable.ic_add_favoritos);
        }
    }
    
    private void refresh() {
        if (processo != null) {
            processo.cancel(true);
        }
        processo = new ProcessoLoadDetalheLinha();
        processo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        //The Refresh must be only active when the offset is zero :
        swipeRefresh.setEnabled(i == 0);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processo != null) {
            processo.cancel(true);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(favoritoReceive);
        task.cancel();
    }
    
    private class ProcessoLoadDetalheLinha extends AsyncTask<Void, String, Boolean> {
        
        private final String TAG = "ProcessoLoadLinhasPonto";
        protected Context context;
        private FragmentManager fragmentManager;
        private RetornoLinhasPonto retornoLinhasPonto;
        
        public ProcessoLoadDetalheLinha() {
            this.context = LinhaDetalheActivity.this;
            this.fragmentManager = getFragmentManager();
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Fragment alert = fragmentManager.findFragmentByTag(AlertDialogFragmentV11.FRAGMENT_ID);
            if (alert != null) {
                fragmentManager.beginTransaction().remove(alert).commitAllowingStateLoss();
            }
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (FragmentExtended.isOnline(context)) {
                    try {
                        
                        String url = Constantes.detalharLinha;
                        String urlParam = "{\"pontoDeOrigemId\": " + pontoTO.getIdPonto() + ", \"itinerarioId\": " + estimativa
                                .getItinerarioId() + "}";
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);
                        
                        String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);
                        
                        retornoLinhasPonto = MainActivity.mapper.readValue(retorno, RetornoLinhasPonto.class);
                        LogHelper.log(TAG, retornoLinhasPonto.getEstimativas().size() + " item(s)");
                        
                        Collections.sort(retornoLinhasPonto.getEstimativas(), new Comparator<Estimativa>() {
                            @Override
                            public int compare(Estimativa lhs, Estimativa rhs) {
                                return lhs.getHorarioNaOrigem().compareTo(rhs.getHorarioNaOrigem());
                            }
                        });
                        
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
                    
                    boolean has = retornoLinhasPonto.getEstimativas() != null && !retornoLinhasPonto.getEstimativas().isEmpty();
                    recyclerView.setVisibility(has ? View.VISIBLE : View.GONE);
                    //                    emptyView.setVisibility(has ? View.GONE : View.VISIBLE);
                    
                    List<Estimativa> estimativas = retornoLinhasPonto.getEstimativas();
                    
                    EstimativaLinhaRecyclerAdapter adapter = new EstimativaLinhaRecyclerAdapter(context, estimativas, retornoLinhasPonto
                            .getHorarioDoServidor());
                    recyclerView.setAdapter(adapter);
                }
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }
            processo = null;
        }
        
    }
}
