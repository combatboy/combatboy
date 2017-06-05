package org.zywx.wbpalmstar.plugin.uexappmarket.down;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UpdateDbHelper extends SQLiteOpenHelper {

	public static String DB_NAME = "Downloader.db";
	private final static String TABLE_NAME = "UpdateDownloader";

	public UpdateDbHelper(Context context) {
		super(context, DB_NAME, null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String F_CREATETABLE_SQL = "CREATE TABLE IF  NOT EXISTS "
				+ TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY,url TEXT,filePath TEXT,fileSize TEXT,downSize TEXT,time TEXT)";
		db.execSQL(F_CREATETABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
