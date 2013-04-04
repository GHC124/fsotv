package com.fsotv.dao;

import java.util.ArrayList;
import java.util.List;
import com.fsotv.dto.Channel;
import com.fsotv.utils.DataHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChannelDao extends DataHelper {

	public static final String TABLE_NAME = "Channel";
	public static final String ID_CHANNEL = "IdChannel";
	public static final String ID_REAL_CHANNEL = "IdRealChannel";
	public static final String NAME_CHANNEL = "NameChannel";
	public static final String URI = "Uri";
	public static final String THUMNAIL = "Thumnail";
	public static final String DESCRIBES = "Describes";

	public ChannelDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		String CreateTable = "CREATE TABLE " + TABLE_NAME + "("
				+ ID_CHANNEL + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ NAME_CHANNEL + " TEXT," + URI + " TEXT," + THUMNAIL
				+ " TEXT," + DESCRIBES + " TEXT," + ID_REAL_CHANNEL + " TEXT)";
		db.execSQL(CreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

		// Create tables again
		onCreate(db);
	}

	public List<Channel> getListChannel() {
		List<Channel> listDto = new ArrayList<Channel>();
		String sql = "Select * from Channel";
		try {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				do {
					Channel channel = new Channel();
					channel.setIdChannel(cursor.getInt(0));
					channel.setNameChannel(cursor.getString(1));
					channel.setUri(cursor.getString(2));
					channel.setThumnail(cursor.getString(3));
					channel.setDescribes(cursor.getString(4));
					channel.setIdRealChannel(cursor.getString(5));
					// Adding obj to list
					listDto.add(channel);
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

	public void insertChannel(Channel channel) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NAME_CHANNEL, channel.getNameChannel());
		values.put(URI, channel.getUri());
		values.put(THUMNAIL, channel.getThumnail());
		values.put(DESCRIBES, channel.getDescribes());
		values.put(ID_REAL_CHANNEL, channel.getIdRealChannel());
		// Check if row already existed in database
		int idExist = isChannelExists(db, channel.getIdRealChannel());
		if (idExist == 0) {
			int id = (int) db.insert(TABLE_NAME, null, values);
			channel.setIdChannel(id);
		} else {
			channel.setIdChannel(idExist);
			updateChannel(channel);
		}
		db.close();
	}

	/**
	 * Updating a single row row will be identified by id
	 * */
	public int updateChannel(Channel channel) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NAME_CHANNEL, channel.getNameChannel());
		values.put(URI, channel.getUri());
		values.put(THUMNAIL, channel.getThumnail());
		values.put(DESCRIBES, channel.getDescribes());
		values.put(ID_REAL_CHANNEL, channel.getIdRealChannel());
		// updating row return
		int update = db.update(TABLE_NAME, values, ID_CHANNEL + " = ?",
				new String[] { String.valueOf(channel.getIdChannel()) });
		db.close();
		return update;

	}

	/**
	 * Reading a row is identified by row id
	 * */
	public Channel getChannel(int id) {
		Channel channel = new Channel();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { ID_CHANNEL,
				NAME_CHANNEL, URI, DESCRIBES, THUMNAIL, ID_REAL_CHANNEL }, ID_CHANNEL + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			channel.setIdChannel(cursor.getInt(0));
			channel.setNameChannel(cursor.getString(1));
			channel.setUri(cursor.getString(2));
			channel.setDescribes(cursor.getString(3));
			channel.setThumnail(cursor.getString(4));
			channel.setIdRealChannel(cursor.getString(5));
		}
		cursor.close();
		db.close();
		return channel;
	}

	/**
	 * Deleting single row
	 * */
	public void deleteChannel(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, ID_CHANNEL + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	/**
	 * Checking whether a channel is already existed, check is done by matching
	 * read id
	 * */
	public int isChannelExists(SQLiteDatabase db, String realId) {
		Cursor cursor = db.query(TABLE_NAME, new String[] { ID_CHANNEL }, ID_REAL_CHANNEL
				+ "='" + realId + "'", null, null, null, null, null);
		int id = 0;
		if (cursor != null && cursor.moveToFirst()) {
			id = cursor.getInt(0);
		}

		return id;
	}
}
