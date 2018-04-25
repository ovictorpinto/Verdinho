package com.github.ovictorpinto.verdinho.persistencia.po;

import android.content.ContentValues;
import android.database.Cursor;

import com.github.ovictorpinto.verdinho.persistencia.TransferObject;
import com.github.ovictorpinto.verdinho.to.PontoTO;

@SuppressWarnings("serial")
public class PontoPO extends TransferObject {
    
    private PontoTO pontoTO;
    
    public interface Mapeamento {
        
        String TABLE = "ponto";
        String ID = "id_ponto";
        String IDENTIFICADOR = "identificador";
        String LOGRADOURO = "logradouro";
        String DESCRICAO = "descricao";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String MUNICIPIO = "municipio";
        String DIRECAO = "direcao";
        
        String NOTIFICACAO = "notificacao";
        String APELIDO = "apelido";
        
        String CREATE = String
                .format("create table %s (%s integer primary key, %s text, %s text, %s text, %s number, %s number, %s text, %s " +
                        "number);", TABLE, ID, IDENTIFICADOR, LOGRADOURO, DESCRICAO, LATITUDE, LONGITUDE, MUNICIPIO, DIRECAO);
        
        String ADD_NOTIFICACAO = String.format("alter table %s add column %s number default 0;", TABLE, NOTIFICACAO);
        String ADD_APELIDO = String.format("alter table %s add column %s text;", TABLE, APELIDO);
    }
    
    public PontoPO() {
        super();
    }
    
    public PontoPO(PontoTO pontoTO) {
        super();
        this.pontoTO = pontoTO;
    }
    
    @Override
    public String getTableName() {
        return Mapeamento.TABLE;
    }
    
    @Override
    public ContentValues getMapping() {
        ContentValues values = new ContentValues();
        values.put(Mapeamento.ID, pontoTO.getIdPonto());
        values.put(Mapeamento.IDENTIFICADOR, pontoTO.getIdentificador());
        values.put(Mapeamento.LOGRADOURO, pontoTO.getLogradouro());
        values.put(Mapeamento.DESCRICAO, pontoTO.getDescricao());
        values.put(Mapeamento.LATITUDE, pontoTO.getLatitude());
        values.put(Mapeamento.LONGITUDE, pontoTO.getLongitude());
        values.put(Mapeamento.MUNICIPIO, pontoTO.getMunicipio());
        values.put(Mapeamento.DIRECAO, pontoTO.getDirecao());
        values.put(Mapeamento.NOTIFICACAO, pontoTO.getNotificacao() ? 1 : 0);
        values.put(Mapeamento.APELIDO, pontoTO.getApelido());
        return values;
    }
    
    @Override
    public void fill(Cursor cursor) {
        pontoTO = new PontoTO();
        pontoTO.setIdPonto(cursor.getInt(cursor.getColumnIndex(Mapeamento.ID)));
        pontoTO.setIdentificador(cursor.getString(cursor.getColumnIndex(Mapeamento.IDENTIFICADOR)));
        pontoTO.setLogradouro(cursor.getString(cursor.getColumnIndex(Mapeamento.LOGRADOURO)));
        pontoTO.setDescricao(cursor.getString(cursor.getColumnIndex(Mapeamento.DESCRICAO)));
        pontoTO.setLatitude(cursor.getDouble(cursor.getColumnIndex(Mapeamento.LATITUDE)));
        pontoTO.setLongitude(cursor.getDouble(cursor.getColumnIndex(Mapeamento.LONGITUDE)));
        pontoTO.setMunicipio(cursor.getString(cursor.getColumnIndex(Mapeamento.MUNICIPIO)));
        pontoTO.setDirecao(cursor.getInt(cursor.getColumnIndex(Mapeamento.DIRECAO)));
        int columnIndex = cursor.getColumnIndex(Mapeamento.NOTIFICACAO);
        if (!cursor.isNull(columnIndex)) {
            pontoTO.setNotificacao(cursor.getInt(columnIndex) > 0);
        }
        pontoTO.setApelido(cursor.getString(cursor.getColumnIndex(Mapeamento.APELIDO)));
    }
    
    @Override
    public String getId() {
        return pontoTO.getIdPonto().toString();
    }
    
    @Override
    public String getColumnId() {
        return Mapeamento.ID;
    }
    
    @Override
    public String getColumnOrder() {
        return Mapeamento.APELIDO + ", " + Mapeamento.IDENTIFICADOR;
    }
    
    public PontoTO getPontoTO() {
        return pontoTO;
    }
}
