package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AppsListAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppDownTask.DatabaseHelper;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateDownTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.DataCallBack;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.MyAsyncTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.CustomDialog;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;

import com.way.locus.Contains;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ViewDataManager {

	private static final String TAG = "ViewDataManager";
	private Context mContext;
	private UpdateTaskList mAppTaskList;
	private DataCallBack mDataCallBack;
	private String mRequestUrl;
	private Toast mToast;

	public ViewDataManager(Context context, String url,
			DataCallBack dataCallBack, UpdateTaskList appTaskList) {
		super();
		EUExUtil.init(context);
		this.mContext = context;
		this.mDataCallBack = dataCallBack;
		this.mAppTaskList = appTaskList;
		this.mRequestUrl = url;
		mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
	}

	public boolean performAsyncLoadAppListAction() {
		if (!CommonUtility.isOnline(mContext)) {
			return true;
		}

		new AsyncTask<Object, Void, List<AppBean>>() {
			ProgressDialog progressDialog = null;

			@Override
			protected void onPreExecute() {
				if (((AppMarketMainActivity) mContext).isFirstRun(mContext)) {
					progressDialog = ProgressDialog.show(mContext, null,
							"加载应用列表...", false, true,
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									cancel(true);
								}
							});
				}
			}

			@Override
			protected List<AppBean> doInBackground(Object... params) {

				String response = Tools.getRequest(mContext, mRequestUrl);

				List<AppBean> defaultApps = AppListJsonParser
						.parseDefaultList(response);

				List<AppBean> remainApps = AppListJsonParser
						.parseRemainList(response);

				List<AppBean> apps = new ArrayList<AppBean>();

				if (defaultApps != null) {
					apps.addAll(defaultApps);
				}

				if (remainApps != null) {
					apps.addAll(remainApps);
				}

				List<AppBean> appList = new AppBeanDao(mContext).getAppList();
				return syncAppsWithDB(appList, apps);
			}

			@Override
			protected void onPostExecute(List<AppBean> result) {

				mDataCallBack.callBackRes(result);
				if (isCancelled()) {
					return;
				}
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		}.execute(new Object[] {});
		return true;
	}

	/**
	 * 本地下载列表与Server同步下载状态
	 * 
	 * @param localApps
	 * @param serverApps
	 * @return
	 */
	public List<AppBean> syncLocalListWithServer(List<AppBean> localApps,
			List<AppBean> serverApps) {
		// if((serverApps == null
		// || serverApps.size() == 0) && (localApps == null || localApps.size()
		// == 0 )){
		// return null;
		// }
		if (serverApps == null || serverApps.size() == 0) {
			return localApps;
		}
		if ((localApps == null || localApps.size() == 0)) {
			return serverApps;
		}
		for (int i = 0, size = serverApps.size(); i < size; i++) {
			AppBean serverApp = serverApps.get(i);
			int index = localApps.indexOf(serverApp);
			if (index >= 0) {// find it
				// 发现本地App,将其安装状态赋值给ServerApp
				serverApp.setState(localApps.get(index).getState());
				serverApp.setInstallPath(localApps.get(index).getInstallPath());
				serverApp.setAppVer(localApps.get(index).getAppVer());
			}
		}
		return serverApps;
	}

	public void launch(final AppBean appBean, final DragGridView gridView,
			final int type, final int position) {

		switch (type) {
		case 0:
			if (!CommonUtility.isOnline(mContext)) {
				mToast.setText("网络不给力");
				mToast.show();

				return;
			}
			Log.e(EUExAppMarketEx.TAG, "网络不给力");
			break;
		case 3:
			try {
				JSONObject json = new JSONObject();
				json.put("platform", "android");
				json.put("pkgName", appBean.getPackageName() + "");
				json.put("key", appBean.getWgtAppKey() + "");
				EUExAppMarketEx.onCallBack.onStartNativeWight(json.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			break;

		default:
			break;
		}
		UpdateDownTask appDownTask = new UpdateDownTask(appBean, gridView,
				type, position) {

			@Override
			protected Object doInBackground(Object... params) {
				Object result = null;
				switch (type) {

				case 0:
					Log.i(EUExAppMarketEx.TAG, "appBean.getDownloadUrl()==="
							+ appBean.getDownloadUrl());
					// 下载应用
					result = super
							.doInBackground(appBean.getDownloadUrl(), "0");

					if (result != null) {
						List<NameValuePair> nameValuePairsReport = new ArrayList<NameValuePair>();
						nameValuePairsReport.add(new BasicNameValuePair(
								"appId", appBean.getId()));
					}
					break;
				case 1:

					// 打开应用
					EUExAppMarketEx.onCallBack.onWidgetClicked(
							appBean.getInstallPath(), appBean.getAppId(),
							appBean.getWgtAppKey());

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
							mContext);
					Log.e(EUExAppMarketEx.TAG, "检查更新    " + result);

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
							CommonUtility.installApp(mContext, file);
							CommonUtility.removeProgress(mContext, appBean);
						}
					}
				} else {
					switch (type) {
					case 0:
						// 解压
						if (result == null) {
							mToast.setText("下载失败");
							// mToast.setText("解压失败");
							mToast.show();
							return;
						}

						if (result instanceof File) {
							File file = (File) result;
							unzip(file.getAbsolutePath(), appBean.getAppVer(),
									type, appBean, gridView, position);
						}

						break;
					case 1:
						// 提示更新
						// 用户确认，下载更新，

						if (result == null) {
							return;
						}

						Log.i(EUExAppMarketEx.TAG, "======开始升级=====");
						String updateInfo = (String) result;
						checkUpdate(updateInfo, appBean, gridView, type,
								position);

					default:
						break;
					}
				}
			}
		};
		mAppTaskList.addTask(appDownTask);
		appDownTask.execute();
	}

	private void checkUpdate(String jsonStr, AppBean appBean,
			DragGridView gridView, int type, int position) {

		if (TextUtils.isEmpty(jsonStr)) {
			return;
		}
		// https://apptest.sumec.com/storeIn/store/searchAppList?iswantTiles=t&softToken=62f859682d8195f6f5190c7679f54b24
		try {
			JSONObject json = new JSONObject(jsonStr);
			// String id = json.optString("id");
			// String wgtId = json.optString("wgtId");
			String updateUrl = json.getString("pkgUrl");
			String updateVer = json.getString("version");
			boolean patchFile = json.optBoolean("patchFile");
			boolean needConfirm = json.optBoolean("needConfirm");
			String updateMsg = json.optString("widgetUpdateHints");
			boolean forceUpdate = json.optBoolean("forceUpdate");
			Log.i("", "needConfirm=====>>>" + needConfirm
					+ "=====patchFile===>>>" + patchFile
					+ "=========jsonStr=====>>>" + jsonStr);
			if (!needConfirm && forceUpdate) {
				// 必须强制升级且是补丁包，下载升级包
				update(appBean, gridView, type, position, updateUrl, updateVer);
				return;
			}
			showUpdateDialog(false, patchFile, needConfirm, updateMsg, appBean,
					gridView, type, position, updateUrl, updateVer);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			boolean needConfirm, String msg, AppBean appBean,
			DragGridView gridView, int type, int position, String updateUrl,
			String appVer) {
		// AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
		builder.setTitle("升级提示");
		if (TextUtils.isEmpty(msg)) {
			String resId = isPatch ? "plugin_app_market_update_patch_default_msg"
					: "plugin_app_market_update_file_default_msg";
			msg = mContext.getString(EUExUtil.getResStringID(resId));
		}
		builder.setMessage(msg);
		builder.setPositiveButton("确定", new PositiveListener(appBean, gridView,
				type, position, updateUrl, appVer));
		if (needConfirm && !isPatch) {
			builder.setNegativeButton("取消", new NegativeListener(appBean));
		}
		CustomDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(cancel);
		dialog.show();
	}

	class PositiveListener implements DialogInterface.OnClickListener {
		private AppBean appBean;
		private DragGridView gridView;
		// private int type;
		private int position;
		private String updateUrl;
		private String appVer;

		public PositiveListener(AppBean appBean, DragGridView gridView,
				int type, int position, String updateUrl, String appVer) {
			super();
			this.appBean = appBean;
			this.gridView = gridView;
			// this.type = type;
			this.position = position;
			this.updateUrl = updateUrl;
			this.appVer = appVer;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			// update(appBean, gridView, type, position, updateUrl, appVer);

			upgradeWidget(appBean, appVer, updateUrl, gridView, position);
		};
	}

	class NegativeListener implements DialogInterface.OnClickListener {

		AppBean appBean;

		public NegativeListener(AppBean appBean) {
			this.appBean = appBean;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();

		}

	}

	public void unzip(final String filePath, final String appVer,
			final int type, final AppBean appBean, final DragGridView gridView,
			final int position) {

		if (!CommonUtility.isExistSdcard()) {
			mToast.setText("内存卡不存在");
			mToast.show();
			return;
		} else {
			long sdFree = CommonUtility.getSDFreeSize() * 1024 * 1024;
			if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
				long fileSize = new File(filePath).length();
				if (sdFree < fileSize) {
					mToast.setText("存储空间不足!");
					mToast.show();
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
				dialog = CommonUtility.showLoadingDialog(mContext);
				dialog.show();
			};

			@Override
			protected String doInBackground(Void... params) {
				String installPath = null;
				try {
					String pathSandbox = Tools.getSandbox();

					// installPath = CommonUtility.unzip(new FileInputStream(
					// new File(filePath)), CommonUtility.WIDGET_SAVE_PATH,
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

					new AppBeanDao(mContext).updateAppState(appBean.getAppId(),
							appBean.getState(), appBean.getInstallPath());
					new AppBeanDao(mContext).updateAppVer(appBean.getAppId(),
							appBean.getAppVer(), installPath);
					new AppBeanDao(mContext).updateAppVer2(appBean.getAppId(),
							appVer, installPath);
					CommonUtility.saveUpdateProgress(mContext, appBean, 100);
					CommonUtility.saveUpdateStatus(mContext,
							appBean.getAppId(), false);

					EUExAppMarketEx.onCallBack.onWidgetClicked(
							appBean.getInstallPath(), appBean.getAppId(),
							appBean.getWgtAppKey());

					switch (type) {
					case 0:
						// 应用信息添加到数据库
						new AppBeanDao(mContext).addAppBean(appBean);
						break;
					case 1:
						// 更新数据库应用信息
						new AppBeanDao(mContext).deleteUpdate(appBean);
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

				AppMarketMainActivity activity = (AppMarketMainActivity) mContext;

				List<AppBean> apps = new AppBeanDao(mContext)
						.getOrderInstallApp();
				activity.setData(apps);

				switch (type) {
				case 0:
					break;
				case 1:
					launch(appBean, gridView, 1, position);
					break;
				default:
					break;
				}

			};
		}.execute();
	}

	private UpdateDownTask updateDownTask;

	private void update(final AppBean appBean, final DragGridView gridView,
			final int type, final int position, final String updateUrl,
			final String updateVer) {

		updateDownTask = new UpdateDownTask(appBean, gridView, type, position) {
			@Override
			protected Object doInBackground(Object... params) {
				Object object = super.doInBackground(updateUrl, "1");

				if (null != object && object instanceof File) {
					File file = (File) object;
					new AppBeanDao(mContext).addUpdate(appBean.getId(),
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
								gridView, position);
					}
					break;

				case 1:
					upgradeWidget(appBean, updateVer, updateUrl, gridView,
							position);
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

	public String[] getFilePahFromDownload(Context context, String url) {
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

	public void deleteUpdateFromDb(String url) {
		// DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
		// SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteDatabase db = DatabaseHelper.getDB(mContext);
		try {

			String sql = "DELETE FROM Downloader WHERE url='" + url + "'";
			// if (!db.isDbLockedByOtherThreads()) {
			db.execSQL(sql);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}

		// dbHelper = null;
	}

	public String[] getDownFileFromDb(String url) {
		// DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
		// SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteDatabase db = null;
		Cursor c = null;
		String[] result = null;
		try {
			db = DatabaseHelper.getDB(mContext);
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

	private List<AppBean> syncAppsWithDB(List<AppBean> appList,
			List<AppBean> serverList) {
		try {

			if (appList == null || appList.size() < 1) {
				new AppBeanDao(mContext).addAllAppList(serverList);
			} else {
				if (serverList == null || serverList.size() < 1) {
					return new AppBeanDao(mContext).getOrderInstallApp();
				}
				// 删除缓存在本地的下载地址
				Tools.deleteCacheLocAdd(mContext, appList, serverList);

				// 删除本地已经下架的子应用
				// List<AppBean> appListInstall = new AppBeanDao(mContext)
				// .getOrderInstallApp();
				// 本地appid
				List<String> listInstall = Tools.getList(new AppBeanDao(
						mContext).getOrderInstallApp());
				// 网络appid
				List<String> listNet = Tools.getList(serverList);

				// 未下载列表数据
				// List<AppBean> noDownLoadapp = new AppBeanDao(mContext)
				// .getallAppList();
				List<String> noDownLoadappList = Tools.getList(new AppBeanDao(
						mContext).getallAppList());

				Tools.deleteUnWidget(mContext, listNet, listInstall);
				// 列表
				Tools.deleteUnWidget(mContext, listNet, noDownLoadappList);
				// 如果当前服务器的版本与已安装的应用版本号相同，则更新其描述

				for (int i = 0, size = appList.size(); i < size; i++) {
					AppBean app = appList.get(i);
					int index = serverList.indexOf(app);

					if (index == -1) {
						// 服务器返回应用列表，如果列表中不含本地应用，从数据库删除当前对象
						new AppBeanDao(mContext).deleteApp(
								AppDbHelper.TABLE_APP_LIST, appList.get(i)
										.getAppId());
						new AppBeanDao(mContext).deleteApp(
								AppDbHelper.TABLE_NAME, appList.get(i)
										.getAppId());
					} else {// 如果包含此本地应用
						AppBean serverApp = serverList.get(index);
						if (app != null && serverApp != null) {
							if (!serverApp.getDownloadUrl().equals(
									app.getDownloadUrl())) {
								new AppBeanDao(mContext)
										.updateDownloadUrl(serverApp);
							}
							// 更新图标地址
							if (serverApp.getIconLoc() != null) {
								if (!serverApp.getIconLoc().equals(
										app.getIconLoc())) {
									new AppBeanDao(mContext).updateIconUrl(
											serverApp, app.getId());
								}
							}
							// 更新默认
							if (!TextUtils.isEmpty(app.getIconLoc())) {
								if (!serverApp.getIconLoc().equals(
										app.getIconLoc())) {
									new AppBeanDao(mContext).updateInstallMain(
											AppDbHelper.TABLE_APP_LIST, app);
								}
							}
							// 更新本地的子应用的描述
							if (serverApp.getDescription() != null) {
								if (!serverApp.getDescription().equals(
										app.getDescription())) {
									new AppBeanDao(mContext).updateDescription(
											serverApp, app.getId());
								}
							}

							// 将本地应用的默认版本与服务器应用版本同步，以便删除应用时恢复本地应用版本
							if (!serverApp.getAppVer().equals(app.getAppVer())) {
								new AppBeanDao(mContext)
										.updateDefaultAppVer(serverApp);
							}

							/**
							 * 将本地未下载版本更新时间与服务器上的更新时间同步
							 */
							if (serverApp.getUpdateTime() != app
									.getUpdateTime()) {
								new AppBeanDao(mContext).updateTime(serverApp);
							}

							// 更新名字
							if (!serverApp.getAppName()
									.equals(app.getAppName())) {
								new AppBeanDao(mContext)
										.updateAppName(serverApp);
							}

							if (TextUtils.isEmpty(app.getWgtAppKey())
									&& !TextUtils.isEmpty(serverApp
											.getWgtAppKey())) {
								new AppBeanDao(mContext)
										.updateWgtAppKey(serverApp);
							}

							if (app.getDefaultApp() != serverApp
									.getDefaultApp()) {
								new AppBeanDao(mContext)
										.updateDefaultState(serverApp);
							} else {
								/***
								 * 最初版本首页应用为0，剩余应用为1，现改为首页应用为2，剩余应用为3
								 */
								switch (app.getRemainApp()) {
								case AppBean.INIT_HOME_APP:
									new AppBeanDao(mContext).updateRemainState(
											serverApp, AppBean.NON_REMAIN_APP);
									break;
								case AppBean.INIT_REMAIN_APP:
									new AppBeanDao(mContext).updateRemainState(
											serverApp, AppBean.REMAIN_APP);
									break;
								default:
									break;
								}
							}

						}
					}
				}

				for (int i = 0, size = serverList.size(); i < size; i++) {
					AppBean serverApp = serverList.get(i);
					int index = appList.indexOf(serverApp);
					if (index == -1) {
						// 如果本地應用列表不含有服務器返回的應用，添加服務器应用到数据库
						// 服务器新增应用标识为“新” 标识 1，
						serverList.get(i).setNewApp(1);
						new AppBeanDao(mContext).addApp(
								AppDbHelper.TABLE_APP_LIST, serverList.get(i));
					}
				}
			}
			List<AppBean> list = new AppBeanDao(mContext).getAppList();

			List<AppBean> localApps = new AppBeanDao(mContext)
					.getInstallAppList();
			if (localApps != null && list != null) {
				for (int i = 0, size = localApps.size(); i < size; i++) {
					int index = list.indexOf(localApps.get(i));
					if (index > -1
							&& AppBean.STATE_DOWNLOADED != list.get(index)
									.getState()) {
						new AppBeanDao(mContext).updateAppState(localApps
								.get(i).getId(), localApps.get(i).getState(),
								localApps.get(i).getInstallPath());
					}
				}
			}

			// 第一版：判断是否是第一次登录进入
			// Prefence pre = new Prefence(mContext);
			// if (!pre.getBoolean("isFirst")) {
			// pre.addBoolean("isFirst", true);

			// 第二版：不需要判断第一次登录，只要有默认选项，则表示子应用已经下载到本地，门户只是将该目录下的子应用显示出来
			// List<AppBean> allList = new AppBeanDao(mContext).getAppList();
			for (int i = 0; i < serverList.size(); i++) {
				AppBean app = serverList.get(i);
				boolean b = Boolean.parseBoolean(app.getMainApp());
				if (b) {
					if (!new AppBeanDao(mContext).isInstall(app)) {
						// 当本地已下载中没有没有服务器中默认的子应用的时候则添加到本地已下载的列表中
						new AppBeanDao(mContext).addDefaultAppBean(app);// /widgetone/widgets/aaaah10031/
						String pathSandbox = Tools.getSandbox();
						File file = new File(pathSandbox);
						// File dir = Environment
						// .getExternalStorageDirectory();
						// File file = new File(dir + "/widgetone/widgets");

						if (!file.exists()) {
							file.mkdirs();
						}
						new AppBeanDao(mContext).addUpdate(app.getId(),
								app.getAppId(), app.getAppName(),
								app.getAppVer(), "", file.getAbsolutePath()
										+ "/" + app.getAppId());
					}
				}

			}

			// 检查，当本地默认的子应用在服务器中是非默认的时候，则在已下载列表中修改状态，
			List<AppBean> allInstall = new AppBeanDao(mContext)
					.getInstallAppList();

			for (int i = 0; i < allInstall.size(); i++) {
				AppBean install = allInstall.get(i);
				if (serverList != null) {
					for (int j = 0; j < serverList.size(); j++) {
						AppBean server = serverList.get(j);
						if (install.getAppId().trim().equals(server.getAppId())) {
							new AppBeanDao(mContext).updateInstallMain(
									AppDbHelper.TABLE_NAME, server);
						}
					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		List<AppBean> LISTAPP = new AppBeanDao(mContext).getOrderInstallApp();

		// 如果
		return LISTAPP;
	}

	public void remove(AppBean appBean) {
		if (appBean == null) {
			return;
		}

		removeTask(appBean);

		new AppBeanDao(mContext).deleteDownFileWithDB(appBean);
		new AppBeanDao(mContext).deleteDefaultApp(appBean);
		new AppBeanDao(mContext).deleteDownloadApp(appBean);
	}

	/**
	 * 取消下载任务
	 * 
	 * @param appBean
	 */
	private void removeTask(AppBean appBean) {
		UpdateTaskList updateTaskList = ((AppMarketMainActivity) mContext)
				.getAppsTaskList();
		for (int i = 0, size = updateTaskList.size(); i < size; i++) {
			UpdateDownTask task = updateTaskList.getTask(i);
			if (task != null && appBean.getId().equals(task.getTaskId())) {
				updateTaskList.getTask(i).setWait(false);
				updateTaskList.getTask(i).cancel(true);
			}
		}
	}

	public void unzip2(final String filePath, final String appVer,
			final int type, final AppBean appBean, final GridView gridView,
			final int position) {

		if (!CommonUtility.isExistSdcard()) {
			mToast.setText("内存卡不存在");
			mToast.show();
			return;
		} else {
			long sdFree = CommonUtility.getSDFreeSize() * 1024 * 1024;
			if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
				long fileSize = new File(filePath).length();
				if (sdFree < fileSize) {
					mToast.setText("存储空间不足!");
					mToast.show();
					return;
				}
			}
		}

		if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
			return;
		}

		// UnzipTask task = new UnzipTask(filePath, appVer, type, appBean,
		// gridView, position);

	}

	class UnzipTask extends AsyncTask<String, Void, Object> {
		private boolean wait = false;
		private String filePath;
		private String appVer;
		private int type;
		private AppBean appBean;
		final DragGridView gridView;
		private int position;

		public UnzipTask(String filePath, String appVer, int type,
				AppBean appBean, DragGridView gridView, int position) {
			super();
			this.filePath = filePath;
			this.appVer = appVer;
			this.type = type;
			this.appBean = appBean;
			this.gridView = gridView;
			this.position = position;
		}

		public boolean isWait() {
			return wait;
		}

		public void setWait(boolean wait) {
			this.wait = wait;
		}

		Dialog dialog;

		protected void onPreExecute() {
			dialog = CommonUtility.showLoadingDialog(mContext);
			dialog.show();
		};

		@Override
		protected Object doInBackground(String... params) {
			String installPath = null;
			try {

				String pathSandbox = Tools.getSandbox();

				// installPath = CommonUtility.unzip(new FileInputStream(new
				// File(
				// filePath)), CommonUtility.WIDGET_SAVE_PATH, null);
				installPath = CommonUtility.unzip(new FileInputStream(new File(
						filePath)), pathSandbox, null);
				if (TextUtils.isEmpty(installPath)
						|| !new File(installPath).exists()) {
					return null;
				}

				appBean.setState(1);
				appBean.setInstallPath(installPath);

				new File(filePath).delete();
				deleteUpdateFromDb(appBean.getDownloadUrl());
				new AppBeanDao(mContext).updateAppVer(appBean.getAppId(),
						appBean.getAppVer(), installPath);
				new AppBeanDao(mContext).updateAppVer2(appBean.getAppId(),
						appVer, installPath);

				switch (type) {
				case 0:
					// 应用信息添加到数据库
					new AppBeanDao(mContext).addAppBean(appBean);
					// new AppBeanDao(mContext).updateAppState(appBean.getId(),
					// appBean.getState(), appBean.getInstallPath());

					break;
				case 1:
					// 更新数据库应用信息
					new AppBeanDao(mContext).deleteUpdate(appBean);
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

			switch (type) {
			case 0:
				AppsListAdapter adapter = (AppsListAdapter) gridView
						.getAdapter();
				if (adapter != null) {
					adapter.updateViewProgress(position, 100, true, true);
				}
				break;
			case 1:
				launch(appBean, gridView, 1, position);
				break;
			default:
				break;
			}

		};

	}

	private LinkedList<AsyncTask<Void, Void, Object>> unzipTaskList = new LinkedList<AsyncTask<Void, Void, Object>>();

	void addTask(AsyncTask<Void, Void, Object> task) {
		if (unzipTaskList.size() > 0) {

		}
		unzipTaskList.add(task);
	}

	public void openMore() {
		// TODO Auto-generated method stub
		EUExAppMarketEx.onCallBack.onClickMore();
	}

	private void upgradeWidget(AppBean appBean, String appVer,
			String updateUrl, DragGridView gridView, int position) {
		AppBean app = appBean;
		app.setAppVer(appVer);
		app.setDownloadUrl(updateUrl);
		app.setState(AppBean.STATE_UNLOAD);
		// app.setAppId(appBean.getAppId());
		new AppBeanDao(mContext).updateAppState(appBean.getId(),
				AppBean.STATE_UNLOAD, appBean.getInstallPath());
		// 0 表示关闭
		EUExAppMarketEx.onCallBack.onfinishWidget(appBean.getAppId());
		launch(app, gridView, 0, position);
	}
}
