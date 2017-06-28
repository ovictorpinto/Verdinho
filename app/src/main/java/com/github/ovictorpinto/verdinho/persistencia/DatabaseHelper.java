package com.github.ovictorpinto.verdinho.persistencia;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.github.ovictorpinto.verdinho.persistencia.po.LinhaFavoritoPO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoFavoritoPO;
import com.github.ovictorpinto.verdinho.persistencia.po.PontoPO;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String NOME_BANCO = "com.github.ovictorpinto.verdinho.sqlite3";
    private static final int VERSAO = 2;
    private final Context context;
    
    public DatabaseHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
        this.context = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DATABASE", "Criando banco...");
        onUpgrade(db, 0, VERSAO);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        List<String> commandsList = new ArrayList<>();
        if (!existsTable(db, PontoPO.Mapeamento.TABLE)) {
            commandsList.add(PontoPO.Mapeamento.CREATE);
        }
        if (!existsTable(db, PontoFavoritoPO.Mapeamento.TABLE)) {
            commandsList.add(PontoFavoritoPO.Mapeamento.CREATE);
        }
        if (!existsTable(db, LinhaFavoritoPO.Mapeamento.TABLE)) {
            commandsList.add(LinhaFavoritoPO.Mapeamento.CREATE);
        }
        if (!existsColumnInTable(db, PontoPO.Mapeamento.TABLE, PontoPO.Mapeamento.NOTIFICACAO)) {
            commandsList.add(PontoPO.Mapeamento.ADD_NOTIFICACAO);
        }
        runScript(db, commandsList);
    }
    
    public void runScript(SQLiteDatabase db, List<String> lines) {
        try {
            String sqlAcumulado = "";
            for (String sql : lines) {
                sqlAcumulado += sql;
                
                if (sqlAcumulado.endsWith(";")) {//só executa quando encontra o ponto e vírgula
                    db.execSQL(sqlAcumulado);
                    sqlAcumulado = "";
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private boolean existsColumnInTable(SQLiteDatabase db, String inTable, String columnToCheck) {
        try {
            //query 1 row
            Cursor mCursor = db.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);
            
            //getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1) {
                return true;
            } else {
                return false;
            }
            
        } catch (Exception Exp) {
            //something went wrong. Missing the database? The table?
            return false;
        }
    }
    
    private boolean existsTable(SQLiteDatabase db, String inTable) {
        try {
            //query 1 row
            Cursor mCursor = db.rawQuery("SELECT * FROM " + inTable + " LIMIT 0", null);
            return true;
        } catch (Exception Exp) {
            //something went wrong. Missing the database? The table?
            return false;
        }
    }
    
    private boolean existsDataInTable(SQLiteDatabase db, String inTable) {
        try {
            //count
            Cursor mCursor = db.rawQuery("SELECT count(*) total FROM " + inTable + " LIMIT 1", null);
            
            //getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
            return mCursor.moveToNext();
            
        } catch (Exception Exp) {
            //something went wrong. Missing the database? The table?
            return false;
        }
    }
}
