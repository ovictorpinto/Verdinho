package com.github.ovictorpinto.verdinho.ui.ponto;

import android.content.Context;
import android.os.AsyncTask;

import com.github.ovictorpinto.ConstantesEmpresa;
import com.github.ovictorpinto.verdinho.R;
import com.github.ovictorpinto.verdinho.persistencia.dao.LinhaFavoritoDAO;
import com.github.ovictorpinto.verdinho.persistencia.po.LinhaFavoritoPO;
import com.github.ovictorpinto.verdinho.retorno.RetornoLinhasPonto;
import com.github.ovictorpinto.verdinho.retorno.RetornoListarLinhas;
import com.github.ovictorpinto.verdinho.to.Estimativa;
import com.github.ovictorpinto.verdinho.to.LinhaTO;
import com.github.ovictorpinto.verdinho.to.PontoTO;
import com.github.ovictorpinto.verdinho.ui.main.MainActivity;
import com.github.ovictorpinto.verdinho.util.FragmentExtended;
import com.github.ovictorpinto.verdinho.util.LogHelper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.tcsistemas.common.net.HttpHelper;

public abstract class ProcessoLoadLinhasPonto extends AsyncTask<Void, String, Boolean> {
    
    private final String TAG = "ProcessoLoadLinhasPonto";
    protected Context context;
    protected PontoTO pontoTO;
    protected RetornoLinhasPonto retornoLinhasPonto;
    protected RetornoListarLinhas retornoListarLinhas;
    protected  HashMap<Integer, LinhaTO> mapLinhas;
    
    public ProcessoLoadLinhasPonto(Context context, PontoTO pontoTO) {
        this.context = context;
        this.pontoTO = pontoTO;
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            
            if (FragmentExtended.isOnline(context)) {
                try {
                    
                    String url = ConstantesEmpresa.linhasPonto;
                    String urlParam = "{\"pontoDeOrigemId\": " + pontoTO.getIdPonto() + "}";
                    
                    Map<String, String> headers = new ConstantesEmpresa(context).getHeaders();
                    LogHelper.log(TAG, url);
                    LogHelper.log(TAG, urlParam);
                    
                    String retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                    LogHelper.log(TAG, retorno);
                    
                    retornoLinhasPonto = MainActivity.mapper.readValue(retorno, RetornoLinhasPonto.class);
                    LogHelper.log(TAG, retornoLinhasPonto.getEstimativas().size() + " item(s)");
                    
                    //vejo as linhas favoritas
                    LinhaFavoritoDAO linhaFavoritoDAO = new LinhaFavoritoDAO(context);
                    List<LinhaFavoritoPO> favoritoPOList = linhaFavoritoDAO.findAll();
                    Set<String> favoritoSet = new HashSet<>();
                    for (int i = 0; i < favoritoPOList.size(); i++) {
                        LinhaFavoritoPO linhaFavoritoPO = favoritoPOList.get(i);
                        favoritoSet.add(linhaFavoritoPO.getId());
                    }
                    
                    List<Estimativa> tmp = retornoLinhasPonto.getEstimativas();
                    //ordena antes de filtrar o primeiro de cada
                    Collections.sort(tmp, new Comparator<Estimativa>() {
                        @Override
                        public int compare(Estimativa lhs, Estimativa rhs) {
                            return lhs.getHorarioNaOrigem().compareTo(rhs.getHorarioNaOrigem());
                        }
                    });
                    List<Estimativa> estimativas = new ArrayList<>();
                    
                    Set<Integer> linhas = new HashSet<>();
                    for (int i = 0; i < tmp.size(); i++) {
                        Estimativa estimativa = tmp.get(i);
                        if (linhas.add(estimativa.getItinerarioId())) {//só exibo uma estimativa de cada linha
                            estimativas.add(estimativa);
                        }
                    }
                    
                    retornoLinhasPonto.setEstimativas(estimativas);
                    
                    url = ConstantesEmpresa.listarLinhas;
                    urlParam = "{\"listaIds\": " + linhas.toString() + " }";
                    LogHelper.log(TAG, url);
                    LogHelper.log(TAG, urlParam);
                    
                    retorno = HttpHelper.doPost(url, urlParam, HttpHelper.UTF8, headers);
                    LogHelper.log(TAG, retorno);
                    
                    retornoListarLinhas = MainActivity.mapper.readValue(retorno, RetornoListarLinhas.class);
                    LogHelper.log(TAG, retornoListarLinhas.getLinhas().size() + " item(s)");
                    
                    mapLinhas = new HashMap<>(retornoListarLinhas.getLinhas().size());
                    for (int i = 0; i < retornoListarLinhas.getLinhas().size(); i++) {
                        LinhaTO linha = retornoListarLinhas.getLinhas().get(i);
                        mapLinhas.put(linha.getId(), linha);
                    }
                    
                    //se existe pelo menos um favorito
                    boolean hasFavorito = false;
                    
                    for (int i = 0; i < estimativas.size(); i++) {
                        Estimativa estimativa = estimativas.get(i);
                        boolean favorito = favoritoSet.contains(mapLinhas.get(estimativa.getItinerarioId()).getIdentificadorLinha());
                        hasFavorito = hasFavorito | favorito;
                        estimativa.setFavorito(favorito);
                    }
                    
                    Collections.sort(retornoLinhasPonto.getEstimativas(), new Comparator<Estimativa>() {
                        @Override
                        public int compare(Estimativa lhs, Estimativa rhs) {
                            int fav = lhs.isFavorito().compareTo(rhs.isFavorito());
                            if (fav == 0) {
                                return lhs.getHorarioNaOrigem().compareTo(rhs.getHorarioNaOrigem());
                            } else {
                                return fav;
                            }
                        }
                    });
                    
                    if (hasFavorito) {
                        //inclui os headers
                        List<Estimativa> favoritos = new ArrayList<>();
                        List<Estimativa> comuns = new ArrayList<>();
                        
                        Estimativa linhasFavoritas = new Estimativa();
                        linhasFavoritas.setVeiculo(context.getString(R.string.linhas_favoritas));
                        
                        Estimativa linhasComuns = new Estimativa();
                        linhasComuns.setVeiculo(context.getString(R.string.linhas));
                        
                        List<Estimativa> estimativas1 = retornoLinhasPonto.getEstimativas();
                        for (int i = 0; i < estimativas1.size(); i++) {
                            Estimativa estimativa = estimativas1.get(i);
                            if (estimativa.isFavorito()) {
                                favoritos.add(estimativa);
                            } else {
                                comuns.add(estimativa);
                            }
                            
                        }
                        
                        List<Estimativa> comHeaders = new ArrayList<>();
                        comHeaders.add(linhasFavoritas);
                        comHeaders.addAll(favoritos);
                        comHeaders.add(linhasComuns);
                        comHeaders.addAll(comuns);
                        
                        retornoLinhasPonto.setEstimativas(comHeaders);
                        
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