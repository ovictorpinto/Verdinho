package br.com.tecnologia.verdinho.retorno;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class RetornoDetalharPontos {

	private List<JsonNode> pontosDeParada;

	public List<JsonNode> getPontosDeParada() {
		return pontosDeParada;
	}

	public void setPontosDeParada(List<JsonNode> pontosDeParada) {
		this.pontosDeParada = pontosDeParada;
	}

}