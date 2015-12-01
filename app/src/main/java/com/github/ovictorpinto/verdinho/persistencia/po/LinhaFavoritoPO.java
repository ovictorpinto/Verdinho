package com.github.ovictorpinto.verdinho.persistencia.po;

import android.content.ContentValues;
import android.database.Cursor;

import com.github.ovictorpinto.verdinho.persistencia.TransferObject;
import com.github.ovictorpinto.verdinho.to.LinhaTO;

@SuppressWarnings("serial")
public class LinhaFavoritoPO extends TransferObject {

    private String identificadorLinha;

    public interface Mapeamento {

        String TABLE = "linha_favorito";
        String ID = "id_linha";

        String CREATE = String.format("create table %s (%s text primary key);", TABLE, ID);
    }

    public LinhaFavoritoPO() {
        super();
    }

    public LinhaFavoritoPO(LinhaTO linhaTO) {
        super();
        this.identificadorLinha = linhaTO.getIdentificadorLinha();
    }

    @Override
    public String getTableName() {
        return Mapeamento.TABLE;
    }

    @Override
    public ContentValues getMapping() {
        ContentValues values = new ContentValues();
        values.put(Mapeamento.ID, identificadorLinha);
        return values;
    }

    @Override
    public void fill(Cursor cursor) {
        identificadorLinha = cursor.getString(cursor.getColumnIndex(Mapeamento.ID));
    }

    @Override
    public String getId() {
        return identificadorLinha;
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
