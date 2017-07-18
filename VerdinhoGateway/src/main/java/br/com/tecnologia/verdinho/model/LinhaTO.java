package br.com.tecnologia.verdinho.model;

import java.io.Serializable;

/**
 * Created by victorpinto on 24/10/15.
 */
public class LinhaTO implements Serializable {

	private Integer id;
	private String identificadorLinha;
	private String bandeira;
	private String complemento;
	private Integer linhaId;
	private String descricaoLinha;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getIdentificadorLinha() {
		return identificadorLinha;
	}

	public void setIdentificadorLinha(String identificadorLinha) {
		this.identificadorLinha = identificadorLinha;
	}

	public String getBandeira() {
		return bandeira;
	}

	public void setBandeira(String bandeira) {
		this.bandeira = bandeira;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public Integer getLinhaId() {
		return linhaId;
	}

	public void setLinhaId(Integer linhaId) {
		this.linhaId = linhaId;
	}

	public String getDescricaoLinha() {
		return descricaoLinha;
	}

	public void setDescricaoLinha(String descricaoLinha) {
		this.descricaoLinha = descricaoLinha;
	}
}
