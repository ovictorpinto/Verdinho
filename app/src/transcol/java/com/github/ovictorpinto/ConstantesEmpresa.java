package com.github.ovictorpinto;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorpinto on 23/06/17. 
 */

public class ConstantesEmpresa {
    private static String prefix = "https://r29tecnologia.com.br/verdinho-gateway/svc/";
    public static String listarPontos = prefix + "pesquisarPontosDeParada";
    public static String detalharPontos = prefix + "listarPontosDeParada";
    public static String listarLinhas = prefix + "listarItinerarios";
    public static String linhasPonto = prefix + "obterEstimativasPorOrigem";
    public static String detalharLinha = prefix + "obterEstimativasPorOrigemEItinerario";
    public static String ENVELOPE = "[-39.482856, -17.888743, -42.075630, -21.160240]";
    
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
