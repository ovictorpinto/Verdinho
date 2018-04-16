package com.github.ovictorpinto;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorpinto on 23/06/17. 
 */

public class ConstantesEmpresa {
    
    public static String listarPontos = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/pesquisarPontosDeParada";
    public static String listarLinhas = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/listarItinerarios";
    public static String detalharPontos = "https://pmv.geocontrol.com.br/pontovitoria/svc/json/db/listarPontosDeParada";
    public static String linhasPonto = "https://pmv.geocontrol.com.br/pontovitoria/svc/estimativas/obterEstimativasPorOrigem";
    public static String detalharLinha = "https://pmv.geocontrol.com.br/pontovitoria/svc/estimativas/obterEstimativasPorOrigemEItinerario";
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
