package com.github.ovictorpinto.verdinho.to;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ovictorpinto.verdinho.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

import br.com.tcsistemas.common.string.StringHelper;

/**
 * Created by victorpinto on 18/10/15.
 */
public class PontoTO implements Serializable, ClusterItem {
    
    public static final String PARAM = "paramPontoTO";
    public static final String PARAM_ID = "paramIdPonto";
    
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
    private String apelido;
    
    public String getNomeApresentacao(Context context){
        return StringHelper.coalesce(apelido, context.getString(R.string.ponto_n_, getIdentificador()));
    }
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
    
    @Override
    public String getTitle() {
        return descricao;
    }
    
    @Override
    public String getSnippet() {
        return descricao;
    }
    
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
    
    public String getApelido() {
        return apelido;
    }
    
    public void setApelido(String apelido) {
        this.apelido = apelido;
    }
}
