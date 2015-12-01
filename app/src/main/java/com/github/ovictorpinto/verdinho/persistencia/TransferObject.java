package com.github.ovictorpinto.verdinho.persistencia;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class TransferObject implements Serializable {

	public abstract String getTableName();

	public abstract ContentValues getMapping();

	public abstract void fill(Cursor cursor);

	public abstract String getId();

	public abstract String getColumnId();

	public String getColumnOrder() {
		return null;
	}
}
