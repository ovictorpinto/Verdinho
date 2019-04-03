package br.com.tecnologia.verdinho.model;

import java.io.Serializable;

/**
 * Created by victorpinto on 24/10/15.
 */
public class Estimativa implements Serializable {

    private String veiculo;
    private Boolean acessibilidade;
    private Boolean accessibility;
    private Integer itinerarioId;
    private long horarioDePartida;
    private String horarioNaOrigem;
    private String horarioNoDestino;
    private long horarioDaTransmissao;
    private long previsaoNaOrigemEmMinutos;
    private long previsaoNoDestinoEmMinutos;

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

    public long getHorarioDaTransmissao() {
        return horarioDaTransmissao;
    }

    public void setHorarioDaTransmissao(long horarioDaTransmissao) {
        this.horarioDaTransmissao = horarioDaTransmissao;
    }

    public String getHorarioNaOrigem() {
        return horarioNaOrigem;
    }

    public void setHorarioNaOrigem(String horarioNaOrigem) {
        this.horarioNaOrigem = horarioNaOrigem;
    }

    public long getPrevisaoNaOrigemEmMinutos() {
        return previsaoNaOrigemEmMinutos;
    }

    public void setPrevisaoNaOrigemEmMinutos(long previsaoNaOrigemEmMinutos) {
        this.previsaoNaOrigemEmMinutos = previsaoNaOrigemEmMinutos;
    }

    public String getHorarioNoDestino() {
        return horarioNoDestino;
    }

    public void setHorarioNoDestino(String horarioNoDestino) {
        this.horarioNoDestino = horarioNoDestino;
    }

    public long getPrevisaoNoDestinoEmMinutos() {
        return previsaoNoDestinoEmMinutos;
    }

    public void setPrevisaoNoDestinoEmMinutos(long previsaoNoDestinoEmMinutos) {
        this.previsaoNoDestinoEmMinutos = previsaoNoDestinoEmMinutos;
    }

    public Boolean getAccessibility() {
        return accessibility;
    }

    public void setAccessibility(Boolean accessibility) {
        this.accessibility = accessibility;
    }
}
