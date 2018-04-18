package com.github.ovictorpinto.verdinho.ui.main;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.ponto.PontoDetalheActivity;
import com.github.ovictorpinto.verdinho.ui.ponto.RenomearDialogFrag;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.github.ovictorpinto.verdinho.util.AwarenessHelper;
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class PontoFavoritoFragment extends Fragment {
    
    private static final String TAG = "Favorito";
    private BroadcastReceiver favoritosUpdate;
    private RecyclerView recyclerView;
    private View emptyView;
    private View coordinator;
    private AnalyticsHelper analyticsHelper;
    
    private GoogleApiClient mGoogleApiClient;
    private FavoritoRecyclerAdapter adapter;
    
    public PontoFavoritoFragment() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritosUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refresh();
            }
        };
        LocalBroadcastManager.getInstance(getActivity())
                             .registerReceiver(favoritosUpdate, new IntentFilter(Constantes.actionUpdatePontoFavorito));
        
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Awareness.API).build();
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (favoritosUpdate != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(favoritosUpdate);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        analyticsHelper = new AnalyticsHelper(getActivity());
        
        View mainView = inflater.inflate(R.layout.ly_recycler, null);
        coordinator = mainView.findViewById(R.id.coordinator);
        
        recyclerView = mainView.findViewById(R.id.recyclerview);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        
        emptyView = mainView.findViewById(android.R.id.empty);
        ((ImageView) emptyView.findViewById(R.id.image)).setImageResource(R.drawable.estrela_favorito);
        ((TextView) emptyView.findViewById(R.id.textview_title)).setText(R.string.favoritos_empty_title);
        ((TextView) emptyView.findViewById(R.id.textview_subtitle)).setText(R.string.favoritos_empty_subtitle);
        refresh();
        
        return mainView;
    }
    
    private void refresh() {
        PontoFavoritoDAO dao = new PontoFavoritoDAO(getActivity());
        List<PontoPO> allFavoritos = dao.findAllFavoritos();
        List<PontoTO> all = new ArrayList<>(allFavoritos.size());
        for (int i = 0; i < allFavoritos.size(); i++) {
            PontoPO allFavorito = allFavoritos.get(i);
            all.add(allFavorito.getPontoTO());
        }
        FavoritoRecyclerAdapter.FavoritoListener listener = new FavoritoRecyclerAdapter.FavoritoListener() {
            @Override
            public void onClick(final PontoTO pontoTO) {
                analyticsHelper.selecionouPonto(pontoTO, "favorito");
                Intent i = new Intent(getActivity(), PontoDetalheActivity.class);
                i.putExtra(PontoTO.PARAM, pontoTO);
                startActivity(i);
            }
            
            @Override
            public void onEnableNotification(PontoTO pontoTO) {
                analyticsHelper.habilitouNotificacao(pontoTO);
                pontoTO.setNotificacao(true);
                PontoDAO dao = new PontoDAO(getActivity());
                dao.update(new PontoPO(pontoTO));
                Snackbar.make(coordinator, R.string.notificacao_habilitada, Snackbar.LENGTH_SHORT).show();
                new AwarenessHelper(getActivity()).criaFenda(pontoTO, mGoogleApiClient);
            }
            
            @Override
            public void onDisableNotification(PontoTO pontoTO) {
                analyticsHelper.desabilitouNotificacao(pontoTO);
                pontoTO.setNotificacao(false);
                PontoDAO dao = new PontoDAO(getActivity());
                dao.update(new PontoPO(pontoTO));
                Snackbar.make(coordinator, R.string.notificacao_desabilitada, Snackbar.LENGTH_SHORT).show();
                new AwarenessHelper(getActivity()).removeFenda(pontoTO, mGoogleApiClient);
            }
            
            @Override
            public void onRename(PontoTO pontoTO) {
                analyticsHelper.openRename();
                RenomearDialogFrag fragment = new RenomearDialogFrag();
                Bundle arguments = new Bundle();
                arguments.putSerializable(PontoTO.PARAM, pontoTO);
                fragment.setArguments(arguments);
                getFragmentManager().beginTransaction().add(fragment, null).commitAllowingStateLoss();
            }
            
        };
        adapter = new FavoritoRecyclerAdapter(getActivity(), all, listener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        emptyView.setVisibility(allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(!allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && adapter != null && adapter.getItemCount() > 0) {
            exibeDialogoProximidade();
        }
    }
    
    private void exibeDialogoProximidade() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean jaExibiu = sharedPreferences.getBoolean(Constantes.pref_show_proximidade, false);
        if (!jaExibiu) {
            getFragmentManager().beginTransaction().add(new ProximidadeDialogFrag(), null).commitAllowingStateLoss();
            sharedPreferences.edit().putBoolean(Constantes.pref_show_proximidade, true).apply();
        }
    }
}
