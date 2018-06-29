package com.github.ovictorpinto;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorpinto on 28/06/18.
 */

public class ConstantesEmpresa {
    
    private static String PREFIXO = "https://pmv.geocontrol.com.br/pontovitoria/svc/";
    public static String listarPontos = PREFIXO + "json/db/pesquisarPontosDeParada";
    public static String listarLinhas = PREFIXO + "json/db/listarItinerarios";
    public static String detalharPontos = PREFIXO + "json/db/listarPontosDeParada";
    public static String linhasPonto = PREFIXO + "estimativas/obterEstimativasPorOrigem";
    public static String detalharLinha = PREFIXO + "estimativas/obterEstimativasPorOrigemEItinerario";
    public static String linhasTrecho = PREFIXO + "estimativas/obterEstimativasPorOrigemEDestino";
    
    public static String ENVELOPE = "[-40.2558446019482, -20.3411474261535, -40.3615017219324, -20.1865857661999]";
    
    private Context context;
    
    public ConstantesEmpresa(Context context) {
        this.context = context;
    }
    
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
