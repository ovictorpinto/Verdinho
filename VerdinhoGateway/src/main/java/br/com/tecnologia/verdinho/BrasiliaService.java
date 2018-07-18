package br.com.tecnologia.verdinho;

import br.com.tecnologia.verdinho.model.LinhaTO;
import br.com.tecnologia.verdinho.retorno.RetornoDetalharPontos;
import br.com.tecnologia.verdinho.retorno.RetornoListarLinhas;
import br.com.tecnologia.verdinho.util.SslUtil;
import okhttp3.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.IOException;

@Path(value = "brasilia")
@Produces("application/json")
@Consumes("application/json")
public class BrasiliaService {

    private static String prefix = "https://csodf.geocontrol.com.br/previsao/";
    public static String listarPontos = prefix + "svc/json/db/pesquisarPontosDeParada";
    public static String detalharPontos = prefix + "svc/json/db/listarPontosDeParada";
    public static String linhasPonto = prefix + "svc/estimativas/obterEstimativasPorOrigem";
    public static String listarLinhas = prefix + "svc/json/db/listarItinerarios";
    public static String detalharLinha = prefix + "svc/estimativas/obterEstimativasPorOrigemEItinerario";
    public static String linhasTrecho = prefix + "svc/estimativas/obterEstimativasPorOrigemEDestino";

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @POST
    @Path("pesquisarPontosDeParada")
    public String getPesquisarPontosDeParada(String json) {
        try {
            return post(listarPontos, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = SslUtil.trustAllSslClient(client).newCall(request).execute();
        return response.body().string();
    }


    @POST
    @Path("listarPontosDeParada")
    public RetornoDetalharPontos getListarPontosDeParada(String json) {
        String retorno;
        try {
            retorno = post(detalharPontos, json);
            return VerdinhoService.mapper.readValue(retorno, RetornoDetalharPontos.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigem")
    public String getObterEstimativasPorOrigem(String json) {
        System.out.println("obterEstimativasPorOrigem");
        String url = linhasPonto;
        try {
            return post(url, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("listarItinerarios")
    public RetornoListarLinhas getListarItinerarios(String json) {
        String url = listarLinhas;
        try {
            String retorno = post(url, json);
            RetornoListarLinhas parsed = VerdinhoService.mapper.readValue(retorno, RetornoListarLinhas.class);
            for (LinhaTO item : parsed.getLinhas()) {
                item.setBandeira(item.getBandeira() + " - " + item.getComplemento());
            }
            return parsed;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigemEItinerario")
    public String getObterEstimativasPorOrigemEItinerario(String json) {
        try {
            return post(detalharLinha, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @POST
    @Path("obterEstimativasPorOrigemEDestino")
    public String getObterEstimativasPorOrigemEDestino(String json) {
        try {
            return post(linhasTrecho, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
