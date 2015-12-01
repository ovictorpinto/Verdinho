package com.github.ovictorpinto.verdinho.retorno;

import com.github.ovictorpinto.verdinho.to.PontoTO;

import java.util.List;

public class RetornoDetalharPontos {
    private List<PontoTO> pontosDeParada;

    public List<PontoTO> getPontosDeParada() {
        return pontosDeParada;
    }

    public void setPontosDeParada(List<PontoTO> pontosDeParada) {
        this.pontosDeParada = pontosDeParada;
    }
}