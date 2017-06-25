package com.github.ovictorpinto;

import android.content.Context;

import com.github.ovictorpinto.verdinho.BuildConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by victorpinto on 23/06/17. 
 */

public class ConstantesEmpresa {
    private static String prefix = "https://buscabus-prodest.geocontrol.com.br/";
    public static String listarPontos = prefix + "svc/json/db/pesquisarPontosDeParada";
    public static String listarLinhas = prefix + "svc/json/db/listarItinerarios";
    public static String detalharPontos = prefix + "svc/json/db/listarPontosDeParada";
    public static String linhasPonto = prefix + "svc/estimativas/obterEstimativasPorOrigem";
    public static String detalharLinha = prefix + "svc/estimativas/obterEstimativasPorOrigemEItinerario";
    public static String ENVELOPE = "[-39.482856, -17.888743, -42.075630, -21.160240]";
    
    private Context context;
    
    public ConstantesEmpresa(Context context) {
        this.context = context;
    }
    
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", BuildConfig.TRANSCOL_AUTH);
        return headers;
    }
}
