package com.fsotv.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fsotv.dto.Reference;

public class ReferenceDao {

	public static final String KEY_YOUTUBE_CATEGORY = "youtube_category";
	public static final String KEY_YOUTUBE_USERTYPE = "youtube_usertype";
	public static final String EXTRAS_CATEGORY_SELECT = "select";

	public static final String TABLE_NAME = "Reference";
	public static final String KEY_ID = "ID";
	public static final String KEY_KEY = "Key";
	public static final String KEY_VALUE = "Value";
	public static final String KEY_EXTRAS = "Extras";
	public static final String KEY_DISPLAY = "Display";

	private SQLiteHelper sqLiteHelper;

	public ReferenceDao(Context context) {
		sqLiteHelper = new SQLiteHelper(context);
		// Insert inital data
		int count = countReferences();
		if (count == 0) {
			initData();
		}
	}

	public void initData() {
		// Create category
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Comedy", "Comedy", EXTRAS_CATEGORY_SELECT));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Music", "Music", EXTRAS_CATEGORY_SELECT));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"News", "News", EXTRAS_CATEGORY_SELECT));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Autos", "Autos", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Education", "Education", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Entertainment", "Entertaiment", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Film", "Film", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Howto", "Howto", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"People", "People", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Animals", "Animals", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Tech", "Technical", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Sports", "Sports", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
				"Travel", "Travel", ""));

		// Create user type
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Comedians", "Comedians", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Directors", "Directors", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Gurus", "Gurus", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Musicians", "Musicians", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Non-profit", "Non-profit", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Partners", "Partners", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Politicians", "Politicians", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Reporters", "Reporters", ""));
		insertReference(new Reference(-1, ReferenceDao.KEY_YOUTUBE_USERTYPE,
				"Sponsors", "Sponsors", ""));
	}

	public List<Reference> getListReference() {
		List<Reference> listDto = getListReference(null, null);
		return listDto;
	}

	public List<Reference> getListReference(String key, String extras) {
		List<Reference> listDto = new ArrayList<Reference>();
		StringBuilder sb = new StringBuilder();
		sb.append("Select * from ");
		sb.append(TABLE_NAME);
		sb.append(" where 1=1");
		if (key != null && !key.isEmpty()) {
			sb.append(" and ");
			sb.append(KEY_KEY);
			sb.append(" = '");
			sb.append(key);
			sb.append("'");
		}
		if (extras != null && !extras.isEmpty()) {
			sb.append(" and ");
			sb.append(KEY_EXTRAS);
			sb.append(" = '");
			sb.append(extras);
			sb.append("'");
		}
		String sql = sb.toString();
		try {
			SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				do {
					Reference reference = new Reference();
					reference.setId(cursor.getInt(0));
					reference.setKey(cursor.getString(1));
					reference.setValue(cursor.getString(2));
					reference.setExtras(cursor.getString(3));
					reference.setDisplay(cursor.getString(4));
					// Adding obj to list
					listDto.add(reference);
				} while (cursor.moveToNext());
			}
			cursor.close();
			db.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
		return listDto;
	}

	public void insertReference(Reference reference) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_KEY, reference.getKey());
		values.put(KEY_VALUE, reference.getValue());
		values.put(KEY_EXTRAS, reference.getExtras());
		values.put(KEY_DISPLAY, reference.getDisplay());
		int id = (int) db.insert(TABLE_NAME, null, values);
		reference.setId(id);

		db.close();
	}

	/**
	 * Updating a single row row will be identified by id
	 * */
	public int updateReference(Reference reference) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_KEY, reference.getKey());
		values.put(KEY_VALUE, reference.getValue());
		values.put(KEY_EXTRAS, reference.getExtras());
		values.put(KEY_DISPLAY, reference.getDisplay());
		// updating row return
		int update = db.update(TABLE_NAME, values, KEY_ID + " = ?",
				new String[] { String.valueOf(reference.getId()) });
		db.close();
		return update;

	}

	/**
	 * Reading a row is identified by row id
	 * */
	public Reference getReference(int id) {
		Reference reference = new Reference();
		SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { KEY_ID, KEY_KEY,
				KEY_VALUE, KEY_EXTRAS, KEY_DISPLAY }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			reference.setId(cursor.getInt(0));
			reference.setKey(cursor.getString(1));
			reference.setValue(cursor.getString(2));
			reference.setExtras(cursor.getString(3));
			reference.setDisplay(cursor.getString(4));
		}
		cursor.close();
		db.close();
		return reference;
	}

	/**
	 * Deleting single row
	 * */
	public void deleteReference(int id) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	/**
	 * Count site
	 * */
	public int countReferences() {
		SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME,
				new String[] {});
		return cursor.getCount();
	}
}
