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
	public static final String NAME_CHANNEL = "NameChannel";
	public static final String URI = "Uri";
	public static final String THUMNAIL = "Thumnail";
	public static final String DESCRIBES = "Describes";

	@Override
	public void onCreate(SQLiteDatabase db) {

		String CreateTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
				+ ID_CHANNEL + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ NAME_CHANNEL + " TEXT," + URI + " TEXT," + THUMNAIL + " TEXT"
				+ DESCRIBES + " TEXT" + ")";
		db.execSQL(CreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		super.onUpgrade(db, oldVersion, newVersion);
	}

	public ChannelDao(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public List<Channel> getListChannel() {
		List<Channel> listDto = new ArrayList<Channel>();
		String sql = "Select * from Channel";
		try {
			SQLiteDatabase db = getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);

			if (cursor.moveToFirst()) {
				do {
					Channel dto = new Channel();
					dto.setIdChannel(cursor.getInt(0));
					dto.setNameChannel(cursor.getString(1));
					dto.setUri(cursor.getString(2));
					dto.setThumnail(cursor.getString(3));
					dto.setDescribes(cursor.getString(4));

					// Adding obj to list
					listDto.add(dto);
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

	public long insertChannel(Channel dto) {
		long flag = 0;
		// SQLiteDatabase db = this.getWritableDatabase();
		//
		// ContentValues values = new ContentValues();
		// values.put(KEY_TITLE, site.getTitle()); // site title
		// values.put(KEY_LINK, site.getLink()); // site url
		// values.put(KEY_RSS_LINK, site.getRssLink()); // rss link url
		// values.put(KEY_ICON_LINK, site.getIconLink()); // icon link url
		// values.put(KEY_DESCRIPTION, site.getDescription()); // site
		// description
		//
		// // Check if row already existed in database
		// if (!isSiteExists(db, site.getRssLink())) {
		// // site not existed, create a new row
		// long id = db.insert(TABLE_RSS, null, values);
		// if (id != 0) {
		// flag = id;
		// }
		// site.setId(id);
		//
		// db.close();
		// } else {
		// // site already existed update the row
		// // updateSite(dto);
		// // db.close();
		// }
		return flag;
	}
}
