package com.github.ovictorpinto.verdinho.util;

import android.content.Context;
import android.os.Bundle;

import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by victorpinto on 22/06/17. 
 */

public class AnalyticsHelper {
    
    private FirebaseAnalytics mFirebaseAnalytics;
    
    public AnalyticsHelper(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
    
    public void selecionouLinha(Estimativa item) {
        Bundle bundle = new Bundle();
        fillEstimativa(item, bundle);
        mFirebaseAnalytics.logEvent("select_linha", bundle);
    }
    
    public void selecionouPonto(PontoTO pontoTO, String origem) {
        Bundle bundle = new Bundle();
        fillPonto(pontoTO, bundle);
        mFirebaseAnalytics.logEvent("select_ponto", bundle);
    }
    
    public void favoritou(PontoTO pontoTO, String origem) {
        Bundle bundle = new Bundle();
        fillPonto(pontoTO, bundle);
        bundle.putString(FirebaseAnalytics.Param.ORIGIN, origem);
        mFirebaseAnalytics.logEvent("add_favorito_ponto", bundle);
    }
    
    public void removeuFavoritou(PontoTO pontoTO, String origem) {
        Bundle bundle = new Bundle();
        fillPonto(pontoTO, bundle);
        bundle.putString(FirebaseAnalytics.Param.ORIGIN, origem);
        mFirebaseAnalytics.logEvent("remove_favorito_ponto", bundle);
    }
    
    public void clickMapa() {
        mFirebaseAnalytics.logEvent("click_mapa", new Bundle());
    }
    
    public void clickSobre() {
        mFirebaseAnalytics.logEvent("click_sobre", new Bundle());
    }
    
    public void clickFavorito() {
        mFirebaseAnalytics.logEvent("click_favorito", new Bundle());
    }
    
    public void forceRefresh(PontoTO pontoTO) {
        Bundle bundle = new Bundle();
        fillPonto(pontoTO, bundle);
        mFirebaseAnalytics.logEvent("force_refresh_ponto", bundle);
    }
    
    public void forceRefresh(PontoTO pontoTO, LinhaTO linhaTO) {
        Bundle bundle = new Bundle();
        fillPonto(pontoTO, bundle);
        fillLinha(linhaTO, bundle);
        mFirebaseAnalytics.logEvent("force_refresh_linha", bundle);
    }
    
    private void fillPonto(PontoTO pontoTO, Bundle bundle) {
        bundle.putString("id_ponto", pontoTO.getIdPonto().toString());
        bundle.putString("identificador", pontoTO.getIdentificador());
    }
    
    private void fillEstimativa(Estimativa item, Bundle bundle) {
        String id = item.getItinerarioId() != null ? item.getItinerarioId().toString() : "N/A";
        bundle.putString("intinerario_id", id);
        bundle.putString("veiculo", item.getVeiculo());
    }
    
    private void fillLinha(LinhaTO item, Bundle bundle) {
        bundle.putString("descricao_linha", item.getDescricaoLinha());
        bundle.putString("bandeira_linha", item.getBandeira());
    }
    
    public void clickRatingMaisTarde() {
        mFirebaseAnalytics.logEvent("click_rating_mais_tarde", new Bundle());
    }
    
    public void clickRatingNao() {
        mFirebaseAnalytics.logEvent("click_rating_nao", new Bundle());
    }
    
    public void clickRatingSim() {
        mFirebaseAnalytics.logEvent("click_rating_sim", new Bundle());
    }
}
