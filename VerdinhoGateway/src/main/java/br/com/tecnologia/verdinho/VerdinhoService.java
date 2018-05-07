package br.com.tecnologia.verdinho;

import br.com.tcsistemas.common.net.HttpHelper;
import br.com.tecnologia.verdinho.model.Estimativa;
import br.com.tecnologia.verdinho.retorno.RetornoDetalharPontos;
import br.com.tecnologia.verdinho.retorno.RetornoLinhasPonto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path(value = "/")
@Produces("application/json")
@Consumes("application/json")
public class VerdinhoService {

    private static String prefix = "https://api.es.gov.br/ceturb/transcolOnline/";
    public static String listarPontos = prefix + "svc/json/db/pesquisarPontosDeParada";
    public static String detalharPontos = prefix + "svc/json/db/listarPontosDeParada";
    public static String linhasPonto = prefix + "svc/estimativas/obterEstimativasPorOrigem";
    public static String listarLinhas = prefix + "svc/json/db/listarItinerarios";
    public static String detalharLinha = prefix + "svc/estimativas/obterEstimativasPorOrigemEItinerario";
    public static String linhasTrecho = prefix + "svc/estimativas/obterEstimativasPorOrigemEDestino";

    private static long minutoInMili = 60 * 1000;

    static ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    static JavaType listJsonNode = mapper.getTypeFactory().constructParametricType(List.class, JsonNode.class);
    static Map<String, String> headers;

    static {
        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
    }

    @POST
    @Path("pesquisarPontosDeParada")
    public String getPesquisarPontosDeParada(String json) {
        String retorno;
        try {
            retorno = HttpHelper.doPost(listarPontos, json, HttpHelper.UTF8, headers);
            return retorno;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("listarPontosDeParada")
    public RetornoDetalharPontos getListarPontosDeParada(String json) {
        String retorno;
        try {
            retorno = HttpHelper.doPost(detalharPontos, json, HttpHelper.UTF8, headers);
            List<JsonNode> parsed = mapper.readValue(retorno, listJsonNode);
            RetornoDetalharPontos detalharPontos = new RetornoDetalharPontos();
            detalharPontos.setPontosDeParada(parsed);
            return detalharPontos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigem")
    public RetornoLinhasPonto getObterEstimativasPorOrigem(String json) {
        System.out.println("obterEstimativasPorOrigem");
        String url = linhasPonto;
        try {
            String retorno = HttpHelper.doPost(url, json, HttpHelper.UTF8, headers);
            JavaType listEstimativaClazz = mapper.getTypeFactory().constructParametricType(List.class, Estimativa.class);
            List<Estimativa> parsed = mapper.readValue(retorno, listEstimativaClazz);
            long agora = System.currentTimeMillis();
            for (Estimativa estimativa : parsed) {
                long proximo = agora + estimativa.getPrevisaoNaOrigemEmMinutos() * minutoInMili;
                estimativa.setHorarioNaOrigem(String.valueOf(proximo));
            }
            RetornoLinhasPonto retornoLinhasPonto = new RetornoLinhasPonto();
            retornoLinhasPonto.setEstimativas(parsed);
            retornoLinhasPonto.setHorarioDoServidor(System.currentTimeMillis());
            return retornoLinhasPonto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("listarItinerarios")
    public String getListarItinerarios(String json) {
        String url = listarLinhas;
        String retorno;
        try {
            retorno = HttpHelper.doPost(url, json, HttpHelper.UTF8, headers);
            return retorno;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigemEItinerario")
    public String getObterEstimativasPorOrigemEItinerario(String json) {
        String url = detalharLinha;
        String retorno;
        try {
            retorno = HttpHelper.doPost(url, json, HttpHelper.UTF8, headers);
            return retorno;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigemEDestino")
    public RetornoLinhasPonto getObterEstimativasPorOrigemEDestino(String json) {
        System.out.println("obterEstimativasPorOrigemEDestino");
        String url = linhasTrecho;
        try {
            String retorno = HttpHelper.doPost(url, json, HttpHelper.UTF8, headers);
            JavaType listEstimativaClazz = mapper.getTypeFactory().constructParametricType(List.class, Estimativa.class);
            List<Estimativa> parsed = mapper.readValue(retorno, listEstimativaClazz);
            long agora = System.currentTimeMillis();
            for (Estimativa estimativa : parsed) {
                long proximo = agora + estimativa.getPrevisaoNaOrigemEmMinutos() * minutoInMili;
                estimativa.setHorarioNaOrigem(String.valueOf(proximo));

                proximo = agora + estimativa.getPrevisaoNoDestinoEmMinutos() * minutoInMili;
                estimativa.setHorarioNoDestino(String.valueOf(proximo));
            }
            RetornoLinhasPonto retornoLinhasPonto = new RetornoLinhasPonto();
            retornoLinhasPonto.setEstimativas(parsed);
            retornoLinhasPonto.setHorarioDoServidor(System.currentTimeMillis());
            return retornoLinhasPonto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
