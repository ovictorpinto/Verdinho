package com.github.ovictorpinto.verdinho;

/**
 * Created by victorpinto on 21/10/15.
 */
public interface Constantes {

    String listarPontos = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/pesquisarPontosDeParada";
    String listarLinhas = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/listarItinerarios";
    String detalharPontos = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/listarPontosDeParada";
    String linhasPonto = "https://pmv.geocontrol.com.br/pontovitoria/svc/estimativas/obterEstimativasPorOrigem";
    String detalharLinha = "https://pmv.geocontrol.com.br/pontovitoria/svc/estimativas/obterEstimativasPorOrigemEItinerario";

    String pref_loaded = "loadedPontos";

    String actionUpdatePontoFavorito = "updateFavoritos";
    String actionUpdateLinhaFavorito = "updateLinhaFavoritos";

}
