package com.github.ovictorpinto;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorpinto on 28/06/18.
 */

public class ConstantesEmpresa {
    
    private static String prefix = "https://r29tecnologia.com.br/verdinho-gateway/svc/brasilia/";
    public static String listarPontos = prefix + "pesquisarPontosDeParada";
    public static String detalharPontos = prefix + "listarPontosDeParada";
    public static String listarLinhas = prefix + "listarItinerarios";
    public static String linhasPonto = prefix + "obterEstimativasPorOrigem";
    public static String detalharLinha = prefix + "obterEstimativasPorOrigemEItinerario";
    public static String linhasTrecho = prefix + "obterEstimativasPorOrigemEDestino";
    
    public static String ENVELOPE = "[-49.2746124,-14.4907389,-45.3122787,-17.0583511]";
    public static final LatLng POSICAO_SEDE = new LatLng(-15.793722, -47.883537);
    
    public ConstantesEmpresa(Context context) {
    }
    
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
