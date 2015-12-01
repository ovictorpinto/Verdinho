package com.github.ovictorpinto.verdinho.persistencia.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.ovictorpinto.verdinho.persistencia.DAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.to.PontoTO;

import java.util.ArrayList;
import java.util.List;

public class PontoFavoritoDAO extends DAO<PontoFavoritoPO> {

    private static final String TAG = "PontoFavoritoDAO";

    public PontoFavoritoDAO(Context context) {
        super(context, PontoFavoritoPO.class);
    }

    private final static String SQL_FIND_ALL;

    static {
        StringBuilder sql = new StringBuilder();
        sql.append(" select p.* ");
        sql.append(" from ").append(PontoPO.Mapeamento.TABLE).append(" p, ");
        sql.append(PontoFavoritoPO.Mapeamento.TABLE).append(" f ");
        sql.append(" where p.").append(PontoPO.Mapeamento.ID).append(" = ");
        sql.append(" f.").append(PontoFavoritoPO.Mapeamento.ID);
        SQL_FIND_ALL = sql.toString();
    }

    public List<PontoPO> findAllFavoritos() {

        List<PontoPO> resultado = new ArrayList<>();
        SQLiteDatabase db = null;

        try {
            db = this.databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(SQL_FIND_ALL, null);
            while (cursor.moveToNext()) {
                PontoPO novo = new PontoPO();
                novo.fill(cursor);
                resultado.add(novo);
            }
            cursor.close();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return resultado;
    }

    public void addFavorito(PontoTO pontoTO) {
        create(new PontoFavoritoPO(pontoTO));
    }

}
