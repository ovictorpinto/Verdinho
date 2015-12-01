package com.github.ovictorpinto.verdinho.persistencia.dao;

import android.content.Context;

import com.github.ovictorpinto.verdinho.persistencia.DAO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;
import com.github.ovictorpinto.verdinho.util.LogHelper;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class PontoDAO extends DAO<PontoPO> {

    private static final String TAG = "PontoDAO";

    public PontoDAO(Context context) {
        super(context, PontoPO.class);
    }

    public List<PontoPO> findByRegiao(LatLngBounds latLngBounds) {
        StringBuilder where = new StringBuilder();
        where.append(PontoPO.Mapeamento.LATITUDE).append(" between ? and ? and ");
        where.append(PontoPO.Mapeamento.LONGITUDE).append(" between ? and ? ");
        LogHelper.log(TAG, where.toString());
        LogHelper.log(TAG, String.valueOf(latLngBounds.northeast.latitude));
        LogHelper.log(TAG, String.valueOf(latLngBounds.southwest.latitude));
        LogHelper.log(TAG, String.valueOf(latLngBounds.southwest.longitude));
        LogHelper.log(TAG, String.valueOf(latLngBounds.northeast.longitude));
        return findBy(where.toString(), String.valueOf(latLngBounds.southwest.latitude), String
                        .valueOf(latLngBounds.northeast.latitude), String.valueOf(latLngBounds.southwest.longitude), String
                        .valueOf(latLngBounds.northeast.longitude));
    }
}
