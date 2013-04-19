package com.fsotv.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "FsoTV";
	private static final int DATABASE_VERSION = 8;

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createChannel = "CREATE TABLE " + ChannelDao.TABLE_NAME + "("
				+ ChannelDao.ID_CHANNEL + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ChannelDao.NAME_CHANNEL + " TEXT," + ChannelDao.URI
				+ " TEXT," + ChannelDao.THUMNAIL + " TEXT,"
				+ ChannelDao.DESCRIBES + " TEXT," + ChannelDao.ID_REAL_CHANNEL
				+ " TEXT," + ChannelDao.COMMENT_COUNT + " INTEGER,"
				+ ChannelDao.VIDEO_COUNT + " INTEGER," + ChannelDao.VIEW_COUNT
				+ " INTEGER," + ChannelDao.UPDATED + " TEXT)";
		String createVideo = "CREATE TABLE " + VideoDao.TABLE_NAME + "("
				+ VideoDao.ID_VIDEO + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ VideoDao.ID_CATEGORY + " INTEGER," + VideoDao.NAME_VIDEO
				+ " TEXT," + VideoDao.URI + " TEXT," + VideoDao.THUMNAIL
				+ " TEXT," + VideoDao.DESCRIBES + " TEXT," + VideoDao.ACCOUNT
				+ " TEXT," + VideoDao.TYPE_VIDEO + " INTEGER,"
				+ VideoDao.ID_REAL_VIDEO + " TEXT," + VideoDao.DURATION
				+ " INTEGER," + VideoDao.VIEW_COUNT + " INTEGER,"
				+ VideoDao.FAVORITE_COUNT + " INTEGER," + VideoDao.PUBLISHED
				+ " TEXT," + VideoDao.UPDATED + " TEXT)";
		String createReference = "CREATE TABLE " + ReferenceDao.TABLE_NAME
				+ "(" + ReferenceDao.KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + ReferenceDao.KEY_KEY
				+ " VARCHAR(255)," + ReferenceDao.KEY_VALUE + " TEXT,"
				+ ReferenceDao.KEY_EXTRAS + " TEXT," + ReferenceDao.KEY_DISPLAY
				+ " TEXT)";

		db.execSQL(createChannel);
		db.execSQL(createVideo);
		db.execSQL(createReference);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + ChannelDao.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + VideoDao.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ReferenceDao.TABLE_NAME);

		// Create tables again
		onCreate(db);
	}
}
