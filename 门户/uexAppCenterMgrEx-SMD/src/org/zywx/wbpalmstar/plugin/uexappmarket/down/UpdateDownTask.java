package org.zywx.wbpalmstar.plugin.uexappmarket.down;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AppsListAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.MyAsyncTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Utils;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

/**
 * @author Jinbin Lang
 * @version 创建时间：2013-12-14 上午11:12:18 类说明：
 */
@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class UpdateDownTask extends MyAsyncTask {
	public static final int SDCARD_NOT_EXISTS = 10010;
	public static final int SDCARD_FREE_SPACE_INSUFFICIENT = 10011;
	public static final String TAG = "UpdateDownTask";
	public AppBean appBean;
	private DragGridView gridView;

	private long length;
	private long count;
	private boolean isWait = false;
	private int mPosition;
	DatabaseHelper mDatabaseHelper = null;
	SQLiteDatabase mDatabase = null;
	private int type = -1;
	private final static String TABLE_NAME = "Downloader";
	public static String F_CREATETABLE_SQL;

	Dialog mDialog;
	private Context mContext;
	private int mProgress;

	public UpdateDownTask(AppBean appBean, DragGridView gridView, int inType,
			int position) {
		if (appBean == null) {
			throw new NullPointerException(
					"new AppDownTask params can not be null...");
		}
		this.type = inType;
		this.appBean = appBean;
		this.gridView = gridView;
		mPosition = position;
		// timer = new Timer();
		// creatTaskTable();
		mContext = gridView.getContext();
		// creatTaskTable();

	}

	public AppBean getAppBean() {
		return this.appBean;
	}

	public String getTaskId() {
		return appBean.getId();
	}

	public void setWait(boolean wait) {
		isWait = wait;
	}

	public boolean getWait() {
		return isWait;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected Object doInBackground(Object... params) {

		HttpURLConnection conn = null;
		InputStream is = null;
		File tmpFile = null;
		RandomAccessFile fos = null;
		FileInputStream fis = null;
		long fileSize = 0;
		// timer.schedule(timerTask, 0, 1000);

		try {
			Activity activity = (Activity) gridView.getContext();
			String[] res = selectTaskFromDB((String) params[0]);
			if ("0".equals((String) params[1])) {
				type = 0;
			} else {
				type = 1;
			}
			if (res != null) {
				fileSize = Integer.parseInt(res[1]);
				count = Integer.parseInt(res[2]);
				tmpFile = new File(res[0]);
			} else {
				if (AppBean.TYPE_WIDGET == appBean.getType()) {
					tmpFile = CommonUtility.createCacheFile(activity);
				} else {
					tmpFile = CommonUtility.createExternalCacheFile(activity);
				}
				BDebug.i(TAG, "download tmpFile:" + tmpFile.getAbsolutePath());
				tmpFile.createNewFile();
			}

			if (fileSize > 0 && fileSize == count) {
				if (tmpFile.exists()) {
					Log.e(EUExAppMarketEx.TAG, "======文件存在，重命名========");

					if (AppBean.TYPE_NATIVE == appBean.getType()) {
						tmpFile = Utils.renameApk(tmpFile);
					} else {
						tmpFile = Utils.rename(tmpFile);
					}
					// return MyUtils.rename(tmpFile);
					return tmpFile;
				} else {
					Log.e(EUExAppMarketEx.TAG, "======文件不存在========");
					deleteTaskFromDB((String) params[0]);
					count = 0;
				}
			}

			String netExtra = CommonUtility.getNetExtras(gridView.getContext());
			if ("CTWAP".equalsIgnoreCase(netExtra)) {
				@SuppressWarnings("deprecation")
				InetSocketAddress inetSocketAddress = new InetSocketAddress(
						android.net.Proxy.getDefaultHost(),
						android.net.Proxy.getDefaultPort());
				Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP,
						inetSocketAddress);// 端口
				conn = (HttpURLConnection) new URL((String) params[0])
						.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) new URL((String) params[0])
						.openConnection();
			}

			conn.setConnectTimeout(5000);
			conn.setReadTimeout(1000 * 60);

			if (count > 0) {
				conn.setRequestProperty("RANGE", "bytes=" + count + "-");
			}

			// conn.setDoInput(true);
			// conn.setDoOutput(true);
			conn.connect();
			length = conn.getContentLength();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK
					|| conn.getResponseCode() == 206) {

				is = conn.getInputStream();
				if (is != null) {
					if (length == -1) {
						length = is.available();
					}
					length = length + count;
					addTaskToDB((String) params[0], tmpFile.getPath(), length);
					// countable = length == -1 ? false : true;
					byte[] buffer = new byte[8096];
					int actualSize = -1;
					fos = new RandomAccessFile(tmpFile, "rw");
					if (count > 0) {
						fos.seek(count);
					}
					float downloadPercent = 0;
					while (!isCancelled()
							&& (actualSize = is.read(buffer)) != -1) {
						fos.write(buffer, 0, actualSize);
						count = count + actualSize;

						downloadPercent = ((length == 0) ? 0
								: (100 * count / length));
						if (downloadPercent > 0) {
							CommonUtility.saveUpdateProgress(mContext, appBean,
									(int) downloadPercent);
							publishProgress((int) downloadPercent);

						}

						// updateTaskFromDB((String) params[0], count);
					}

					if (downloadPercent >= 100) {
						if (AppBean.TYPE_NATIVE == appBean.getType()) {
							tmpFile = Utils.renameApk(tmpFile);
						} else {
							tmpFile = Utils.rename(tmpFile);
						}

						updateTaskFromDB((String) params[0],
								tmpFile.getAbsolutePath(), (int) count);
					}
				}
			}

			String filePath = tmpFile.getAbsolutePath();
			if (tmpFile.exists() && (!filePath.endsWith(".tmp"))) {
				return tmpFile;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			updateTaskFromDB((String) params[0], (int) count);
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fis = null;
			}
			if (conn != null) {
				conn.disconnect();
			}

			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				fos = null;
			}
		}

		return null;
	}

	@Override
	public void handleOnPreLoad(MyAsyncTask task) {
		if (type == 0) {

			// adapter = (AppsListAdapter) gridView.getAdapter();
			if (appBean != null) {
				int progress = CommonUtility.getUpdateProgress(mContext,
						appBean);
				if (progress > 0) {
					mProgress = progress;
					showProgress(mProgress);
				}
			}
		}
	}

	@Override
	public void handleOnUpdateProgress(MyAsyncTask task, int percent) {
		if (type == 0) {
			int progress = CommonUtility.getUpdateProgress(mContext, appBean);
			this.mProgress = progress;
			if (percent > 0) {
				showProgress(mProgress);
			}

		}
	}

	@Override
	public void handleOnCanceled(MyAsyncTask task) {

		int progress = CommonUtility.getUpdateProgress(mContext, appBean);
		// if (progress>0) {

		this.mProgress = progress;
		showProgress(mProgress);
		// }

		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}

		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
			mDatabaseHelper = null;
		}
	}

	@Override
	public void handleOnCompleted(MyAsyncTask task, Object result) {

		if (mDialog != null) {
			mDialog.dismiss();
		}

		if (result != null && type == 0) {

		}
		int progress = CommonUtility.getUpdateProgress(mContext, appBean);

		if (progress > 0) {
			mProgress = progress;

			showProgress(mProgress);
		}

		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}

		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
			mDatabaseHelper = null;
		}

	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof UpdateDownTask) {
			UpdateDownTask task = (UpdateDownTask) o;
			if (task.getTaskId().equals(this.getTaskId())) {
				return true;
			}
		}
		return super.equals(o);
	}

	private void creatTaskTable() {
		if (mDatabaseHelper != null) {
			return;
		}
		mDatabaseHelper = new DatabaseHelper(gridView.getContext(),
				"Downloader.db", 1);
		mDatabase = mDatabaseHelper.getReadableDatabase();

	}

	private void addTaskToDB(String url, String filePath, long fileSize) {
		if (selectTaskFromDB(url) == null) {
			String sql = "INSERT INTO " + TABLE_NAME
					+ " (url,filePath,fileSize,downSize,time) VALUES ('" + url
					+ "','" + filePath + "','" + fileSize + "','0','"
					+ getNowTime() + "')";
			if (mDatabase == null) {
				creatTaskTable();
			}
			mDatabase.execSQL(sql);
			mDatabase.close();
			mDatabaseHelper.close();
			mDatabase = null;
			mDatabaseHelper = null;
		}

	}

	private void updateTaskFromDB(String url, int downSize) {
		String sql = "UPDATE " + TABLE_NAME + " SET time = '" + getNowTime()
				+ "',downSize ='" + downSize + "'  WHERE url = '" + url + "'";
		if (mDatabase == null) {
			creatTaskTable();
		}
		mDatabase.execSQL(sql);
		mDatabase.close();
		mDatabaseHelper.close();
		mDatabase = null;
		mDatabaseHelper = null;
	}

	private void updateTaskFromDB(String url, String filePath, int downSize) {
		String sql = "UPDATE " + TABLE_NAME + " SET time = '" + getNowTime()
				+ "', filePath='" + filePath + "', downSize ='" + downSize
				+ "'  WHERE url = '" + url + "'";
		if (mDatabase == null) {
			creatTaskTable();
		}
		mDatabase.execSQL(sql);
		mDatabase.close();
		mDatabaseHelper.close();
		mDatabase = null;
		mDatabaseHelper = null;
	}

	private String[] selectTaskFromDB(String url) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE url = '" + url
				+ "'";
		if (mDatabase == null) {
			creatTaskTable();
		}
		String[] reslt = null;
		Cursor cursor = mDatabase.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			reslt = new String[4];
			reslt[0] = cursor.getString(2);
			reslt[1] = cursor.getString(3);
			reslt[2] = cursor.getString(4);
			reslt[3] = cursor.getString(5);

		}
		cursor.close();
		mDatabase.close();
		mDatabaseHelper.close();
		mDatabase = null;
		mDatabaseHelper = null;
		return reslt;
	}

	public void deleteTaskFromDB(String url) throws SQLException {
		// mDatabaseHelper = new DatabaseHelper(mContext, "Downloader.db", 1);
		// mDatabase = mDatabaseHelper.getReadableDatabase();
		String sql = "DELETE FROM " + TABLE_NAME + " WHERE url = '" + url + "'";
		if (mDatabase == null) {
			creatTaskTable();
		}
		mDatabase.execSQL(sql);
		mDatabase.close();
		mDatabaseHelper.close();
		mDatabase = null;
		mDatabaseHelper = null;
	}

	private String getNowTime() {
		Time time = new Time();
		time.setToNow();
		int year = time.year;
		int month = time.month + 1;
		int day = time.monthDay;
		int minute = time.minute;
		int hour = time.hour;
		int sec = time.second;
		return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
				+ sec;
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {

		String dbName;
		Context context;
		public static final String DB_NAME = "Downloader.db";
		public static final int DB_VERSION = 1;
		public static final String TB_DOWNLOADER = TABLE_NAME;
		private static DatabaseHelper mDatabaseHelper;
		public static final String FIELD_URL = "url";
		public static final String FIELD_FILE_PATH = "filePath";

		public static final String FIELD_FILE_SIZE = "fileSize";

		public static synchronized DatabaseHelper getInstance(Context context) {
			if (mDatabaseHelper == null) {
				mDatabaseHelper = new DatabaseHelper(context, DB_NAME,
						DB_VERSION);
			}
			return mDatabaseHelper;
		}

		public static synchronized SQLiteDatabase getDB(Context context) {
			return getInstance(context).getWritableDatabase();
		}

		DatabaseHelper(Context context, String dbName, int dbVer) {
			super(context, dbName, null, dbVer);
			// createTableSql = createTableSql;
			this.dbName = dbName;
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			// db.execSQL("DROP TABLE IF EXISTS " + createTable_aql);
			F_CREATETABLE_SQL = "CREATE TABLE IF  NOT EXISTS "
					+ TABLE_NAME
					+ "(_id INTEGER PRIMARY KEY,url TEXT,filePath TEXT,fileSize TEXT,downSize TEXT,time TEXT)";
			db.execSQL(F_CREATETABLE_SQL);
			String sql = "CREATE TABLE IF  NOT EXISTS update_info(_id INTEGER PRIMARY KEY, position INTEGER, appId TEXT, filePath TEXT, ver TEXT)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			context.deleteDatabase(dbName);
		}
	}

	public void setContext(Context context) {
		mContext = context;
	}

	String getFilePath(boolean isDownloaded, String path) {
		String filePath = path;
		if (!TextUtils.isEmpty(filePath) && filePath.endsWith(".tmp")) {
			if (isDownloaded) {
				filePath = filePath.substring(0, filePath.lastIndexOf("."))
						+ ".zip";
			}
			return filePath;
		}
		return null;
	}

	public void close() {
		if (mDatabase != null) {
			mDatabase.close();
		}

		if (mDatabaseHelper != null) {
			mDatabaseHelper.close();
		}
	}

	public void setPosition(int pos) {
		mPosition = pos;
	}

	public int getPosition() {
		return mPosition;
	}

	private void showProgress(int percent) {

		showProgress(appBean, percent);
	}

	private void showProgress(AppBean appBean, int progress) {
		if (gridView != null) {

			AppsListAdapter mAdapter = (AppsListAdapter) gridView.getAdapter();
			int taskPos = mAdapter.getItemPosition(appBean);
			if (progress > 0) {
				mAdapter.updateViewProgress(taskPos, progress,
						AppBean.STATE_DOWNLOADED == appBean.getState(),
						isCancelled());
			}
		}
	}

}
