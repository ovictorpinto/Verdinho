package com.github.ovictorpinto.verdinho.ui.ponto;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.main.EstimativaPontoRecyclerAdapter;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.github.ovictorpinto.verdinho.util.AwarenessHelper;
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;

public class PontoDetalheActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {
    
    public static final long TIME_REFRESH_MILI = 30 * 1000;
    private PontoTO pontoTO;
    
    private View progress;
    private View emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FloatingActionButton buttonFavorito;
    private AppBarLayout appBarLayout;
    
    private BroadcastReceiver updatePontoFavoritoReceive;
    private BroadcastReceiver updateLinhaFavoritoReceive;
    private ProcessoLoadLinhasPonto processo;
    private AnalyticsHelper analyticsHelper;
    
    private boolean fotoExpandida = false;
    private Timer timerAtual;
    private TimerTask task;
    private final Handler handler = new Handler();
    private EstimativaPontoRecyclerAdapter estimativaPontoRecyclerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_ponto_detalhe);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        analyticsHelper = new AnalyticsHelper(this);
        pontoTO = (PontoTO) getIntent().getSerializableExtra(PontoTO.PARAM);
        
        appBarLayout = findViewById(R.id.app_bar);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        setTitle(pontoTO.getNomeApresentacao(this));
        
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        
        progress = findViewById(R.id.layout_progress);
        progress.setVisibility(View.VISIBLE);
        
        emptyView = findViewById(android.R.id.empty);
        ((ImageView) emptyView.findViewById(R.id.image)).setImageResource(R.drawable.error);
        ((TextView) emptyView.findViewById(R.id.textview_title)).setText(R.string.ponto_empty_title);
        ((TextView) emptyView.findViewById(R.id.textview_subtitle)).setText(R.string.ponto_empty_subtitle);
        
        swipeRefreshLayout = findViewById(R.id.swipe);
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            analyticsHelper.forceRefresh(pontoTO);
            refresh();
        });
        refresh();
        
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Awareness.API).build();
        mGoogleApiClient.connect();
        
        buttonFavorito = findViewById(R.id.fab);
        buttonFavorito.setOnClickListener(view -> {
            final PontoFavoritoDAO dao = new PontoFavoritoDAO(PontoDetalheActivity.this);
            PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
            if (banco == null) {
                analyticsHelper.favoritou(pontoTO, "ponto_detalhe");
                dao.create(new PontoFavoritoPO(pontoTO));
                View.OnClickListener desfazerListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dao.removeByPK(new PontoFavoritoPO(pontoTO));
                        LocalBroadcastManager.getInstance(PontoDetalheActivity.this)
                                             .sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
                    }
                };
                Snackbar.make(view, R.string.ponto_adicionado, Snackbar.LENGTH_SHORT).setAction(R.string.desfazer, desfazerListener)
                        .show();
            } else {
                analyticsHelper.removeuFavoritou(pontoTO, "ponto_detalhe");
                dao.removeByPK(new PontoFavoritoPO(pontoTO));
                View.OnClickListener desfazerListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dao.create(new PontoFavoritoPO(pontoTO));
                        LocalBroadcastManager.getInstance(PontoDetalheActivity.this)
                                             .sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
                    }
                };
                Snackbar.make(view, R.string.ponto_removido, Snackbar.LENGTH_SHORT).setAction(R.string.desfazer, desfazerListener)
                        .show();
                new AwarenessHelper(PontoDetalheActivity.this).removeFenda(pontoTO, mGoogleApiClient);
            }
            LocalBroadcastManager.getInstance(PontoDetalheActivity.this)
                                 .sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
        });
        setButtonFavorito();
        updatePontoFavoritoReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setButtonFavorito();
            }
        };
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(updatePontoFavoritoReceive, new IntentFilter(Constantes.actionUpdatePontoFavorito));
        updateLinhaFavoritoReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(updateLinhaFavoritoReceive, new IntentFilter(Constantes.actionUpdateLinhaFavorito));
        
        exibeLegenda();
        configuraFoto();
    }
    
    private void configuraFoto() {
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
    }
    
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        panorama.setPosition(new LatLng(pontoTO.getLatitude(), pontoTO.getLongitude()));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        timerAtual.cancel();
        timerAtual = null;
        if (estimativaPontoRecyclerAdapter != null) {
            estimativaPontoRecyclerAdapter.onPause();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (timerAtual == null) {
            iniciaRefresh();
        }
        if (estimativaPontoRecyclerAdapter != null) {
            estimativaPontoRecyclerAdapter.onResume();
        }
    }
    
    private void iniciaRefresh() {
        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        refresh();
                    }
                });
            }
        };
        timerAtual = new Timer();
        timerAtual.schedule(task, TIME_REFRESH_MILI, TIME_REFRESH_MILI);
    }
    
    private void exibeLegenda() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean jaExibiu = sharedPreferences.getBoolean(Constantes.pref_show_legenda, false);
        if (!jaExibiu) {
            getFragmentManager().beginTransaction().add(new LegendaDialogFrag(), null).commitAllowingStateLoss();
            sharedPreferences.edit().putBoolean(Constantes.pref_show_legenda, true).apply();
        }
    }
    
    private void refresh() {
        if (processo != null) {
            processo.cancel(true);
        }
        processo = new ProcessoLoadLinhasPonto();
        processo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    private void setButtonFavorito() {
        PontoFavoritoDAO dao = new PontoFavoritoDAO(PontoDetalheActivity.this);
        PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
        if (banco != null) {
            buttonFavorito.setImageResource(R.drawable.ic_remove_favorito);
        } else {
            buttonFavorito.setImageResource(R.drawable.ic_add_favoritos);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (estimativaPontoRecyclerAdapter != null) {
            estimativaPontoRecyclerAdapter.onDestroy();
        }
        task.cancel();
        if (processo != null) {
            processo.cancel(true);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updatePontoFavoritoReceive);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (BuildConfig.USA_PRECO) {
            inflater.inflate(R.menu.menu_preco, menu);
        }
        inflater.inflate(R.menu.menu_legenda, menu);
        inflater.inflate(R.menu.menu_show_foto, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_legenda:
                analyticsHelper.clickLegenda();
                getFragmentManager().beginTransaction().add(new LegendaDialogFrag(), null).commitAllowingStateLoss();
                return true;
            case R.id.menu_monetization:
                analyticsHelper.clickPreco();
                getFragmentManager().beginTransaction().add(new PrecoDialogFrag(), null).commitAllowingStateLoss();
                return true;
            case R.id.menu_photo:
                analyticsHelper.clickFoto();
                fotoExpandida = !fotoExpandida;
                appBarLayout.setExpanded(fotoExpandida, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class ProcessoLoadLinhasPonto extends com.github.ovictorpinto.verdinho.ui.ponto.ProcessoLoadLinhasPonto {
        
        public ProcessoLoadLinhasPonto() {
            super(PontoDetalheActivity.this, PontoDetalheActivity.this.pontoTO);
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Fragment alert = getFragmentManager().findFragmentByTag(AlertDialogFragmentV11.FRAGMENT_ID);
            if (alert != null) {
                getFragmentManager().beginTransaction().remove(alert).commitAllowingStateLoss();
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            
            if (!isCancelled()) {
                if (!success) {
                    //abrir uma nova janela de erro
                    AlertDialogFragmentV11 alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor);
                    getFragmentManager().beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss();
                } else {
                    
                    boolean has = retornoLinhasPonto != null && retornoLinhasPonto.getEstimativas() != null && !retornoLinhasPonto
                            .getEstimativas().isEmpty();
                    recyclerView.setVisibility(has ? View.VISIBLE : View.GONE);
                    emptyView.setVisibility(has ? View.GONE : View.VISIBLE);
                    
                    if (has) {
                        estimativaPontoRecyclerAdapter = new EstimativaPontoRecyclerAdapter(context, retornoLinhasPonto
                                .getEstimativas(), retornoLinhasPonto.getHorarioDoServidor(), mapLinhas, pontoTO);
                        recyclerView.setAdapter(estimativaPontoRecyclerAdapter);
                    }
                    
                }
                swipeRefreshLayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
            }
            processo = null;
        }
        
    }
}
