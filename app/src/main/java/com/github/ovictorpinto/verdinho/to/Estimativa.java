package com.github.ovictorpinto.verdinho.to;

import android.content.Context;

import com.github.ovictorpinto.verdinho.R;

import java.io.Serializable;

import br.com.tcsistemas.common.date.HoraHelper;

/**
 * Created by victorpinto on 24/10/15.
 */
public class Estimativa implements Serializable {

    public static final String PARAM = "EstimativaParam";

    private Boolean favorito;
    private String veiculo;
    private Boolean acessibilidade;
    private Integer itinerarioId;
    private long horarioDePartida;
    private Long horarioNaOrigem;
    private long horarioDaTransmissao;

    public static final long CONFIABILIDADE_RUIM = 36 * HoraHelper.MINUTO_IN_MILI;
    public static final long CONFIABILIDADE_MEDIO = 22 * HoraHelper.MINUTO_IN_MILI;

    public int getBackgroundConfiabilidade(long horarioDoServidor) {
        long distanciaOrigem = horarioNaOrigem - horarioDaTransmissao;
        long distanciaAgora = horarioDoServidor - horarioDaTransmissao;
        long distancia = Math.max(distanciaOrigem, distanciaAgora);
        if (distancia > CONFIABILIDADE_RUIM) {
            return R.drawable.badge_ruim;
        } else if (distancia > CONFIABILIDADE_MEDIO) {
            return R.drawable.badge_medio;
        }
        return R.drawable.badge_ok;
    }

    public String getHorarioText(Context context, long horarioDoServidor) {
        String hora;
        long miliRestante = getHorarioNaOrigem() - horarioDoServidor;
        if (miliRestante < HoraHelper.MINUTO_IN_MILI) {
            hora = context.getString(R.string.meno_minuto);
        } else if (miliRestante < HoraHelper.HORA_IN_MILI) {
            int minutos = (int) (miliRestante / HoraHelper.MINUTO_IN_MILI);
            hora = context.getResources().getQuantityString(R.plurals.minutos, minutos, minutos);
        } else {
            long minutos = miliRestante / HoraHelper.MINUTO_IN_MILI;
            long horaRestante = minutos / 60;
            minutos %= 60L;
            hora = context.getString(R.string.mais_hora, horaRestante, minutos);
        }
        return hora;
    }

    public Boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(Boolean favorito) {
        this.favorito = favorito;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(String veiculo) {
        this.veiculo = veiculo;
    }

    public Boolean getAcessibilidade() {
        return acessibilidade;
    }

    public void setAcessibilidade(Boolean acessibilidade) {
        this.acessibilidade = acessibilidade;
    }

    public Integer getItinerarioId() {
        return itinerarioId;
    }

    public void setItinerarioId(Integer itinerarioId) {
        this.itinerarioId = itinerarioId;
    }

    public long getHorarioDePartida() {
        return horarioDePartida;
    }

    public void setHorarioDePartida(long horarioDePartida) {
        this.horarioDePartida = horarioDePartida;
    }

    public Long getHorarioNaOrigem() {
        return horarioNaOrigem;
    }

    public void setHorarioNaOrigem(Long horarioNaOrigem) {
        this.horarioNaOrigem = horarioNaOrigem;
    }

    public long getHorarioDaTransmissao() {
        return horarioDaTransmissao;
    }

    public void setHorarioDaTransmissao(long horarioDaTransmissao) {
        this.horarioDaTransmissao = horarioDaTransmissao;
    }
}
