package com.github.ovictorpinto.verdinho.retorno;

import com.github.ovictorpinto.verdinho.to.Estimativa;

import java.util.List;

public class RetornoLinhasPonto {
        private long horarioDoServidor;
        private int pontoDeOrigemId;
        private List<Estimativa> estimativas;

        public long getHorarioDoServidor() {
            return horarioDoServidor;
        }

        public void setHorarioDoServidor(long horarioDoServidor) {
            this.horarioDoServidor = horarioDoServidor;
        }

        public int getPontoDeOrigemId() {
            return pontoDeOrigemId;
        }

        public void setPontoDeOrigemId(int pontoDeOrigemId) {
            this.pontoDeOrigemId = pontoDeOrigemId;
        }

        public List<Estimativa> getEstimativas() {
            return estimativas;
        }

        public void setEstimativas(List<Estimativa> estimativas) {
            this.estimativas = estimativas;
        }
    }