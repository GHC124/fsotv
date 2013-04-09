package com.fsotv.dao;

import java.util.ArrayList;
import java.util.List;
import com.fsotv.dto.Channel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ChannelDao{
	public static final String TABLE_NAME = "Channel";
	public static final String ID_CHANNEL = "IdChannel";
	public static final String ID_REAL_CHANNEL = "IdRealChannel";
	public static final String NAME_CHANNEL = "NameChannel";
	public static final String URI = "Uri";
	public static final String THUMNAIL = "Thumnail";
	public static final String DESCRIBES = "Describes";
	public static final String COMMENT_COUNT = "commentCount";
	public static final String VIDEO_COUNT = "videoCount";
	public static final String VIEW_COUNT = "viewVount";
	
	private SQLiteHelper sqLiteHelper;
	
	public ChannelDao(Context context) {
		sqLiteHelper = new SQLiteHelper(context);
	}

	public List<Channel> getListChannel() {
		List<Channel> listDto = new ArrayList<Channel>();
		String sql = "Select * from " + TABLE_NAME;
		try {
			SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
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
					channel.setCommentCount(cursor.getInt(6));
					channel.setVideoCount(cursor.getInt(7));
					channel.setViewCount(cursor.getInt(8));
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
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NAME_CHANNEL, channel.getNameChannel());
		values.put(URI, channel.getUri());
		values.put(THUMNAIL, channel.getThumnail());
		values.put(DESCRIBES, channel.getDescribes());
		values.put(ID_REAL_CHANNEL, channel.getIdRealChannel());
		values.put(COMMENT_COUNT, channel.getCommentCount());
		values.put(VIDEO_COUNT, channel.getVideoCount());
		values.put(VIEW_COUNT, channel.getViewCount());
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
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NAME_CHANNEL, channel.getNameChannel());
		values.put(URI, channel.getUri());
		values.put(THUMNAIL, channel.getThumnail());
		values.put(DESCRIBES, channel.getDescribes());
		values.put(ID_REAL_CHANNEL, channel.getIdRealChannel());
		values.put(COMMENT_COUNT, channel.getCommentCount());
		values.put(VIDEO_COUNT, channel.getVideoCount());
		values.put(VIEW_COUNT, channel.getViewCount());
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
		SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, new String[] { ID_CHANNEL,
				NAME_CHANNEL, URI, DESCRIBES, THUMNAIL, ID_REAL_CHANNEL, COMMENT_COUNT,
				VIDEO_COUNT, VIEW_COUNT}, ID_CHANNEL + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			channel.setIdChannel(cursor.getInt(0));
			channel.setNameChannel(cursor.getString(1));
			channel.setUri(cursor.getString(2));
			channel.setDescribes(cursor.getString(3));
			channel.setThumnail(cursor.getString(4));
			channel.setIdRealChannel(cursor.getString(5));
			channel.setCommentCount(cursor.getInt(6));
			channel.setVideoCount(cursor.getInt(7));
			channel.setViewCount(cursor.getInt(8));
		}
		cursor.close();
		db.close();
		return channel;
	}

	/**
	 * Deleting single row
	 * */
	public void deleteChannel(int id) {
		SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
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
