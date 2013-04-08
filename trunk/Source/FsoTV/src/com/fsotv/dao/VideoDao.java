package com.fsotv.dao;

import java.util.ArrayList;
import java.util.List;
import com.fsotv.dto.Video;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VideoDao{
	public static final String TABLE_NAME = "Video";
	public static final String ID_VIDEO = "IdVideo";
	public static final String ID_CATEGORY = "IdCategory";
	public static final String NAME_VIDEO = "NameVideo";
	public static final String URI = "Uri";
	public static final String THUMNAIL = "Thumnail";
	public static final String DESCRIBES = "Describes";
	public static final String ACCOUNT = "Account";
	public static final String TYPE_VIDEO = "typeVideo";
	public static final String ID_REAL_VIDEO = "IdRealVideo";
	
	private SQLiteHelper sqLiteHelper;
	
	public VideoDao(Context context) {
		sqLiteHelper = new SQLiteHelper(context);
	}

	public List<Video> getListVideo() {
		List<Video> listDto = new ArrayList<Video>();
		String sql = "Select * from " + TABLE_NAME;
		try {
			SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				do {
					Video video = new Video();
					video.setIdVideo(cursor.getInt(0));
					video.setIdCategory(cursor.getInt(1));
					video.setNameVideo(cursor.getString(2));
					video.setUri(cursor.getString(3));
					video.setThumnail(cursor.getString(4));
					video.setDescribes(cursor.getString(5));
					video.setAccount(cursor.getString(6));
					video.setTypeVideo(cursor.getInt(7));
					video.setIdRealVideo(cursor.getString(8));
					// Adding obj to list
					listDto.add(video);
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

	public void insertVideo(Video video) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ID_CATEGORY, video.getIdCategory());
		values.put(NAME_VIDEO, video.getNameVideo());
		values.put(URI, video.getUri());
		values.put(THUMNAIL, video.getThumnail());
		values.put(DESCRIBES, video.getDescribes());
		values.put(ACCOUNT, video.getAccount());
		values.put(TYPE_VIDEO, video.getTypeVideo());
		values.put(ID_REAL_VIDEO, video.getIdRealVideo());
		// Check if row already existed in database
		int idExist = isVideoExists(db, video.getIdRealVideo());
		if (idExist == 0) {
			int id = (int) db.insert(TABLE_NAME, null, values);
			video.setIdVideo(id);
		} else {
			video.setIdVideo(idExist);
			updateVideo(video);
		}
		db.close();
	}

	/**
	 * Updating a single row row will be identified by id
	 * */
	public int updateVideo(Video video) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(ID_CATEGORY, video.getIdCategory());
		values.put(NAME_VIDEO, video.getNameVideo());
		values.put(URI, video.getUri());
		values.put(THUMNAIL, video.getThumnail());
		values.put(DESCRIBES, video.getDescribes());
		values.put(ACCOUNT, video.getAccount());
		values.put(TYPE_VIDEO, video.getTypeVideo());
		values.put(ID_REAL_VIDEO, video.getIdRealVideo());
		// updating row return
		int update = db.update(TABLE_NAME, values, ID_CATEGORY + " = ?",
				new String[] { String.valueOf(video.getIdVideo()) });
		db.close();
		return update;

	}

	/**
	 * Reading a row is identified by row id
	 * */
	public Video getVideo(int id) {
		Video video = new Video();
		SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { ID_CATEGORY,
				NAME_VIDEO, URI, DESCRIBES, THUMNAIL, ACCOUNT, TYPE_VIDEO, ID_REAL_VIDEO }, ID_VIDEO + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			video.setIdVideo(cursor.getInt(0));
			video.setIdCategory(cursor.getInt(1));
			video.setNameVideo(cursor.getString(2));
			video.setUri(cursor.getString(3));
			video.setThumnail(cursor.getString(4));
			video.setDescribes(cursor.getString(5));
			video.setAccount(cursor.getString(6));
			video.setTypeVideo(cursor.getInt(7));
			video.setIdRealVideo(cursor.getString(8));
		}
		cursor.close();
		db.close();
		return video;
	}

	/**
	 * Deleting single row
	 * */
	public void deleteVideo(int id) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
		db.delete(TABLE_NAME, ID_VIDEO + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	/**
	 * Checking whether a video is already existed, check is done by matching
	 * read id
	 * */
	public int isVideoExists(SQLiteDatabase db, String realId) {
		Cursor cursor = db.query(TABLE_NAME, new String[] { ID_VIDEO }, ID_REAL_VIDEO
				+ "='" + realId + "'", null, null, null, null, null);
		int id = 0;
		if (cursor != null && cursor.moveToFirst()) {
			id = cursor.getInt(0);
		}

		return id;
	}
}
