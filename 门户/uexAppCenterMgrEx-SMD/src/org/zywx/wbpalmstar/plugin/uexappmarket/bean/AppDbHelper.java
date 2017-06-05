package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDbHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "appmarket.db";

	public static final String TABLE_NAME = "t_apps";
	public static final String TABLE_APP_LIST = "app_list";
	public static final String TABLE_UPDATE_INFO = "update_info";
	public static final String TABLE_AD = "ad";

	public static final String FIELD_ID = "_id";
	public static final String FIELD_STORE_ID = "store_id";
	public static final String FIELD_APP_NAME = "app_name";
	public static final String FIELD_ICON_LOC = "icon_url";
	public static final String FIELD_DOWNLOAD_URL = "download_url";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_APP_TYPE = "type";
	public static final String FIELD_INSTALL_PATH = "install_path";
	public static final String FIELD_FILE_PATH = "file_path";
	public static final String FIELD_APP_VER = "app_ver";
	public static final String FIELD_CERTIFICATES_PATH = "certificates_path";
	public static final String FIELD_CERTIFICATES_PWD = "certificates_pwd";
	public static final String FIELD_PACKAGENAME = "package_name";
	public static final String FIELD_CERTIFICATES_URL = "certificates_url";
	public static final String FIELD_APPID = "appId";
	public static final String FIELD_APP_ID = "app_id";
	public static final String FIELD_WGT_APPID = "wgtAppId";
	public static final String FIELD_MAIN_APP = "mainApp";
	public static final String FIELD_DEFAULT_APP = "default_app";
	public static final String FIELD_REMAIN_APP = "remain_app";
	public static final String FIELD_NEW_APP = "new_app";// 从剩余列表添加到首页为新，点击之后就不为新
	public static final String FIELD_APP_KEY = "app_key";
	public static final String FIELD_DEFAULT_VERSION = "def_ver";
	public static final String FIELD_MOFIFY_TIME = "modify_time";
	public static final String JK_APPLYDEFAULT_APP = "applyDefault";
	public static final String FIELD_SIZE = "pkgSize";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_POSITION = "position";
	public static final String FIELD_TIME = "up_time"; // 上架时间
	public static final String FIELD_UPDATE_TIME = "updateTime";
	public static final String FIELD_CREATE_TIME = "createTime";

	/**
	 * 广告数据
	 */
	public static final String FIELD_AD_IMG_URL = "img_url";
	public static final String FIELD_AD_LINK_URL = "link_url";
	public static final String FIELD_AD_IMG_PATH = "img_path";
	public static final String FIELD_AD_TITLE = "adv_title";

	/**
	 * 延续一期版本号递增，一期最后一个版本号为7
	 */
	public final static int DB_VERSION = 11;

	public static final String COMMA = ",";
	public static final String EQUAL_SIGN = "=";

	// public static AppDbHelper sInstance = null;
	//
	// public static AppDbHelper getInstance(Context context) {
	// synchronized (AppDbHelper.class) {
	// if (sInstance == null) {
	// sInstance = new AppDbHelper(context);
	// }
	// }
	//
	// return sInstance;
	// }

	public AppDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db, TABLE_APP_LIST);// 所有数据与
		createTable(db, TABLE_NAME);// 已安装的
		db.execSQL(CREATE_UPDATE);
		db.execSQL(CREATE_AD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		onCreate(db);

		addColumn(db, TABLE_APP_LIST, FIELD_WGT_APPID, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_DEFAULT_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_REMAIN_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_APP_KEY, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_NEW_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_DEFAULT_VERSION, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_MOFIFY_TIME, "INTEGER");
		addColumn(db, TABLE_APP_LIST, JK_APPLYDEFAULT_APP, "TEXT");

		addColumn(db, TABLE_NAME, FIELD_WGT_APPID, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_DEFAULT_APP, "INTEGER");
		addColumn(db, TABLE_NAME, FIELD_REMAIN_APP, "INTEGER");
		addColumn(db, TABLE_NAME, FIELD_APP_KEY, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_DEFAULT_VERSION, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_MOFIFY_TIME, "INTEGER");
		addColumn(db, TABLE_NAME, JK_APPLYDEFAULT_APP, "TEXT");

		addColumn(db, TABLE_AD, FIELD_AD_TITLE, "TEXT");
		addColumn(db, TABLE_AD, FIELD_UPDATE_TIME, "TEXT");
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		onCreate(db);

		addColumn(db, TABLE_APP_LIST, FIELD_WGT_APPID, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_DEFAULT_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_REMAIN_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_APP_KEY, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_NEW_APP, "INTEGER");
		addColumn(db, TABLE_APP_LIST, FIELD_DEFAULT_VERSION, "TEXT");
		addColumn(db, TABLE_APP_LIST, FIELD_MOFIFY_TIME, "INTEGER");
		addColumn(db, TABLE_APP_LIST, JK_APPLYDEFAULT_APP, "TEXT");

		addColumn(db, TABLE_NAME, FIELD_WGT_APPID, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_DEFAULT_APP, "INTEGER");
		addColumn(db, TABLE_NAME, FIELD_REMAIN_APP, "INTEGER");
		addColumn(db, TABLE_NAME, FIELD_APP_KEY, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_DEFAULT_VERSION, "TEXT");
		addColumn(db, TABLE_NAME, FIELD_MOFIFY_TIME, "INTEGER");
		addColumn(db, TABLE_NAME, JK_APPLYDEFAULT_APP, "TEXT");
		addColumn(db, TABLE_AD, FIELD_AD_TITLE, "TEXT");
	}

	private void createTable(SQLiteDatabase db, String table) {
		String sql = "CREATE TABLE IF NOT EXISTS " + table
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + FIELD_STORE_ID
				+ " TEXT," + FIELD_APP_NAME + " TEXT," + FIELD_APP_TYPE
				+ " INTEGER," + FIELD_DOWNLOAD_URL + " TEXT," + FIELD_ICON_LOC
				+ " TEXT," + FIELD_STATE + " INTEGER," + FIELD_INSTALL_PATH
				+ " TEXT," + FIELD_MAIN_APP + " TEXT," + FIELD_APP_VER
				+ " TEXT," + FIELD_CERTIFICATES_PATH + " TEXT,"
				+ FIELD_CERTIFICATES_PWD + " TEXT," + FIELD_PACKAGENAME
				+ " TEXT," + FIELD_CERTIFICATES_URL + " TEXT," + FIELD_APPID
				+ " TEXT," + FIELD_WGT_APPID + " TEXT, " + FIELD_APP_KEY
				+ " TEXT, " + FIELD_DEFAULT_APP + " INTEGER, "
				+ JK_APPLYDEFAULT_APP + " TEXT, " + FIELD_REMAIN_APP
				+ " TEXT, " + FIELD_NEW_APP + " INTEGER, "
				+ FIELD_DEFAULT_VERSION + " TEXT, " + FIELD_MOFIFY_TIME
				+ " INTEGER, " + FIELD_SIZE + " LONG," + FIELD_DESCRIPTION
				+ " TEXT," + FIELD_POSITION + " INTEGER DEFAULT 100,"
				+ FIELD_TIME + " TEXT," + FIELD_UPDATE_TIME + " TEXT,"
				+ FIELD_CREATE_TIME + " TEXT)";
		db.execSQL(sql);
	}

	/**
	 * 
	 * @param db
	 * @param table
	 *            数据表
	 * @param column
	 *            新增字段
	 * @param type
	 *            字段类型
	 */
	private void addColumn(SQLiteDatabase db, String table, String column,
			String type) {
		Cursor c = null;
		try {
			String querySql = "SELECT * FROM " + table;
			c = db.rawQuery(querySql, null);
			if (c != null) {
				int index = c.getColumnIndex(column);
				c.close();
				if (index == -1) {
					String addSql = "ALTER TABLE " + table + " ADD " + column
							+ " " + type;
					db.execSQL(addSql);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}

	}

	private static final String CREATE_UPDATE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_UPDATE_INFO + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ FIELD_STORE_ID + " TEXT," + FIELD_APP_ID + " TEXT,"
			+ FIELD_APP_NAME + " TEXT," + FIELD_APP_VER + " TEXT,"
			+ JK_APPLYDEFAULT_APP + " TEXT, " + FIELD_FILE_PATH + " TEXT,"
			+ FIELD_DOWNLOAD_URL + " TEXT)";

	private static final String CREATE_AD = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_AD + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FIELD_AD_IMG_URL + " TEXT, " + FIELD_AD_LINK_URL + " TEXT, "
			+ JK_APPLYDEFAULT_APP + " TEXT, " + FIELD_AD_IMG_PATH + " TEXT, "
			+ FIELD_AD_TITLE + " TEXT)";

}
