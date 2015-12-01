package com.github.ovictorpinto.verdinho.persistencia.dao;

import android.content.Context;

import com.github.ovictorpinto.verdinho.persistencia.DAO;
import com.github.ovictorpinto.verdinho.persistencia.po.LinhaFavoritoPO;

public class LinhaFavoritoDAO extends DAO<LinhaFavoritoPO> {

    private static final String TAG = "PontoDAO";

    public LinhaFavoritoDAO(Context context) {
        super(context, LinhaFavoritoPO.class);
    }

}
