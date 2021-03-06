package br.com.tecnologia.verdinho.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by victorpinto on 18/10/15.
 */
public class PontoTO implements Serializable {

	@JsonProperty("id")
	private Integer idPonto;

	private String identificador;
	private String descricao;
	private String logradouro;
	private String municipio;
	private Double longitude;
	private Double latitude;
	private Integer direcao;
	private boolean notificacao;

	public Integer getIdPonto() {
		return idPonto;
	}

	public void setIdPonto(Integer idPonto) {
		this.idPonto = idPonto;
	}

	public String getIdentificador() {
		return identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getDirecao() {
		return direcao;
	}

	public void setDirecao(Integer direcao) {
		this.direcao = direcao;
	}

	public boolean getNotificacao() {
		return notificacao;
	}

	public void setNotificacao(boolean notificacao) {
		this.notificacao = notificacao;
	}
}
