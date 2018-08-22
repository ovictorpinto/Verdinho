package com.github.ovictorpinto.verdinho.ui.main;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ovictorpinto.ConstantesEmpresa;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoDAO;
import com.github.ovictorpinto.verdinho.persistencia.dao.PontoFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoDetalharPontos;
import com.github.ovictorpinto.verdinho.retorno.RetornoPesquisarPontos;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import br.com.tcsistemas.common.net.HttpHelper;
import br.com.tcsistemas.common.string.StringHelper;

/**
 * Created by victorpinto on 19/04/18. 
 */
class ProcessoLoadPontos extends AsyncTask<Void, String, Boolean> {
    
    private final String TAG = "ProcessoLoadPontos";
    protected Context context;
    
    ProcessoLoadPontos(Context context) {
        this.context = context;
    }
    
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        
        try {
            if (FragmentExtended.isOnline(context)) {
                try {
                    ObjectMapper mapper = MainActivity.mapper;
                    
                    String url = ConstantesEmpresa.listarPontos;
                    String urlParam = "{\"envelope\":" + ConstantesEmpresa.ENVELOPE + "}";
                    Map<String, String> headers = new ConstantesEmpresa(context).getHeaders();
                    
                    LogHelper.log(TAG, url);
                    LogHelper.log(TAG, urlParam);
                    
                    String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                    LogHelper.log(TAG, retorno);
                    
                    RetornoPesquisarPontos retornoPesquisarPontos = mapper.readValue(retorno, RetornoPesquisarPontos.class);
                    LogHelper.log(TAG, retornoPesquisarPontos.getPontosDeParada().size() + " item(s)");
                    
                    url = ConstantesEmpresa.detalharPontos;
                    urlParam = "{\"listaIds\": " + retornoPesquisarPontos.getPontosDeParada().toString() + " }";
                    LogHelper.log(TAG, url);
                    LogHelper.log(TAG, urlParam);
                    
                    retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                    LogHelper.log(TAG, retorno);
                    
                    RetornoDetalharPontos retornoDetalharPontos = mapper.readValue(retorno, RetornoDetalharPontos.class);
                    LogHelper.log(TAG, retornoDetalharPontos.getPontosDeParada().size() + " item(s)");
                    
                    List<PontoTO> pontosDeParada = retornoDetalharPontos.getPontosDeParada();
                    
                    PontoFavoritoDAO favoritoDAO = new PontoFavoritoDAO(context);
                    List<PontoPO> allFavoritos = favoritoDAO.findAllFavoritos();
                    SparseArray<String> apelidosPontos = new SparseArray<>(allFavoritos.size());
                    for (PontoPO pontoPO : allFavoritos) {
                        PontoTO pontoTO = pontoPO.getPontoTO();
                        if (StringHelper.isNotBlank(pontoTO.getApelido())) {
                            apelidosPontos.put(pontoTO.getIdPonto(), pontoTO.getApelido());
                        }
                    }
                    
                    PontoDAO dao = new PontoDAO(context);
                    dao.removeAll();
                    for (int i = 0; i < pontosDeParada.size(); i++) {
                        onProgressUpdate("Atualizando " + i + " de " + pontosDeParada.size() +" pontos…");
                        PontoTO pontoTO = pontosDeParada.get(i);
                        //salva os apelidos dos pontos cadastrados pelo usuário
                        String apelido = apelidosPontos.get(pontoTO.getIdPonto());
                        if (apelido != null) {
                            pontoTO.setApelido(apelido);
                        }
                        dao.create(new PontoPO(pontoTO));
                    }
                    return true;
                    
                } catch (UnknownHostException e) {
                    LogHelper.log(e);
                }
            }
        } catch (Exception e) {
            LogHelper.log(e);
        }
        return false;
    }
}
