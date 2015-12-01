package com.github.ovictorpinto.verdinho.persistencia.po;

import android.content.ContentValues;
import android.database.Cursor;

import com.github.ovictorpinto.verdinho.persistencia.TransferObject;
import com.github.ovictorpinto.verdinho.to.PontoTO;

@SuppressWarnings("serial")
public class PontoFavoritoPO extends TransferObject {

    private PontoTO pontoTO;

    public interface Mapeamento {

        String TABLE = "ponto_favorito";
        String ID = "id_linha";

        String CREATE = String.format("create table %s (%s integer primary key);", TABLE, ID);
    }

    public PontoFavoritoPO() {
        super();
    }

    public PontoFavoritoPO(PontoTO pontoTO) {
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
        return values;
    }

    @Override
    public void fill(Cursor cursor) {
        pontoTO = new PontoTO();
        pontoTO.setIdPonto(cursor.getInt(cursor.getColumnIndex(Mapeamento.ID)));
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
        return Mapeamento.ID + " desc";
    }

}
