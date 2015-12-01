package com.github.ovictorpinto.verdinho.retorno;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ovictorpinto.verdinho.to.LinhaTO;

import java.util.List;

public class RetornoListarLinhas {

        @JsonProperty("itinerarios")
        private List<LinhaTO> linhas;

        public List<LinhaTO> getLinhas() {
            return linhas;
        }

        public void setLinhas(List<LinhaTO> linhas) {
            this.linhas = linhas;
        }
    }