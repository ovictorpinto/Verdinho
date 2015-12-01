package com.github.ovictorpinto.verdinho;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class PontoFavoritoFragment extends Fragment {

    private BroadcastReceiver favoritosUpdate;
    private RecyclerView recyclerView;
    private View emptyView;

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
        View mainView = inflater.inflate(R.layout.ly_recycler, null);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.recyclerview);
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
        recyclerView.setAdapter(new FavoritoRecyclerAdapter(getActivity(), all));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        emptyView.setVisibility(allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(!allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
