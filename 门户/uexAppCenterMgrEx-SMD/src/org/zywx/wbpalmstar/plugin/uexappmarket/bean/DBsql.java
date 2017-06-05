package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AddAppActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AddAppsAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppDownTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppDownTask.DatabaseHelper;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnWidgetClickedCallback;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.MyAsyncTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DBsql {
	Context context;
	private OnWidgetClickedCallback mWidgetClickedCallback;
	private AppTaskList mAppTaskList;
	private AppDownTask updateDownTask;

	public DBsql(Context context, AppTaskList mAppTaskList,
			OnWidgetClickedCallback mWidgetClickedCallback) {
		this.context = context;
		this.mAppTaskList = mAppTaskList;
		this.mWidgetClickedCallback = mWidgetClickedCallback;
	}

	/*
	 * 获取下载文件的大小及其路径
	 */
	public String[] getDownFileFromDb(String url) {
		// DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
		// SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteDatabase db = null;
		Cursor c = null;
		String[] result = null;
		try {
			db = DatabaseHelper.getDB(context);
			String sql = "SELECT * FROM Downloader WHERE url='" + url + "'";
			// if (!db.isDbLockedByOtherThreads()) {
			c = db.rawQuery(sql, null);
			if (c.getCount() > 0) {
				c.moveToNext();
				String path = c.getString(c.getColumnIndex("filePath"));
				String fileSize = c.getString(c.getColumnIndex("fileSize"));
				result = new String[] { path, fileSize };
			}

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
		return result;
	}

	public void deleteUpdateFromDb(String url) {
		// DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
		// SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteDatabase db = DatabaseHelper.getDB(context);
		try {

			String sql = "DELETE FROM Downloader WHERE url='" + url + "'";
			db.execSQL(sql);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	public void unzip(final String filePath, final String appVer,
			final int type, final AppBean appBean, final int position,
			final ExpandableListView listView) {

		if (!CommonUtility.isExistSdcard()) {

			return;
		} else {
			long sdFree = CommonUtility.getSDFreeSize() * 1024 * 1024;
			if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
				long fileSize = new File(filePath).length();
				if (sdFree < fileSize) {

					return;
				}
			}
		}

		if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
			return;
		}

		new AsyncTask<Void, Void, String>() {
			Dialog dialog;

			protected void onPreExecute() {
				dialog = CommonUtility.showLoadingDialog(context);
				dialog.show();
			};

			@Override
			protected String doInBackground(Void... params) {
				String installPath = null;
				try {
					String pathSandbox = Tools.getSandbox();

					// installPath = CommonUtility.unzip(new FileInputStream(
					// new File(filePath)), CommonUtility.WIDGET_SAVE_PATH;,
					// null);
					installPath = CommonUtility.unzip(new FileInputStream(
							new File(filePath)), pathSandbox, null);
					if (TextUtils.isEmpty(installPath)
							|| !new File(installPath).exists()) {
						return null;
					}

					appBean.setState(1);
					appBean.setInstallPath(installPath);

					new File(filePath).delete();
					deleteUpdateFromDb(appBean.getDownloadUrl());

					switch (type) {
					case 0:
						// 应用信息添加到数据库
						new AppBeanDao(context).addAppBean(appBean);
						new AppBeanDao(context).updateAppState(appBean.getId(),
								appBean.getState(), appBean.getInstallPath());
						new AppBeanDao(context).updateAppVer2(appBean.getId(),
								appVer, installPath);
						break;
					case 1:
						// 更新数据库应用信息

						new AppBeanDao(context).updateAppVer2(appBean.getId(),
								appVer, installPath);
						new AppBeanDao(context).deleteUpdate(appBean);
						break;
					default:
						break;
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return installPath;
			}

			protected void onPostExecute(String result) {
				if (dialog != null) {
					dialog.dismiss();
				}

				if (result == null) {
					return;
				}
				AddAppsAdapter mAdapter = (AddAppsAdapter) listView
						.getExpandableListAdapter();
				mAdapter.onRefresh();
				// AddAppActivity activity = (AddAppActivity)
				// listView.getContext();
				// activity.onRefresh();

				switch (type) {
				case 0:
					List<AppBean> apps = new AppBeanDao(context)
							.getDefaultAppList();
					break;
				case 1:
					launch(appBean, 1, position, listView);
					break;
				default:
					break;
				}

			};
		}.execute();
	}

	public void launch(final AppBean appBean, final int type,
			final int position, final ExpandableListView listView) {
		switch (type) {
		case 0:
			if (!CommonUtility.isOnline(context)) {
				Toast.makeText(context, "网络异常!", Toast.LENGTH_SHORT).show();
				return;
			}
		case 3:

			break;

		default:
			break;
		}
		AppDownTask appDownTask = new AppDownTask(appBean, listView, type,
				position) {

			@Override
			protected Object doInBackground(Object... params) {
				Object result = null;
				switch (type) {

				case 0:
					// 下载应用
					String urlDown = appBean.getDownloadUrl();
					if (Tools.getURLStatus(urlDown)) {
						result = super.doInBackground(urlDown, "0");
					}
					if (result != null) {
						List<NameValuePair> nameValuePairsReport = new ArrayList<NameValuePair>();
						nameValuePairsReport.add(new BasicNameValuePair(
								"appId", appBean.getId()));
						// String response =
						// CommonUtility.sendHttpRequestByPost(
						// CommonUtility.Report_url, nameValuePairsReport,
						// mContext);
					}
					break;
				case 1:

					if (updateDownTask != null)

						// 打开应用
						// mWidgetClickedCallback.onWidgetClicked(
						// appBean.getInstallPath(), appBean.getWgtAppId());
						mWidgetClickedCallback.onWidgetClicked(
								appBean.getInstallPath(), appBean.getAppId(),
								appBean.getWgtAppKey());

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (updateDownTask != null
							&& Status.FINISHED != updateDownTask.getStatus()) {
						return null;
					}

					// 检查更新
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("subAppId",
							appBean.getAppId()));
					nameValuePairs.add(new BasicNameValuePair("version",
							appBean.getAppVer()));
					result = CommonUtility.sendHttpRequestByPost(
							CommonUtility.CHECKUPDATE_URL, nameValuePairs,
							context);
					Log.e(EUExAppMarketEx.TAG, "检查更新     " + result.toString());

					break;
				default:
					break;
				}
				return result;
			}

			@Override
			public void handleOnCanceled(MyAsyncTask task) {
				super.handleOnCanceled(task);
				mAppTaskList.removeTask(this);

			}

			public void handleOnCompleted(MyAsyncTask task, Object result) {
				super.handleOnCompleted(task, result);
				mAppTaskList.removeTask(this);

				if (AppBean.TYPE_NATIVE == appBean.getType()) {
					if (result instanceof File) {
						File file = (File) result;
						if (file != null && file.exists()) {
							CommonUtility.installApp(context, file);
							CommonUtility.removeProgress(context, appBean);
						}
					}
				} else {
					switch (type) {
					case 0:
						// 解压
						if (result == null) {
							// Toast.makeText(context, "解压失败!",
							// Toast.LENGTH_SHORT)
							// .show();
							Toast.makeText(context, "下载失败!", Toast.LENGTH_SHORT)
									.show();
							return;
						}

						if (result instanceof File) {
							File file = (File) result;
							unzip(file.getAbsolutePath(), appBean.getAppVer(),
									type, appBean, position, listView);
							EUExAppMarketEx.onCallBack.onInstallApp(
									appBean.getAppId(), appBean.getAppName(),
									appBean.getAppVer());
						}

						break;
					case 1:
						// 提示更新
						// 用户确认，下载更新，

						if (result == null) {
							return;
						}

						// if (updateDownTask != null
						// && Status.FINISHED != updateDownTask.getStatus()) {
						// return;
						// }
						String updateInfo = (String) result;
						checkUpdate(updateInfo, appBean, type, position,
								listView);

					default:
						break;
					}
				}
			}
		};
		mAppTaskList.addTask(appDownTask);
		appDownTask.execute();
	}

	private void checkUpdate(String jsonStr, AppBean appBean, int type,
			int position, final ExpandableListView listView) {

		if (TextUtils.isEmpty(jsonStr)) {
			return;
		}

		try {
			JSONObject json = new JSONObject(jsonStr);
			// String id = json.optString("id");
			// String wgtId = json.optString("wgtId");
			String updateUrl = json.getString("pkgUrl");
			String updateVer = json.getString("version");
			boolean patchFile = json.optBoolean("patchFile");
			boolean needConfirm = json.optBoolean("needConfirm");
			String updateMsg = json.optString("widgetUpdateHints");
			if (!needConfirm && patchFile) {
				// 必须强制升级且是补丁包，下载升级包
				update(appBean, type, position, updateUrl, updateVer, listView);
				return;
			}
			showUpdateDialog(false, patchFile, needConfirm, updateMsg, appBean,
					type, position, updateUrl, updateVer, listView);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void update(final AppBean appBean, final int type,
			final int position, final String updateUrl, final String updateVer,
			final ExpandableListView listView) {

		// AppBean app = appBean;
		// app.setState(0);
		// int inType = 0;

		// launch(appBean, gridView, type, position);

		updateDownTask = new AppDownTask(appBean, listView, type, position) {
			@Override
			protected Object doInBackground(Object... params) {
				Object object = null;
				if (Tools.getURLStatus(updateUrl)) {
					object = super.doInBackground(updateUrl, "1");
				}

				if (null != object && object instanceof File) {
					File file = (File) object;
					new AppBeanDao(context).addUpdate(appBean.getId(),
							appBean.getAppId(), appBean.getAppName(),
							updateVer, updateUrl, file.getAbsolutePath());
				}

				return object;
			}

			public void handleOnCompleted(MyAsyncTask task, Object result) {
				// mAppTaskList.removeTask(this);
				if (result == null) {
					return;
				}
				switch (type) {
				case 0:
					if (result instanceof File) {
						File file = (File) result;
						unzip(file.getAbsolutePath(), updateVer, type, appBean,
								position, listView);
					}
					break;

				case 1:

					break;
				default:
					break;
				}

			};

			@Override
			public void handleOnCanceled(MyAsyncTask task) {
				super.handleOnCanceled(task);
				// mAppTaskList.removeTask(this);
			}

		};
		updateDownTask.execute();
		// mAppTaskList.addTask(updateDownTask);
	}

	/**
	 * 
	 * @param cancel
	 * @param isPatch
	 *            是否补丁包
	 * @param needConfirm
	 *            是否需要用户确认
	 * @param msg
	 *            提示信息
	 * @param appBean
	 * @param gridView
	 * @param type
	 * @param position
	 * @param updateUrl
	 * @param appVer
	 */
	private void showUpdateDialog(boolean cancel, boolean isPatch,
			boolean needConfirm, String msg, AppBean appBean, int type,
			int position, String updateUrl, String appVer,
			ExpandableListView listView) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("升级提示");
		if (TextUtils.isEmpty(msg)) {
			String resId = isPatch ? "plugin_app_market_update_patch_default_msg"
					: "plugin_app_market_update_file_default_msg";
			msg = context.getString(EUExUtil.getResStringID(resId));
		}
		builder.setMessage(msg);
		builder.setPositiveButton("确定", new PositiveListener(context, appBean,
				type, position, updateUrl, appVer, listView));
		if (needConfirm && !isPatch) {
			builder.setNegativeButton("取消", new NegativeListener());
		}
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(cancel);
		dialog.show();
	}

	class PositiveListener implements DialogInterface.OnClickListener {
		private Context context;
		private AppBean appBean;
		// private int type;
		private int position;
		private String updateUrl;
		private String appVer;
		ExpandableListView listView;

		public PositiveListener(Context context, AppBean appBean, int type,
				int position, String updateUrl, String appVer,
				ExpandableListView listView) {
			super();
			this.context = context;
			this.appBean = appBean;
			// this.type = type;
			this.position = position;
			this.updateUrl = updateUrl;
			this.appVer = appVer;
			this.listView = listView;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			// update(appBean, gridView, type, position, updateUrl, appVer);

			AppBean app = appBean;
			app.setAppVer(appVer);
			app.setDownloadUrl(updateUrl);
			app.setState(AppBean.STATE_UNLOAD);
			// app.setAppId(appBean.getAppId());
			new AppBeanDao(context).updateAppState(appBean.getId(),
					AppBean.STATE_UNLOAD, appBean.getInstallPath());
			// 0 表示关闭
			// ((AppMarketMainActivity) context).finishWidget("",
			// appBean.getAppId(), "0");

			launch(app, 0, position, listView);
		};
	}

	class NegativeListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}

	}

	public String[] getFilePahFromDownload(String url) {
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
		SQLiteDatabase db = null;
		String sql = "SELECT * FROM " + DatabaseHelper.TB_DOWNLOADER
				+ " WHERE url='" + url + "'";
		Cursor c = null;
		String[] result = null;
		try {
			db = dbHelper.getReadableDatabase();
			c = db.rawQuery(sql, null);

			if (c != null && c.getCount() > 0) {
				c.moveToNext();
				String path = c.getString(c
						.getColumnIndex(DatabaseHelper.FIELD_FILE_PATH));
				String fileSize = c.getString(c
						.getColumnIndex(DatabaseHelper.FIELD_FILE_SIZE));
				result = new String[] { path, fileSize };
			}
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
		return result;
	}

	public void onDeleteApp(AppBean appBean) {
		if (appBean != null) {
			EUExAppMarketEx.onCallBack.onDeleteApp(appBean.getAppId(),
					appBean.getAppName(), appBean.getAppVer());
		}
	}

}
