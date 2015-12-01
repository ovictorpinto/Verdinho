package com.github.ovictorpinto.verdinho.persistencia;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public abstract class DAO<T extends TransferObject> {

	protected final DatabaseHelper databaseHelper;
	private final T exemplo;
	protected Context context;
	private final Class<T> clazz;

	public DAO(Context context, Class<T> clazz) {
		this.context = context;
		this.clazz = clazz;
		databaseHelper = new DatabaseHelper(context);
		try {
			exemplo = clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public long create(T object) {
        long retorno = -1;
        SQLiteDatabase db = null;
        try {
            db = this.databaseHelper.getWritableDatabase();
            retorno = db.insert(object.getTableName(), null, object.getMapping());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return retorno;
    }

	public void update(T object) {
		SQLiteDatabase db = null;
		try {
			db = this.databaseHelper.getWritableDatabase();
			db.update(exemplo.getTableName(), object.getMapping(), object.getColumnId() + "=?", new String[] { object.getId() });
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void remove(T object) {
		SQLiteDatabase db = null;
		try {
			if (object.getId() != null) {
				db = this.databaseHelper.getWritableDatabase();
				db.delete(exemplo.getTableName(), object.getColumnId() + "=?", new String[] { object.getId() });
			}
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void removeAll() {
		removeBy(null, null);
	}

	protected void removeBy(String where, String[] params) {
		SQLiteDatabase db = null;
		try {
			db = this.databaseHelper.getWritableDatabase();
			db.delete(exemplo.getTableName(), where, params);
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public List<T> findAll() {
		return findBy(null, null);
	}

	protected List<T> findBy(String where, String... params) {
		List<T> resultado = new ArrayList<T>();
		SQLiteDatabase db = null;

		try {
			db = this.databaseHelper.getReadableDatabase();
			Cursor cursor = db.query(exemplo.getTableName(), null, where, params, null, null, exemplo.getColumnOrder());
			while (cursor.moveToNext()) {
				T novo = clazz.newInstance();
				novo.fill(cursor);
				resultado.add(novo);
			}
			if (cursor != null) {
				cursor.close();
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return resultado;
	}

	public T findByPK(String id) {
		SQLiteDatabase db = null;

		try {
			T resultado = null;
			T exemplo = clazz.newInstance();
			db = this.databaseHelper.getReadableDatabase();
			Cursor cursor = db.query(exemplo.getTableName(), null, exemplo.getColumnId() + "=?", new String[] { id }, null, null, null,
					null);
			if (cursor.moveToNext()) {
				resultado = clazz.newInstance();
				resultado.fill(cursor);
			}
			if (cursor != null) {
				cursor.close();
			}
			return resultado;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void removeByPK(T item) {
		SQLiteDatabase db = null;

		try {
			db = this.databaseHelper.getWritableDatabase();
			db.delete(exemplo.getTableName(), item.getColumnId() + "=?", new String[] { item.getId() });
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	protected T findFirst(String where, String... params) {
		List<T> lista = findBy(where, params);
		if (!lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}
}
