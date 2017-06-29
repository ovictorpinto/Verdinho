package com.github.ovictorpinto.verdinho.ui.main;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ovictorpinto.verdinho.BuildConfig;
import com.github.ovictorpinto.verdinho.Constantes;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.ponto.PontoDetalheActivity;
import com.github.ovictorpinto.verdinho.util.AnalyticsHelper;
import com.github.ovictorpinto.verdinho.util.DividerItemDecoration;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

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
                
                AwarenessFence exitFence = LocationFence.exiting(pontoTO.getLatitude(), pontoTO.getLongitude(), 100);
                AwarenessFence inFence = LocationFence.in(pontoTO.getLatitude(), pontoTO.getLongitude(), 100, 5 * 1000);
                
                Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".proximidade.action");
                intent.putExtra(PontoTO.PARAM_ID, pontoTO.getIdPonto());
                
                PendingIntent myPendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                // Register the fence to receive callbacks.
                // The fence key uniquely identifies the fence.
                final ResultCallback<Status> resultCallback = new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Fence was successfully registered.");
                        } else {
                            Log.e(TAG, "Fence could not be registered: " + status);
                        }
                    }
                };
                FenceUpdateRequest.Builder builder = new FenceUpdateRequest.Builder();
                builder.addFence("FANCE_IN_" + pontoTO.getIdPonto(), inFence, myPendingIntent);
                builder.addFence("FANCE_OUT_" + pontoTO.getIdPonto(), exitFence, myPendingIntent);
                
                final FenceUpdateRequest request = builder.build();
                Awareness.FenceApi.updateFences(mGoogleApiClient, request).setResultCallback(resultCallback);
            }
            
            @Override
            public void onDisableNotification(PontoTO pontoTO) {
                analyticsHelper.desabilitouNotificacao(pontoTO);
                pontoTO.setNotificacao(false);
                PontoDAO dao = new PontoDAO(getActivity());
                dao.update(new PontoPO(pontoTO));
                Snackbar.make(coordinator, R.string.notificacao_desabilitada, Snackbar.LENGTH_SHORT).show();
    
                FenceUpdateRequest.Builder builder = new FenceUpdateRequest.Builder();
                builder.removeFence("FANCE_IN_" + pontoTO.getIdPonto());
                builder.removeFence("FANCE_OUT_" + pontoTO.getIdPonto());
                FenceUpdateRequest request = builder.build();
                Awareness.FenceApi
                        .updateFences(mGoogleApiClient, request)
                        .setResultCallback(new ResultCallbacks<Status>() {
                            @Override
                            public void onSuccess(@NonNull Status status) {
                                Log.i(TAG, "Fence successfully removed.");
                            }
                            
                            @Override
                            public void onFailure(@NonNull Status status) {
                                Log.i(TAG, "Fence could NOT be removed.");
                            }
                        });
                
            }
        };
        FavoritoRecyclerAdapter adapter = new FavoritoRecyclerAdapter(getActivity(), all, listener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        emptyView.setVisibility(allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(!allFavoritos.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
