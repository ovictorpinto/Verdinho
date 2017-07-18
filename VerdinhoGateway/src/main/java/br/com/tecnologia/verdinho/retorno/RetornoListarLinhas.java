package br.com.tecnologia.verdinho.retorno;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.tecnologia.verdinho.model.LinhaTO;

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