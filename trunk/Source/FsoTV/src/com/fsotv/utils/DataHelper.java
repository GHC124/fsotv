package com.fsotv.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
	
	protected static final String DATABASE_NAME = "FsoTV";
	protected static final int DATABASE_VERSION = 2;
	
	public DataHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
