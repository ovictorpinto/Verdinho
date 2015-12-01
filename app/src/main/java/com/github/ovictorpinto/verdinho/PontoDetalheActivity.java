package com.github.ovictorpinto.verdinho;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.persistencia.dao.LinhaFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.LinhaFavoritoPO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoLinhasPonto;
import com.github.ovictorpinto.verdinho.retorno.RetornoListarLinhas;
import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import br.com.mobilesaude.androidlib.widget.AlertDialogFragmentV11;
import br.com.tcsistemas.common.net.HttpHelper;

public class PontoDetalheActivity extends AppCompatActivity {

    public static final long TIME_REFRESH_MILI = 30 * 1000;
    private PontoTO pontoTO;
    private ProcessoLoadLinhasPonto processo;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private HashMap<Integer, LinhaTO> mapLinhas;
    private FloatingActionButton buttonFavorito;
    private BroadcastReceiver updatePontoFavoritoReceive;
    private View progress;
    private View emptyView;
    private BroadcastReceiver updateLinhaFavoritoReceive;

    private Timer timerAtual = new Timer();
    private TimerTask task;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_ponto_detalhe);

        pontoTO = (PontoTO) getIntent().getSerializableExtra(PontoTO.PARAM);
        setTitle(getString(R.string.ponto_n_, pontoTO.getIdentificador()));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        progress = findViewById(R.id.layout_progress);
        progress.setVisibility(View.VISIBLE);

        emptyView = findViewById(android.R.id.empty);
        ((ImageView) emptyView.findViewById(R.id.image)).setImageResource(R.drawable.error);
        ((TextView) emptyView.findViewById(R.id.textview_title)).setText(R.string.ponto_empty_title);
        ((TextView) emptyView.findViewById(R.id.textview_subtitle)).setText(R.string.ponto_empty_subtitle);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        refresh();

        buttonFavorito = (FloatingActionButton) findViewById(R.id.fab);
        buttonFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PontoFavoritoDAO dao = new PontoFavoritoDAO(PontoDetalheActivity.this);
                PontoFavoritoPO banco = dao.findByPK(pontoTO.getIdPonto().toString());
                if (banco == null) {
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
                }
                LocalBroadcastManager.getInstance(PontoDetalheActivity.this)
                                     .sendBroadcast(new Intent(Constantes.actionUpdatePontoFavorito));
            }
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

        timerAtual.schedule(task, TIME_REFRESH_MILI, TIME_REFRESH_MILI);
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
        if (processo != null) {
            processo.cancel(true);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updatePontoFavoritoReceive);
        task.cancel();
    }

    private class ProcessoLoadLinhasPonto extends AsyncTask<Void, String, Boolean> {

        private final String TAG = "ProcessoLoadLinhasPonto";
        protected Context context;
        private FragmentManager fragmentManager;
        private RetornoLinhasPonto retornoLinhasPonto;
        private RetornoListarLinhas retornoListarLinhas;

        public ProcessoLoadLinhasPonto() {
            this.context = PontoDetalheActivity.this;
            this.fragmentManager = getFragmentManager();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                if (FragmentExtended.isOnline(context)) {
                    try {

                        String url = Constantes.linhasPonto;
                        String urlParam = "{\"pontoDeOrigemId\": " + pontoTO.getIdPonto() + "}";

                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);

                        String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);

                        retornoLinhasPonto = MainActivity.mapper.readValue(retorno, RetornoLinhasPonto.class);
                        LogHelper.log(TAG, retornoLinhasPonto.getEstimativas().size() + " item(s)");

                        //vejo as linhas favoritas
                        LinhaFavoritoDAO linhaFavoritoDAO = new LinhaFavoritoDAO(context);
                        List<LinhaFavoritoPO> favoritoPOList = linhaFavoritoDAO.findAll();
                        Set<String> favoritoSet = new HashSet<>();
                        for (int i = 0; i < favoritoPOList.size(); i++) {
                            LinhaFavoritoPO linhaFavoritoPO = favoritoPOList.get(i);
                            favoritoSet.add(linhaFavoritoPO.getId());
                        }

                        List<Estimativa> tmp = retornoLinhasPonto.getEstimativas();
                        List<Estimativa> estimativas = new ArrayList<>();

                        Set<Integer> linhas = new HashSet<>();
                        for (int i = 0; i < tmp.size(); i++) {
                            Estimativa estimativa = tmp.get(i);
                            if (linhas.add(estimativa.getItinerarioId())) {//só exibo uma estimativa de cada linha
                                estimativas.add(estimativa);
                            }
                        }

                        retornoLinhasPonto.setEstimativas(estimativas);

                        url = Constantes.listarLinhas;
                        urlParam = "{\"listaIds\": " + linhas.toString() + " }";
                        LogHelper.log(TAG, url);
                        LogHelper.log(TAG, urlParam);

                        retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                        LogHelper.log(TAG, retorno);

                        retornoListarLinhas = MainActivity.mapper.readValue(retorno, RetornoListarLinhas.class);
                        LogHelper.log(TAG, retornoListarLinhas.getLinhas().size() + " item(s)");

                        mapLinhas = new HashMap<>(retornoListarLinhas.getLinhas().size());
                        for (int i = 0; i < retornoListarLinhas.getLinhas().size(); i++) {
                            LinhaTO linha = retornoListarLinhas.getLinhas().get(i);
                            mapLinhas.put(linha.getId(), linha);
                        }

                        //se existe pelo menos um favorito
                        boolean hasFavorito = false;

                        for (int i = 0; i < estimativas.size(); i++) {
                            Estimativa estimativa = estimativas.get(i);
                            boolean favorito = favoritoSet.contains(mapLinhas.get(estimativa.getItinerarioId()).getIdentificadorLinha());
                            hasFavorito = hasFavorito | favorito;
                            estimativa.setFavorito(favorito);
                        }

                        Collections.sort(retornoLinhasPonto.getEstimativas(), new Comparator<Estimativa>() {
                            @Override
                            public int compare(Estimativa lhs, Estimativa rhs) {
                                int fav = lhs.isFavorito().compareTo(rhs.isFavorito());
                                if (fav == 0) {
                                    return lhs.getHorarioNaOrigem().compareTo(rhs.getHorarioNaOrigem());
                                } else {
                                    return fav;
                                }
                            }
                        });

                        if (hasFavorito) {
                            //inclui os headers
                            List<Estimativa> favoritos = new ArrayList<>();
                            List<Estimativa> comuns = new ArrayList<>();

                            Estimativa linhasFavoritas = new Estimativa();
                            linhasFavoritas.setVeiculo(getString(R.string.linhas_favoritas));

                            Estimativa linhasComuns = new Estimativa();
                            linhasComuns.setVeiculo(getString(R.string.linhas));

                            List<Estimativa> estimativas1 = retornoLinhasPonto.getEstimativas();
                            for (int i = 0; i < estimativas1.size(); i++) {
                                Estimativa estimativa = estimativas1.get(i);
                                if (estimativa.isFavorito()) {
                                    favoritos.add(estimativa);
                                } else {
                                    comuns.add(estimativa);
                                }

                            }

                            List<Estimativa> comHeaders = new ArrayList<>();
                            comHeaders.add(linhasFavoritas);
                            comHeaders.addAll(favoritos);
                            comHeaders.add(linhasComuns);
                            comHeaders.addAll(comuns);

                            retornoLinhasPonto.setEstimativas(comHeaders);

                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (!isCancelled()) {
                if (!success) {
                    //abrir uma nova janela de erro
                    AlertDialogFragmentV11 alert = AlertDialogFragmentV11.newInstance(null, null, R.string.falha_acesso_servidor);
                    fragmentManager.beginTransaction().add(alert, AlertDialogFragmentV11.FRAGMENT_ID).commitAllowingStateLoss();
                } else {

                    boolean has = retornoLinhasPonto != null && retornoLinhasPonto.getEstimativas() != null && !retornoLinhasPonto
                            .getEstimativas().isEmpty();
                    recyclerView.setVisibility(has ? View.VISIBLE : View.GONE);
                    emptyView.setVisibility(has ? View.GONE : View.VISIBLE);

                    if (retornoLinhasPonto != null) {
                        EstimativaPontoRecyclerAdapter adapter = new EstimativaPontoRecyclerAdapter(context, retornoLinhasPonto
                                .getEstimativas(), retornoLinhasPonto.getHorarioDoServidor(), mapLinhas, pontoTO);
                        recyclerView.setAdapter(adapter);
                    }

                }
                swipeRefreshLayout.setRefreshing(false);
                progress.setVisibility(View.GONE);
            }
            processo = null;
        }

    }
}
