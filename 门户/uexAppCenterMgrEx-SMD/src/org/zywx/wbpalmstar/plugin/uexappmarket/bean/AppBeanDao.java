package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppDownTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppDownTask.DatabaseHelper;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.DateFormat;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;

public class AppBeanDao {

	private AppDbHelper dbHelper;

	private Context mContext;

	public AppBeanDao(Context context) {
		mContext = context;
		dbHelper = new AppDbHelper(context);
		// dbHelper = AppDbHelper.getInstance(context);
	}

	/**
	 * 添加一个应用下载信息
	 * 
	 * @param appBean
	 */
	public void addAppBean(AppBean appBean) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();

			String sql = "SELECT * FROM " + AppDbHelper.TABLE_NAME + " WHERE "
					+ AppDbHelper.FIELD_STORE_ID + "='" + appBean.getId() + "'";
			c = db.rawQuery(sql, new String[] {});
			if (c != null && c.getCount() > 0) {
				return;
			}
			Cursor cs = db.query(AppDbHelper.TABLE_NAME, null, null, null,
					null, null, null);

			ContentValues cv = new ContentValues();
			cv.put(AppDbHelper.FIELD_STORE_ID, appBean.getId());
			cv.put(AppDbHelper.FIELD_APP_NAME, appBean.getAppName());
			cv.put(AppDbHelper.FIELD_APP_TYPE, appBean.getType());
			cv.put(AppDbHelper.FIELD_DOWNLOAD_URL, appBean.getDownloadUrl());
			cv.put(AppDbHelper.FIELD_ICON_LOC, appBean.getIconLoc());
			cv.put(AppDbHelper.FIELD_STATE, appBean.getState());
			cv.put(AppDbHelper.FIELD_INSTALL_PATH, appBean.getInstallPath());
			cv.put(AppDbHelper.FIELD_APP_VER, appBean.getAppVer());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PATH,
					appBean.getCertificatesPath());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PWD,
					appBean.getCertificatesPwd());
			cv.put(AppDbHelper.FIELD_PACKAGENAME, appBean.getPackageName());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_URL,
					appBean.getCertificatesUrl());
			cv.put(AppDbHelper.FIELD_APPID, appBean.getAppId());
			cv.put(AppDbHelper.FIELD_WGT_APPID, appBean.getWgtAppId());
			cv.put(AppDbHelper.FIELD_APP_KEY, appBean.getWgtAppKey());
			cv.put(AppDbHelper.JK_APPLYDEFAULT_APP, appBean.getApplyDefault());
			cv.put(AppDbHelper.FIELD_DESCRIPTION, appBean.getDescription());
			cv.put(AppDbHelper.FIELD_SIZE, appBean.getPkgSize());
			cv.put(AppDbHelper.FIELD_UPDATE_TIME, appBean.getUpdateTime());
			cv.put(AppDbHelper.FIELD_TIME, appBean.getUpTime());
			cv.put(AppDbHelper.FIELD_MAIN_APP, appBean.getMainApp());
			db.insert(AppDbHelper.TABLE_NAME, null, cv);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	/**
	 * 添加一个默认下载的App
	 * 
	 * @param appBean
	 */
	public void addDefaultAppBean(AppBean appBean) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();

			Cursor cs = db.query(AppDbHelper.TABLE_NAME, null, "appId=?",
					new String[] { appBean.getAppId() }, null, null, null);
			if (cs.getCount() != 0) {
				return;
			}
			ContentValues cv = new ContentValues();
			cv.put(AppDbHelper.FIELD_STORE_ID, appBean.getId());
			cv.put(AppDbHelper.FIELD_APP_NAME, appBean.getAppName());
			cv.put(AppDbHelper.FIELD_APP_TYPE, appBean.getType());
			cv.put(AppDbHelper.FIELD_DOWNLOAD_URL, appBean.getDownloadUrl());
			cv.put(AppDbHelper.FIELD_ICON_LOC, appBean.getIconLoc());
			cv.put(AppDbHelper.FIELD_STATE, 1);
			cv.put(AppDbHelper.FIELD_MAIN_APP, appBean.getMainApp());

			String pathSandbox = Tools.getSandbox();
			File file = new File(pathSandbox);
			// File dir = Environment.getExternalStorageDirectory();
			// File file = new File(dir + "/widgetone/widgets");

			if (!file.exists()) {
				file.mkdirs();
			}

			cv.put(AppDbHelper.FIELD_INSTALL_PATH, file.getAbsolutePath() + "/"
					+ appBean.getAppId());
			cv.put(AppDbHelper.FIELD_APP_VER, appBean.getAppVer());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PATH,
					appBean.getCertificatesPath());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PWD,
					appBean.getCertificatesPwd());
			cv.put(AppDbHelper.FIELD_PACKAGENAME, appBean.getPackageName());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_URL,
					appBean.getCertificatesUrl());
			cv.put(AppDbHelper.FIELD_APPID, appBean.getAppId());
			cv.put(AppDbHelper.FIELD_WGT_APPID, appBean.getWgtAppId());
			cv.put(AppDbHelper.FIELD_APP_KEY, appBean.getWgtAppKey());
			cv.put(AppDbHelper.JK_APPLYDEFAULT_APP, appBean.getApplyDefault());
			cv.put(AppDbHelper.FIELD_DESCRIPTION, appBean.getDescription());
			cv.put(AppDbHelper.FIELD_SIZE, appBean.getPkgSize());
			cv.put(AppDbHelper.FIELD_UPDATE_TIME, appBean.getUpdateTime());
			cv.put(AppDbHelper.FIELD_TIME, appBean.getUpTime());
			cv.put(AppDbHelper.FIELD_MAIN_APP, appBean.getMainApp());

			db.insert(AppDbHelper.TABLE_NAME, null, cv);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	/**
	 * 添加一个应用到应用列表
	 * 
	 * @param appBean
	 */
	public void addApp(String table, AppBean appBean) {
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();
			// String sql = "SELECT * FROM " + table;
			// String sql = "SELECT * FROM " + table + " WHERE "
			// + AppDbHelper.FIELD_STORE_ID + "=?";
			String sql = "SELECT COUNT(*) FROM " + table + " WHERE "
					+ AppDbHelper.FIELD_STORE_ID + "=?";
			String[] selectionArgs = { String.valueOf(appBean.getId()) };
			c = db.rawQuery(sql, selectionArgs);
			c.moveToFirst();
			int count = c.getInt(0);
			// int count = c.getCount();
			// boolean isExist = false;

			if (c == null || count > 0) {
				return;
			}

			ContentValues cv = new ContentValues(7);
			cv.put(AppDbHelper.FIELD_STORE_ID, appBean.getId());
			cv.put(AppDbHelper.FIELD_APP_NAME, appBean.getAppName());
			cv.put(AppDbHelper.FIELD_APP_TYPE, appBean.getType());
			cv.put(AppDbHelper.FIELD_DOWNLOAD_URL, appBean.getDownloadUrl());
			cv.put(AppDbHelper.FIELD_ICON_LOC, appBean.getIconLoc());
			cv.put(AppDbHelper.FIELD_STATE, appBean.getState());
			cv.put(AppDbHelper.FIELD_INSTALL_PATH, appBean.getInstallPath());
			cv.put(AppDbHelper.FIELD_APP_VER, appBean.getAppVer());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PATH,
					appBean.getCertificatesPath());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PWD,
					appBean.getCertificatesPwd());
			cv.put(AppDbHelper.FIELD_PACKAGENAME, appBean.getPackageName());
			cv.put(AppDbHelper.FIELD_CERTIFICATES_URL,
					appBean.getCertificatesUrl());
			cv.put(AppDbHelper.FIELD_APPID, appBean.getAppId());
			cv.put(AppDbHelper.FIELD_MAIN_APP, appBean.getMainApp());
			cv.put(AppDbHelper.FIELD_WGT_APPID, appBean.getWgtAppId());
			cv.put(AppDbHelper.FIELD_DEFAULT_APP, appBean.getDefaultApp());
			cv.put(AppDbHelper.FIELD_REMAIN_APP, appBean.getRemainApp());
			cv.put(AppDbHelper.FIELD_NEW_APP, appBean.getNewApp());
			cv.put(AppDbHelper.FIELD_APP_KEY, appBean.getWgtAppKey());
			cv.put(AppDbHelper.FIELD_DEFAULT_VERSION,
					appBean.getDefaultAppVer());
			cv.put(AppDbHelper.JK_APPLYDEFAULT_APP, appBean.getApplyDefault());
			cv.put(AppDbHelper.FIELD_DESCRIPTION, appBean.getDescription());
			cv.put(AppDbHelper.FIELD_SIZE, appBean.getPkgSize());
			cv.put(AppDbHelper.FIELD_TIME, appBean.getUpTime());
			cv.put(AppDbHelper.FIELD_UPDATE_TIME, appBean.getUpdateTime());
			db.insert(AppDbHelper.TABLE_APP_LIST, null, cv);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	/** 删除表中所有数据 */
	public void deleteAllFromTable(String table) {

		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "DELETE FROM " + table;
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
				// db = null;
			}
		}
	}

	/** 将集合中所有数据插入数据库 */
	public void addAppList(List<AppBean> list, String table) {
		if (list == null || list.size() < 1) {
			return;
		}

		try {

			int size = list.size();
			for (int i = 0; i < size; ++i) {
				AppBean appBean = list.get(i);
				if (appBean != null) {
					addApp(table, appBean);

				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteApp(String table, String appId) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			if (!TextUtils.isEmpty(appId) && !"null".equalsIgnoreCase(appId)) {
				String deleteSql = "DELETE FROM " + table + " WHERE "
						+ AppDbHelper.FIELD_APP_ID + "='" + appId + "'";
				db.execSQL(deleteSql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void deleteApp2(String table, String appId) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			if (!TextUtils.isEmpty(appId) && !"null".equalsIgnoreCase(appId)) {
				String deleteSql = "DELETE FROM " + table + " WHERE "
						+ AppDbHelper.FIELD_APPID + "='" + appId + "'";
				db.execSQL(deleteSql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void updateAppVer(String id, String appVer, String installPath) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// ContentValues cv = new ContentValues();
			// cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			// cv.put(AppDbHelper.FIELD_INSTALL_PATH, installPath);
			String sql = "Update " + AppDbHelper.TABLE_NAME + " set "
					+ AppDbHelper.FIELD_APP_VER + " = '" + appVer + "',"
					+ AppDbHelper.FIELD_INSTALL_PATH + " = '" + installPath
					+ "' where " + AppDbHelper.FIELD_APPID + " = '" + id + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void updateAppVer2(String id, String appVer, String installPath) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// ContentValues cv = new ContentValues();
			// cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			// cv.put(AppDbHelper.FIELD_INSTALL_PATH, installPath);
			String sql = "Update " + AppDbHelper.TABLE_APP_LIST + " set "
					+ AppDbHelper.FIELD_APP_VER + " = '" + appVer + "',"
					+ AppDbHelper.FIELD_INSTALL_PATH + " = '" + installPath
					+ "' where " + AppDbHelper.FIELD_APPID + " = '" + id + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}

	public void updateAppState(String id, int state, String installPath) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// ContentValues cv = new ContentValues();
			// cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			// cv.put(AppDbHelper.FIELD_INSTALL_PATH, installPath);
			String sql = "Update " + AppDbHelper.TABLE_NAME + " set "
					+ AppDbHelper.FIELD_STATE + " = " + state + ", "
					+ AppDbHelper.FIELD_INSTALL_PATH + "='" + installPath + "'"
					+ " where " + AppDbHelper.FIELD_APPID + " = '" + id + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}

	public void updateCertificates(String id, String path, String pwd) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PATH, path);
			cv.put(AppDbHelper.FIELD_CERTIFICATES_PWD, pwd);
			db.update(AppDbHelper.TABLE_NAME, cv, AppDbHelper.FIELD_STORE_ID
					+ "=" + id, null);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	public void updateWgtAppId(String id, String appVer, String wgtAppId) {
		updateWgtId(AppDbHelper.TABLE_APP_LIST, id, appVer, wgtAppId);
		updateWgtId(AppDbHelper.TABLE_NAME, id, appVer, wgtAppId);
	}

	public void updateWgtId(String tableName, String id, String appVer,
			String wgtAppId) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// ContentValues cv = new ContentValues();
			// cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			// cv.put(AppDbHelper.FIELD_INSTALL_PATH, installPath);
			String sql = "Update " + tableName + " set "
					+ AppDbHelper.FIELD_WGT_APPID + " = '" + wgtAppId + "'"
					+ " where " + AppDbHelper.FIELD_STORE_ID + " = '" + id
					+ "' AND " + AppDbHelper.FIELD_APP_VER + "='" + appVer
					+ "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}

	public void updateInstall(String id, String appVer, String installPath) {
		updateInstallPath(AppDbHelper.TABLE_APP_LIST, id, appVer, installPath);
	}

	public void updateInstallPath(String tableName, String id, String appVer,
			String installPath) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// ContentValues cv = new ContentValues();
			// cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			// cv.put(AppDbHelper.FIELD_INSTALL_PATH, installPath);
			String sql = "Update " + tableName + " set "
					+ AppDbHelper.FIELD_INSTALL_PATH + " = '" + installPath
					+ "'" + " where " + AppDbHelper.FIELD_STORE_ID + " = '"
					+ id + "' AND " + AppDbHelper.FIELD_APP_VER + "='" + appVer
					+ "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}

	public List<AppBean> getInstallAppList() {
		List<AppBean> list = new ArrayList<AppBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_NAME
					+ " ORDER BY " + AppDbHelper.FIELD_CREATE_TIME + " DESC";
			cursor = db.rawQuery(sql, new String[] {});
			if (cursor != null) {
				int storeIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
				int appNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
				int appTypeIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
				int downloadUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
				int iconUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
				int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
				int installPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
				int appVerIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_VER);
				int certificatesPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
				int certificatesPwdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
				int packageNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
				int certificatesUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
				int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
				int wgtAppIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
				int wgtAppKey = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_KEY);

				int defaultAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
				int remainAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);

				int descriptionIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DESCRIPTION);
				int pkgSizeIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_SIZE);
				int uptimeIndex = cursor.getColumnIndex(AppDbHelper.FIELD_TIME);
				int updatetimeIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_UPDATE_TIME);

				// int applydefaultIndex
				// =cursor.getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);
				while (cursor.moveToNext()) {
					AppBean appBean = new AppBean();
					appBean.setId(cursor.getString(storeIdIndex));
					appBean.setAppName(cursor.getString(appNameIndex));
					appBean.setType(cursor.getInt(appTypeIndex));
					appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
					appBean.setIconLoc(cursor.getString(iconUrlIndex));
					appBean.setState(cursor.getInt(stateIndex));
					appBean.setAppVer(cursor.getString(appVerIndex));
					appBean.setCertificatesPath(cursor
							.getString(certificatesPathIndex));
					appBean.setCertificatesPwd(cursor
							.getString(certificatesPwdIndex));
					// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
					appBean.setPackageName(cursor.getString(packageNameIndex));
					appBean.setCertificatesUrl(cursor
							.getString(certificatesUrlIndex));
					appBean.setAppId(cursor.getString(appIdIndex));
					appBean.setWgtAppKey(cursor.getString(wgtAppKey));
					appBean.setMainApp(cursor.getString(cursor
							.getColumnIndex(AppDbHelper.FIELD_MAIN_APP)));

					appBean.setDescription(cursor.getString(descriptionIndex));
					appBean.setPkgSize(cursor.getLong(pkgSizeIndex));
					appBean.setUpTime(cursor.getString(uptimeIndex));
					appBean.setUpdateTime(cursor.getLong(updatetimeIndex));
					if (defaultAppIndex > -1) {
						appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
					}

					if (remainAppIndex > -1) {
						appBean.setRemainApp(cursor.getInt(remainAppIndex));
					}

					if (wgtAppIdIndex > -1) {
						appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
					}

					if (appBean.getType() == AppBean.TYPE_WIDGET) {
						appBean.setInstallPath(cursor
								.getString(installPathIndex));
					}
					list.add(appBean);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return list;
	}

	public boolean isInstall(AppBean app) {

		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();

			cursor = db.query(AppDbHelper.TABLE_NAME, null, " appId=? ",
					new String[] { app.getAppId() }, null, null, null);

			if (cursor != null) {
				return cursor.getCount() == 0 ? false : true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 从服务器取回应用列表存放在数据库中做临时用
	 * 
	 * @return
	 */
	public List<AppBean> getAppList() {
		List<AppBean> list = new ArrayList<AppBean>();
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_APP_LIST
					+ " ORDER BY " + AppDbHelper.FIELD_ID;
			cursor = db.rawQuery(sql, new String[] {});
			if (cursor != null) {
				int storeIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
				int appNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
				int appTypeIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
				int downloadUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
				int iconUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
				int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
				int installPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
				int appVerIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_VER);
				int certificatesPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
				int certificatesPwdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
				int packageNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
				int certificatesUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
				int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
				int wgtAppIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
				int mainAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
				int newAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_NEW_APP);

				int defaultAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);

				int remainAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);

				int defAppVerIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);

				// int applydefaultIndex =
				// cursor.getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);

				while (cursor.moveToNext()) {
					AppBean appBean = new AppBean();
					appBean.setId(cursor.getString(storeIdIndex));
					appBean.setAppName(cursor.getString(appNameIndex));
					appBean.setType(cursor.getInt(appTypeIndex));
					appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
					appBean.setIconLoc(cursor.getString(iconUrlIndex));
					appBean.setState(cursor.getInt(stateIndex));
					appBean.setAppVer(cursor.getString(appVerIndex));
					appBean.setCertificatesPath(cursor
							.getString(certificatesPathIndex));
					appBean.setCertificatesPwd(cursor
							.getString(certificatesPwdIndex));
					appBean.setPackageName(cursor.getString(packageNameIndex));
					appBean.setCertificatesUrl(cursor
							.getString(certificatesUrlIndex));
					// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
					appBean.setAppId(cursor.getString(appIdIndex));
					appBean.setWgtAppKey(cursor.getString(cursor
							.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
					appBean.setUpdateTime(cursor.getLong(cursor
							.getColumnIndex("updateTime")));
					appBean.setUpTime(cursor.getString(cursor
							.getColumnIndex("up_time")));
					appBean.setPkgSize(cursor.getLong(cursor
							.getColumnIndex("pkgSize")));
					if (defaultAppIndex > -1) {
						appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
					}

					if (remainAppIndex > -1) {
						appBean.setRemainApp(cursor.getInt(remainAppIndex));
					}

					if (wgtAppIdIndex > -1) {
						appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
					}
					appBean.setMainApp(cursor.getString(mainAppIndex));
					if (appBean.getType() == AppBean.TYPE_WIDGET) {
						appBean.setInstallPath(cursor
								.getString(installPathIndex));
					}

					if (newAppIndex > -1) {
						appBean.setNewApp(cursor.getInt(newAppIndex));
					}

					appBean.setDefaultAppVer(defAppVerIndex > -1 ? cursor
							.getString(defAppVerIndex) : null);

					list.add(appBean);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;
	}

	public AppBean getApp(String id) {

		AppBean appBean = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_APP_LIST
					+ " WHERE " + AppDbHelper.FIELD_STORE_ID + "=" + id;
			cursor = db.rawQuery(sql, new String[] {});
			if (cursor != null) {
				int storeIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
				int appNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
				int appTypeIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
				int downloadUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
				int iconUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
				int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
				int installPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
				int appVerIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_APP_VER);
				int certificatesPathIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
				int certificatesPwdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
				int packageNameIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
				int certificatesUrlIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
				int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
				int wgtAppIdIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
				int mainAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
				int defaultAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
				int remainAppIndex = cursor
						.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);
				// int applydefaultIndex =
				// cursor.getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);
				if (cursor.moveToNext()) {
					appBean = new AppBean();
					appBean.setId(cursor.getString(storeIdIndex));
					appBean.setAppName(cursor.getString(appNameIndex));
					appBean.setType(cursor.getInt(appTypeIndex));
					appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
					appBean.setIconLoc(cursor.getString(iconUrlIndex));
					appBean.setState(cursor.getInt(stateIndex));
					appBean.setAppVer(cursor.getString(appVerIndex));
					appBean.setCertificatesPath(cursor
							.getString(certificatesPathIndex));
					appBean.setCertificatesPwd(cursor
							.getString(certificatesPwdIndex));
					appBean.setPackageName(cursor.getString(packageNameIndex));
					appBean.setCertificatesUrl(cursor
							.getString(certificatesUrlIndex));
					appBean.setAppId(cursor.getString(appIdIndex));
					// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
					appBean.setWgtAppKey(cursor.getString(cursor
							.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
					if (defaultAppIndex > -1) {
						appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
					}

					if (remainAppIndex > -1) {
						appBean.setRemainApp(cursor.getInt(remainAppIndex));
					}

					if (wgtAppIdIndex > -1) {
						appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
					}
					appBean.setMainApp(cursor.getString(mainAppIndex));
					if (appBean.getType() == AppBean.TYPE_WIDGET) {
						appBean.setInstallPath(cursor
								.getString(installPathIndex));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return appBean;
	}

	public void deleteSampData(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_NAME + " WHERE "
					+ AppDbHelper.FIELD_STORE_ID + "='" + appBean.getId() + "'";
			cursor = db.rawQuery(sql, new String[] {});
			List<String> list = new ArrayList<String>();
			if (cursor != null && cursor.getCount() > 1) {
				while (cursor.moveToNext()) {
					int storeIdIndex = cursor
							.getColumnIndex(AppDbHelper.FIELD_ID);
					String id = cursor.getString(storeIdIndex);
					if (!TextUtils.isEmpty(id)) {
						list.add(id);
					}
				}
			}

			if (list.size() > 0) {
				for (int i = 0, size = list.size(); i < size; i++) {
					if (i > 0) {
						String deleteSql = "DELETE FROM "
								+ AppDbHelper.TABLE_NAME + " WHERE "
								+ AppDbHelper.FIELD_ID + "='" + list.get(i)
								+ "'";
						db.execSQL(deleteSql);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}

			if (db != null && db.isOpen()) {
				db.close();
			}
		}

	}

	public List<AppBean> getDefaultAppList() {
		String sql = "SELECT * FROM app_list WHERE remain_app ==2 ORDER BY  default_app DESC, modify_time";
		return getData(sql);
	}

	public List<AppBean> getRemainAppList() {
		String sql = "SELECT * FROM app_list WHERE remain_app <>2";
		return getData(sql);
	}

	public List<AppBean> getallAppList() {
		String sql = "SELECT * FROM app_list ORDER BY "
				+ AppDbHelper.FIELD_CREATE_TIME + " DESC";
		return getData(sql);
	}

	/**
	 * 搜索模糊查询
	 * 
	 * @param str
	 * @return
	 */
	public List<AppBean> getSerachAppList(String str) {
		// String sql =
		// "SELECT  * FROM app_list WHERE remain_app LIKE '%"+str+"%'";
		String sql = "SELECT  * FROM app_list";

		if (str.trim().length() == 0) {
			return getData(sql);

		} else {

			return getData1(sql, str);
		}

	}

	public List<AppBean> getClickData(String id) {
		String sql = "SELECT  * FROM app_list WHERE appId=" + id;
		return getData(sql);
	}

	public List<AppBean> getData(String sql) {

		List<AppBean> list = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getWritableDatabase();
			cursor = db.rawQuery(sql, new String[] {});

			if (cursor == null || cursor.getCount() < 1) {
				return null;
			}

			list = new ArrayList<AppBean>();

			int storeIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
			int appNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
			int appTypeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
			int downloadUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
			int iconUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
			int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
			int installPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
			int appVerIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_VER);
			int certificatesPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
			int certificatesPwdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
			int packageNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
			int certificatesUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
			int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
			int wgtAppIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
			int wgtAppKeyIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_KEY);
			int defaultAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
			int remainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);
			int mainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
			int defaultAppVerIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);
			int modifyTimeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MOFIFY_TIME);
			int descriptionIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DESCRIPTION);
			int pkgSizeIndex = cursor.getColumnIndex(AppDbHelper.FIELD_SIZE);
			int upTimeIntex = cursor.getColumnIndex(AppDbHelper.FIELD_TIME);
			int updateTimeIntex = cursor
					.getColumnIndex(AppDbHelper.FIELD_UPDATE_TIME);

			// int applydefaultIndex =
			// cursor.getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);
			while (cursor.moveToNext()) {
				AppBean appBean = new AppBean();
				appBean.setId(cursor.getString(storeIdIndex));
				appBean.setAppName(cursor.getString(appNameIndex));
				appBean.setType(cursor.getInt(appTypeIndex));
				appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
				appBean.setIconLoc(cursor.getString(iconUrlIndex));
				appBean.setState(cursor.getInt(stateIndex));
				appBean.setAppVer(cursor.getString(appVerIndex));
				appBean.setCertificatesPath(cursor
						.getString(certificatesPathIndex));
				appBean.setCertificatesPwd(cursor
						.getString(certificatesPwdIndex));
				appBean.setCertificatesUrl(cursor
						.getString(certificatesUrlIndex));
				appBean.setPackageName(cursor.getString(packageNameIndex));
				appBean.setAppId(cursor.getString(appIdIndex));
				appBean.setDescription(cursor.getString(descriptionIndex));
				appBean.setPkgSize(cursor.getLong(pkgSizeIndex));
				appBean.setUpTime(cursor.getString(upTimeIntex));
				appBean.setUpdateTime(cursor.getLong(updateTimeIntex));
				// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
				appBean.setWgtAppKey(wgtAppKeyIndex == -1 ? null : cursor
						.getString(cursor
								.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
				if (defaultAppIndex > -1) {
					appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
				}

				if (remainAppIndex > -1) {
					appBean.setRemainApp(cursor.getInt(remainAppIndex));
				}

				if (wgtAppIdIndex > -1) {
					appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
				}
				appBean.setMainApp(cursor.getString(mainAppIndex));
				if (appBean.getType() == AppBean.TYPE_WIDGET) {
					appBean.setInstallPath(cursor.getString(installPathIndex));
				}
				appBean.setDefaultAppVer(defaultAppVerIndex > -1 ? cursor
						.getString(defaultAppVerIndex) : null);

				appBean.setModifyTime(modifyTimeIndex > -1 ? cursor
						.getLong(modifyTimeIndex) : 0);
				list.add(appBean);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;

	}

	public List<AppBean> getData1(String sql, String result) {

		List<AppBean> list = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getWritableDatabase();
			cursor = db.rawQuery(sql, new String[] {});
			if (cursor == null || cursor.getCount() < 1) {
				return null;
			}

			list = new ArrayList<AppBean>();

			int storeIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
			int appNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
			int appTypeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
			int downloadUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
			int iconUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
			int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
			int installPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
			int appVerIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_VER);
			int certificatesPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
			int certificatesPwdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
			int packageNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
			int certificatesUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
			int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
			int wgtAppIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
			int wgtAppKeyIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_KEY);
			int defaultAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
			int remainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);
			int mainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
			int defaultAppVerIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);
			int modifyTimeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MOFIFY_TIME);
			// int applydefaultIndex = cursor
			// .getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);
			// //remain_app
			while (cursor.moveToNext()) {
				if (cursor.getString(appNameIndex).indexOf(result) >= 0) {

					AppBean appBean = new AppBean();
					appBean.setId(cursor.getString(storeIdIndex));
					appBean.setAppName(cursor.getString(appNameIndex));
					appBean.setType(cursor.getInt(appTypeIndex));
					appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
					appBean.setIconLoc(cursor.getString(iconUrlIndex));
					appBean.setState(cursor.getInt(stateIndex));
					appBean.setAppVer(cursor.getString(appVerIndex));
					appBean.setCertificatesPath(cursor
							.getString(certificatesPathIndex));
					appBean.setCertificatesPwd(cursor
							.getString(certificatesPwdIndex));
					appBean.setCertificatesUrl(cursor
							.getString(certificatesUrlIndex));
					// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
					appBean.setPackageName(cursor.getString(packageNameIndex));
					appBean.setAppId(cursor.getString(appIdIndex));
					appBean.setWgtAppKey(wgtAppKeyIndex == -1 ? null : cursor
							.getString(cursor
									.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
					if (defaultAppIndex > -1) {
						appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
					}

					if (remainAppIndex > -1) {
						appBean.setRemainApp(cursor.getInt(remainAppIndex));
					}

					if (wgtAppIdIndex > -1) {
						appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
					}
					appBean.setMainApp(cursor.getString(mainAppIndex));
					if (appBean.getType() == AppBean.TYPE_WIDGET) {
						appBean.setInstallPath(cursor
								.getString(installPathIndex));
					}
					appBean.setDefaultAppVer(defaultAppVerIndex > -1 ? cursor
							.getString(defaultAppVerIndex) : null);

					appBean.setModifyTime(modifyTimeIndex > -1 ? cursor
							.getLong(modifyTimeIndex) : 0);
					list.add(appBean);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null) {
				db.close();
			}
		}
		return list;

	}

	// public List<AppBean> getDefaultAppList() {
	//
	// List<AppBean> list = getDataFromDB(AppDbHelper.TABLE_APP_LIST,
	// AppDbHelper.FIELD_REMAIN_APP,
	// String.valueOf(AppBean.NON_REMAIN_APP),
	// AppDbHelper.FIELD_MOFIFY_TIME);
	//
	// List<AppBean> defaultApps = new ArrayList<AppBean>();
	// List<AppBean> remainApps = new ArrayList<AppBean>();
	//
	// if (list != null && list.size() > 0) {
	// for (int i = 0; i < list.size(); i++) {
	// AppBean app = list.get(i);
	// if (app != null) {
	// if (app.getDefaultApp() == AppBean.DEFAULT_APP) {
	// defaultApps.add(app);
	// } else {
	// remainApps.add(app);
	// }
	// }
	// }
	// }
	//
	// List<AppBean> totalApps = new ArrayList<AppBean>();
	// if (defaultApps.size() > 0) {
	// totalApps.addAll(defaultApps);
	// }
	//
	// if (remainApps.size() > 0) {
	// totalApps.addAll(remainApps);
	// }
	//
	// return totalApps;
	// }

	/**
	 * 首页应用列表
	 * 
	 * @return
	 */
	public List<AppBean> getHomeApps() {
		return getDataFromDB(AppDbHelper.TABLE_APP_LIST,
				AppDbHelper.FIELD_REMAIN_APP,
				String.valueOf(AppBean.REMAIN_APP), null);
	}

	/**
	 * 默认应用列表
	 * 
	 * @return
	 */
	public List<AppBean> getDefaultApps() {
		return getDataFromDB(AppDbHelper.TABLE_APP_LIST,
				AppDbHelper.FIELD_DEFAULT_APP,
				String.valueOf(AppBean.DEFAULT_APP), null);
	}

	// public List<AppBean> getRemainAppList() {
	// return getDataFromDB(AppDbHelper.TABLE_APP_LIST,
	// AppDbHelper.FIELD_REMAIN_APP,
	// String.valueOf(AppBean.REMAIN_APP), null);
	// }

	public List<AppBean> getDataFromDB(String table, String whereKey,
			String whereValue, String order) {

		List<AppBean> list = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM " + table + " WHERE " + whereKey + "='"
					+ whereValue + "'";
			if (!TextUtils.isEmpty(order)) {
				sql = "SELECT * FROM " + table + " WHERE " + whereKey + "='"
						+ whereValue + "' ORDER BY " + order;
			}

			cursor = db.rawQuery(sql, new String[] {});
			if (cursor == null || cursor.getCount() < 1) {
				return null;
			}

			list = new ArrayList<AppBean>();

			int storeIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
			int appNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
			int appTypeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
			int downloadUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
			int iconUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
			int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
			int installPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
			int appVerIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_VER);
			int certificatesPathIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
			int certificatesPwdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
			int packageNameIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
			int certificatesUrlIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
			int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
			int wgtAppIdIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
			int wgtAppKeyIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_APP_KEY);
			int defaultAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
			int remainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);
			int mainAppIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
			int defaultAppVerIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);
			int modifyTimeIndex = cursor
					.getColumnIndex(AppDbHelper.FIELD_MOFIFY_TIME);
			// int applydefaultIndex = cursor
			// .getColumnIndex(AppDbHelper.JK_APPLYDEFAULT_APP);
			while (cursor.moveToNext()) {
				AppBean appBean = new AppBean();
				appBean.setId(cursor.getString(storeIdIndex));
				appBean.setAppName(cursor.getString(appNameIndex));
				appBean.setType(cursor.getInt(appTypeIndex));
				appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
				appBean.setIconLoc(cursor.getString(iconUrlIndex));
				appBean.setState(cursor.getInt(stateIndex));
				appBean.setAppVer(cursor.getString(appVerIndex));
				appBean.setCertificatesPath(cursor
						.getString(certificatesPathIndex));
				appBean.setCertificatesPwd(cursor
						.getString(certificatesPwdIndex));
				appBean.setPackageName(cursor.getString(packageNameIndex));
				appBean.setCertificatesUrl(cursor
						.getString(certificatesUrlIndex));
				appBean.setAppId(cursor.getString(appIdIndex));
				// appBean.setApplyDefault(cursor.getString(applydefaultIndex));
				appBean.setWgtAppKey(wgtAppKeyIndex == -1 ? null : cursor
						.getString(cursor
								.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
				if (defaultAppIndex > -1) {
					appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
				}

				if (remainAppIndex > -1) {
					appBean.setRemainApp(cursor.getInt(remainAppIndex));
				}

				if (wgtAppIdIndex > -1) {
					appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
				}
				appBean.setMainApp(cursor.getString(mainAppIndex));
				if (appBean.getType() == AppBean.TYPE_WIDGET) {
					appBean.setInstallPath(cursor.getString(installPathIndex));
				}
				appBean.setDefaultAppVer(defaultAppVerIndex > -1 ? cursor
						.getString(defaultAppVerIndex) : null);

				appBean.setModifyTime(modifyTimeIndex > -1 ? cursor
						.getLong(modifyTimeIndex) : 0);
				list.add(appBean);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
		return list;
	}

	public void deleteDefaultApp(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();
			// String querySql = "SELECT " + AppDbHelper.FIELD_APP_VER +
			// " FROM "
			// + AppDbHelper.TABLE_NAME + " WHERE "
			// + AppDbHelper.FIELD_STORE_ID + "='" + appBean.getId() + "'";

			String querySql = "SELECT * FROM " + AppDbHelper.TABLE_APP_LIST
					+ " WHERE " + AppDbHelper.FIELD_STORE_ID + "='"
					+ appBean.getId() + "'";

			c = db.rawQuery(querySql, null);
			String appVer = null;
			if (c != null && c.getCount() > 0 && c.moveToNext()) {

				int defaultAppVer = c
						.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);

				// appVer = c.getString(c
				// .getColumnIndex(AppDbHelper.FIELD_APP_VER));

				appVer = defaultAppVer > -1 ? c.getString(defaultAppVer) : null;

				c.close();
			}
			// / + AppDbHelper.FIELD_DEFAULT_APP + "=0, "
			String sql = "Update " + AppDbHelper.TABLE_APP_LIST + " SET "
					+ AppDbHelper.FIELD_INSTALL_PATH + "='', "
					+ AppDbHelper.FIELD_STATE + "=" + AppBean.STATE_UNLOAD
					+ ", " + AppDbHelper.FIELD_APP_VER + "='" + appVer + "', "
					+ AppDbHelper.FIELD_REMAIN_APP + "=" + AppBean.REMAIN_APP
					+ " WHERE " + AppDbHelper.FIELD_STORE_ID + " = '"
					+ appBean.getId() + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
			if (db != null && db.isOpen())
				db.close();
		}

	}

	public void deleteDownloadApp(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "DELETE FROM " + AppDbHelper.TABLE_NAME + " where "
					+ AppDbHelper.FIELD_STORE_ID + " = '" + appBean.getId()
					+ "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}
	}

	public void deleteDownFileWithDB(AppBean appBean) {
		SQLiteDatabase db = null;
		try {
			if (appBean == null || TextUtils.isEmpty(appBean.getDownloadUrl())) {
				return;
			}
			DatabaseHelper databaseHelper = DatabaseHelper
					.getInstance(mContext);
			db = databaseHelper.getWritableDatabase();
			String sql = "DELETE FROM Downloader WHERE url='"
					+ appBean.getDownloadUrl() + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

	}

	public String getDownFileFromDB(AppBean appBean) {
		Cursor c = null;
		SQLiteDatabase db = null;
		try {
			if (appBean == null || TextUtils.isEmpty(appBean.getDownloadUrl())) {
				return null;
			}
			DatabaseHelper databaseHelper = DatabaseHelper
					.getInstance(mContext);
			db = databaseHelper.getWritableDatabase();
			String sql = "SELECT * FROM Downloader WHERE url='"
					+ appBean.getDownloadUrl() + "'";
			c = db.rawQuery(sql, null);
			if (c == null || c.getCount() < 1 || !c.moveToNext()) {
				return null;
			}
			return c.getString(c.getColumnIndex("filePath"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (c != null) {
				c.close();
			}

			if (db != null) {
				db.close();
			}
		}
		return null;

	}

	public void deleteRemainApp(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "Update " + AppDbHelper.TABLE_APP_LIST + " set "
					+ AppDbHelper.FIELD_REMAIN_APP + "="
					+ AppBean.NON_REMAIN_APP + " WHERE "
					+ AppDbHelper.FIELD_STORE_ID + " = '" + appBean.getId()
					+ "'";
			if (appBean.getDefaultApp() == AppBean.NON_DEFAULT_APP) {
				sql = "Update " + AppDbHelper.TABLE_APP_LIST + " set "
						+ AppDbHelper.FIELD_MOFIFY_TIME + "="
						+ System.currentTimeMillis() + ", "
						+ AppDbHelper.FIELD_REMAIN_APP + "="
						+ AppBean.NON_REMAIN_APP + " WHERE "
						+ AppDbHelper.FIELD_STORE_ID + " = '" + appBean.getId()
						+ "'";
			}
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}

	}

	public void deleteRemainApp_appId(String appId) {
		List<AppBean> mappBean = getRemainAppList();
		AppBean app = null;
		for (int i = 0; i < mappBean.size(); i++) {
			if (appId.trim().equals(mappBean.get(i).getAppId())) {
				app = mappBean.get(i);
			}
		}

		if (app == null || TextUtils.isEmpty(app.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "Update " + AppDbHelper.TABLE_APP_LIST + " set "
					+ AppDbHelper.FIELD_REMAIN_APP + "="
					+ AppBean.NON_REMAIN_APP + " WHERE "
					+ AppDbHelper.FIELD_STORE_ID + " = '" + app.getId() + "'";
			if (app.getDefaultApp() == AppBean.NON_DEFAULT_APP) {
				sql = "Update " + AppDbHelper.TABLE_APP_LIST + " set "
						+ AppDbHelper.FIELD_MOFIFY_TIME + "="
						+ System.currentTimeMillis() + ", "
						+ AppDbHelper.FIELD_REMAIN_APP + "="
						+ AppBean.NON_REMAIN_APP + " WHERE "
						+ AppDbHelper.FIELD_STORE_ID + " = '" + app.getId()
						+ "'";
			}
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null && db.isOpen())
				db.close();
		}

	}

	public void addAllAppList(List<AppBean> list) {
		addAppList(list, AppDbHelper.TABLE_APP_LIST);
	}

	public void addUpdate(String id, String appId, String name, String appVer,
			String updateUrl, String filePath) {

		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			ContentValues cv = new ContentValues(7);
			cv.put(AppDbHelper.FIELD_STORE_ID, id);
			cv.put(AppDbHelper.FIELD_APP_ID, appId);
			cv.put(AppDbHelper.FIELD_APP_NAME, name);

			cv.put(AppDbHelper.FIELD_APP_VER, appVer);
			cv.put(AppDbHelper.FIELD_DOWNLOAD_URL, updateUrl);
			cv.put(AppDbHelper.FIELD_FILE_PATH, filePath);
			long insertId = db.insert(AppDbHelper.TABLE_UPDATE_INFO, null, cv);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	private void closeDB(SQLiteDatabase db) {
		if (db != null) {
			db.close();
		}
	}

	public void deleteUpdate(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return;
		}
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "DELETE FROM " + AppDbHelper.TABLE_UPDATE_INFO
					+ " WHERE  " + AppDbHelper.FIELD_APPID + "='"
					+ appBean.getAppId() + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

	}

	public String[] getUpdate(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return null;
		}
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_UPDATE_INFO
					+ " WHERE  " + AppDbHelper.FIELD_APP_ID + "='"
					+ appBean.getAppId() + "'";
			c = db.rawQuery(sql, null);
			if (c != null && c.getCount() > 0 && c.moveToNext()) {
				String path = c.getString(c
						.getColumnIndex(AppDbHelper.FIELD_FILE_PATH));
				String ver = c.getString(c
						.getColumnIndex(AppDbHelper.FIELD_APP_VER));
				return new String[] { path, ver };

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(c);
			closeDB(db);
		}

		return null;
	}

	public String[] getUpdate1(AppBean appBean) {
		if (appBean == null || TextUtils.isEmpty(appBean.getId())) {
			return null;
		}
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "SELECT * FROM " + AppDbHelper.TABLE_UPDATE_INFO
					+ " WHERE  " + AppDbHelper.FIELD_APP_ID + "='"
					+ appBean.getAppId() + "'";
			c = db.rawQuery(sql, null);
			if (c != null && c.getCount() > 0 && c.moveToNext()) {
				String path = c.getString(c
						.getColumnIndex(AppDbHelper.FIELD_FILE_PATH));
				String ver = c.getString(c
						.getColumnIndex(AppDbHelper.FIELD_APP_VER));
				return new String[] { path, ver };

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(c);
			closeDB(db);
		}

		return null;
	}

	private void closeCursor(Cursor c) {
		if (c != null) {
			c.close();
		}
	}

	public long deleteADEmpty() {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			int delId = db.delete(AppDbHelper.TABLE_AD,
					AppDbHelper.FIELD_AD_IMG_URL + " is ?", null);
			return delId;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

		return -1;
	}

	public long deleteAllAD() {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			return db.delete(AppDbHelper.TABLE_AD, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
		return -1;
	}

	public void updateNewAppState() {
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_NEW_APP + "=0 WHERE "
				+ AppDbHelper.FIELD_NEW_APP + "=1";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	public void updateDownloadUrl(AppBean serverApp) {
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_DOWNLOAD_URL + "='"
				+ serverApp.getDownloadUrl() + "' WHERE "
				+ AppDbHelper.FIELD_STORE_ID + "='" + serverApp.getId() + "'";
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

	}

	public void updateWgtAppKey(AppBean serverApp) {
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_APP_KEY + "='" + serverApp.getWgtAppKey()
				+ "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='"
				+ serverApp.getId() + "'";
		String sql2 = "UPDATE " + AppDbHelper.TABLE_NAME + " SET "
				+ AppDbHelper.FIELD_APP_KEY + "='" + serverApp.getWgtAppKey()
				+ "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='"
				+ serverApp.getId() + "'";

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL(sql);
			db.execSQL(sql2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

	}

	/**
	 * 更新是否默认app状态
	 */
	public void updateDefaultState(AppBean app) {
		if (app == null) {
			return;
		}

		// + AppDbHelper.FIELD_DEFAULT_APP + "=" + app.getDefaultApp()
		// + ", "
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_DEFAULT_APP + "=" + app.getDefaultApp()
				+ ", " + AppDbHelper.FIELD_REMAIN_APP + "="
				+ app.getRemainApp() + " WHERE " + AppDbHelper.FIELD_STORE_ID
				+ "='" + app.getId() + "'";

		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
			// db.execSQL(sql2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	/**
	 * 更新app标识
	 */
	public void updateRemainState(AppBean app, int appFlag) {
		if (app == null) {
			return;
		}

		// + AppDbHelper.FIELD_DEFAULT_APP + "=" + app.getDefaultApp()
		// + ", "
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_REMAIN_APP + "=" + appFlag + " WHERE "
				+ AppDbHelper.FIELD_STORE_ID + "='" + app.getId() + "'";

		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
			// db.execSQL(sql2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	public void updateDefaultAppVer(AppBean app) {
		if (app == null) {
			return;
		}
		// String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
		// + AppDbHelper.FIELD_DEFAULT_VERSION + "='" + app.getAppVer()
		// + "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='" + app.getId()
		// + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			// db.execSQL(sql);
			ContentValues values = new ContentValues();
			values.put(AppDbHelper.FIELD_APP_VER, app.getAppVer());
			values.put(AppDbHelper.FIELD_DEFAULT_VERSION, app.getAppVer());
			int num = db.update(AppDbHelper.TABLE_APP_LIST, values,
					AppDbHelper.FIELD_STORE_ID + "=?",
					new String[] { app.getId() });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	public void updateTime(AppBean app) {
		if (app == null) {
			return;
		}
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(AppDbHelper.FIELD_UPDATE_TIME, app.getUpdateTime());
			db.update(AppDbHelper.TABLE_APP_LIST, values,
					AppDbHelper.FIELD_STORE_ID + "=?",
					new String[] { app.getId() });
			db.update(AppDbHelper.TABLE_NAME, values,
					AppDbHelper.FIELD_STORE_ID + "=?",
					new String[] { app.getId() });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}
	}

	public void updateInstallMain(String table, AppBean appBean) {
		if (appBean == null) {
			return;
		}
		String sql = "UPDATE " + table + " SET " + AppDbHelper.FIELD_MAIN_APP
				+ "='" + appBean.getMainApp() + "' WHERE "
				+ AppDbHelper.FIELD_APPID + "='" + appBean.getAppId() + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			closeDB(db);
		}
	}

	public void updateIconUrl(AppBean serverApp, String id) {
		if (serverApp == null || TextUtils.isEmpty(serverApp.getIconLoc())) {
			return;
		}
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_ICON_LOC + "='" + serverApp.getIconLoc()
				+ "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='" + id + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			closeDB(db);
		}
	}

	public void updateDescription(AppBean serverApp, String id) {
		if (serverApp == null || TextUtils.isEmpty(serverApp.getDescription())) {
			return;
		}
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_DESCRIPTION + "='"
				+ serverApp.getDescription() + "' WHERE "
				+ AppDbHelper.FIELD_STORE_ID + "='" + id + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			closeDB(db);
		}
	}

	public void updateInstallDescription(AppBean serverApp, String id) {
		if (serverApp == null || TextUtils.isEmpty(serverApp.getDescription())) {
			return;
		}
		String sql = "UPDATE " + AppDbHelper.TABLE_NAME + " SET "
				+ AppDbHelper.FIELD_DESCRIPTION + "='"
				+ serverApp.getDescription() + "' WHERE "
				+ AppDbHelper.FIELD_STORE_ID + "='" + id + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			closeDB(db);
		}
	}

	public void updateInstallPkgSize(AppBean serverApp, String id) {
		// if (serverApp == null || TextUtils.isEmpty(serverApp.getPkgSize())) {
		// return;
		// }
		String sql = "UPDATE " + AppDbHelper.TABLE_NAME + " SET "
				+ AppDbHelper.FIELD_SIZE + "='" + serverApp.getPkgSize()
				+ "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='" + id + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			closeDB(db);
		}
	}

	public void updateAppName(AppBean app) {
		if (app == null) {
			return;
		}
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_APP_NAME + "='" + app.getAppName()
				+ "' WHERE " + AppDbHelper.FIELD_STORE_ID + "='" + app.getId()
				+ "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

	}

	public void updateAppSize(AppBean app) {
		if (app == null) {
			return;
		}
		String sql = "UPDATE " + AppDbHelper.TABLE_APP_LIST + " SET "
				+ AppDbHelper.FIELD_SIZE + "='" + app.getPkgSize() + "' WHERE "
				+ AppDbHelper.FIELD_STORE_ID + "='" + app.getId() + "'";
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeDB(db);
		}

	}

	/**
	 * 
	 */
	public List<AppBean> getOrderInstallApp() {
		List<AppBean> list = new ArrayList<AppBean>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.query(AppDbHelper.TABLE_NAME, null, null, null,
				null, null, "position");

		if (cursor == null) {
			return list;
		}
		int storeIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STORE_ID);
		int appNameIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_NAME);
		int appTypeIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_TYPE);
		int downloadUrlIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_DOWNLOAD_URL);
		int iconUrlIndex = cursor.getColumnIndex(AppDbHelper.FIELD_ICON_LOC);
		int stateIndex = cursor.getColumnIndex(AppDbHelper.FIELD_STATE);
		int installPathIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_INSTALL_PATH);
		int appVerIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_VER);
		int certificatesPathIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PATH);
		int certificatesPwdIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_PWD);
		int packageNameIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_PACKAGENAME);
		int certificatesUrlIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_CERTIFICATES_URL);
		int appIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APPID);
		int wgtAppIdIndex = cursor.getColumnIndex(AppDbHelper.FIELD_WGT_APPID);
		int wgtAppKeyIndex = cursor.getColumnIndex(AppDbHelper.FIELD_APP_KEY);
		int defaultAppIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_DEFAULT_APP);
		int remainAppIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_REMAIN_APP);
		int mainAppIndex = cursor.getColumnIndex(AppDbHelper.FIELD_MAIN_APP);
		int defaultAppVerIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_DEFAULT_VERSION);
		int modifyTimeIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_MOFIFY_TIME);
		int descriptionIndex = cursor
				.getColumnIndex(AppDbHelper.FIELD_DESCRIPTION);
		int pkgSizeIndex = cursor.getColumnIndex(AppDbHelper.FIELD_SIZE);
		int position = cursor.getColumnIndex(AppDbHelper.FIELD_POSITION);
		int uptimeIndex = cursor.getColumnIndex(AppDbHelper.FIELD_TIME);
		int newApp = cursor.getColumnIndex(AppDbHelper.FIELD_NEW_APP);
		while (cursor.moveToNext()) {
			AppBean appBean = new AppBean();
			appBean.setId(cursor.getString(storeIdIndex));
			appBean.setAppName(cursor.getString(appNameIndex));
			appBean.setType(cursor.getInt(appTypeIndex));
			appBean.setDownloadUrl(cursor.getString(downloadUrlIndex));
			appBean.setIconLoc(cursor.getString(iconUrlIndex));
			appBean.setState(cursor.getInt(stateIndex));
			appBean.setAppVer(cursor.getString(appVerIndex));
			appBean.setCertificatesPath(cursor.getString(certificatesPathIndex));
			appBean.setCertificatesPwd(cursor.getString(certificatesPwdIndex));
			appBean.setCertificatesUrl(cursor.getString(certificatesUrlIndex));
			appBean.setPackageName(cursor.getString(packageNameIndex));
			appBean.setAppId(cursor.getString(appIdIndex));

			appBean.setDescription(cursor.getString(descriptionIndex));
			appBean.setPkgSize(cursor.getLong(pkgSizeIndex));
			appBean.setPosition(cursor.getInt(position));
			appBean.setUpTime(cursor.getString(uptimeIndex));

			appBean.setNewApp(cursor.getInt(newApp));
			appBean.setWgtAppKey(wgtAppKeyIndex == -1 ? null
					: cursor.getString(cursor
							.getColumnIndex(AppDbHelper.FIELD_APP_KEY)));
			if (defaultAppIndex > -1) {
				appBean.setDefaultApp(cursor.getInt(defaultAppIndex));
			}

			if (remainAppIndex > -1) {
				appBean.setRemainApp(cursor.getInt(remainAppIndex));
			}

			if (wgtAppIdIndex > -1) {
				appBean.setWgtAppId(cursor.getString(wgtAppIdIndex));
			}
			appBean.setMainApp(cursor.getString(mainAppIndex));
			if (appBean.getType() == AppBean.TYPE_WIDGET) {
				appBean.setInstallPath(cursor.getString(installPathIndex));
			}
			appBean.setDefaultAppVer(defaultAppVerIndex > -1 ? cursor
					.getString(defaultAppVerIndex) : null);

			appBean.setModifyTime(modifyTimeIndex > -1 ? cursor
					.getLong(modifyTimeIndex) : 0);
			list.add(appBean);
		}
		return list;
	}

	public void setAppLocation(int position, String appId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("position", position);
		db.update(AppDbHelper.TABLE_NAME, values, AppDbHelper.FIELD_APPID
				+ "=?", new String[] { appId });

	}

	public int isAppInstalled(String appId) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query(AppDbHelper.TABLE_NAME, null, null, null, null,
				null, null);
		if (c != null && c.getCount() != 0) {
			while (c.moveToNext()) {
				String id = c.getString(c
						.getColumnIndex(AppDbHelper.FIELD_APPID));
				if (appId.trim().equals(id)) {
					return 1;
				}

			}

		}
		return 0;
	}

	public void setNewMsg(List<NewMsg> list) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		for (int i = 0, len = list.size(); i < len; i++) {
			NewMsg nm = list.get(i);
			ContentValues values = new ContentValues();
			values.put(AppDbHelper.FIELD_NEW_APP, nm.getNum());

			db.update(AppDbHelper.TABLE_NAME, values, AppDbHelper.FIELD_APPID
					+ "=?", new String[] { nm.getAppId() });
		}

	}

	/**
	 * 更新下载地址
	 * 
	 * @param appId
	 * @param downloadUrl
	 */
	public void updateDownloadUrl(String tabName, String appId,
			String downloadUrl) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "Update " + tabName + " set "
					+ AppDbHelper.FIELD_DOWNLOAD_URL + " = '" + downloadUrl
					+ "'" + " where " + AppDbHelper.FIELD_APPID + " = '"
					+ appId + "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
	}

	/**
	 * 更新下载图标
	 * 
	 * @param appId
	 * @param iconLoc
	 */
	public void updateDownloadLoc(String tabName, String appId, String iconLoc) {
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			String sql = "Update " + tabName + " set "
					+ AppDbHelper.FIELD_ICON_LOC + " = '" + iconLoc + "'"
					+ " where " + AppDbHelper.FIELD_APPID + " = '" + appId
					+ "'";
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null)
				db.close();
		}
	}

}
