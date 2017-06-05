package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnDateBack;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class SendPost {
	Context mContext;
	OnDateBack cellBack;
	ProgressDialog progressDialog = null;

	public SendPost(Context context, OnDateBack cellBack) {
		this.mContext = context;
		this.cellBack = cellBack;

	}

	public boolean performAsyncLoadAppListAction(final String mRequestUrl) {
		if (!CommonUtility.isOnline(mContext)) {
			return true;
		}

		new AsyncTask<Object, Void, List<AppBean>>() {

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(mContext, null,
						"加载应用列表...", false, true,
						new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								cancel(true);
							}
						});
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
				List<AppBean> installList = new AppBeanDao(mContext)
						.getInstallAppList();
				Log.i(EUExAppMarketEx.TAG, "========apps=====" + apps.size());
				return syncAppsWithDB(appList, apps, installList);
			}

			@Override
			protected void onPostExecute(List<AppBean> result) {
				if (isCancelled()) {
					return;
				}
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
				cellBack.getDate();

				// }

			}
		}.execute(new Object[] {});
		return true;
	}

	private List<AppBean> syncAppsWithDB(List<AppBean> appList,
			List<AppBean> serverList, List<AppBean> installList) {
		if (appList == null || appList.size() < 1) {
			new AppBeanDao(mContext).addAllAppList(serverList);
		} else {
			if (serverList == null || serverList.size() < 1) {
				return new AppBeanDao(mContext).getallAppList();
			}

			// for (int i = 0; i < appList.size(); i++) {
			// AppBean appBeanLoc = appList.get(i);
			// String locAppId = appBeanLoc.getAppId();
			// // String appLocDownloadUrl = appBeanLoc.getDownloadUrl();
			// for (int j = 0; j < serverList.size(); j++) {
			// AppBean appBeanNet = serverList.get(j);
			// String netAppId = appBeanNet.getAppId();
			// if (locAppId.equals(netAppId)) {
			// String appNetDownloadUrl = appBeanNet.getDownloadUrl();
			//
			// // if (appNetDownloadUrl != null
			// // && !appNetDownloadUrl
			// // .equals(appLocDownloadUrl)) {
			//
			// AppBeanDao appBeanDao = new AppBeanDao(mContext);
			// appBeanDao.updateDownloadUrl(AppDbHelper.TABLE_NAME,
			// netAppId, appNetDownloadUrl);
			// appBeanDao.updateDownloadUrl(
			// AppDbHelper.TABLE_APP_LIST, netAppId,
			// appNetDownloadUrl);
			//
			// appBeanDao.updateDownloadLoc(AppDbHelper.TABLE_NAME,
			// netAppId, appBeanNet.getIconLoc());
			// appBeanDao.updateDownloadLoc(
			// AppDbHelper.TABLE_APP_LIST, netAppId,
			// appBeanNet.getIconLoc());
			// // }
			// }
			// }
			// }
			Tools.deleteCacheLocAdd(mContext, appList, serverList);

			// 删除本地已经下架的子应用
			/**
			 * new AppBeanDao(mContext).deleteApp( AppDbHelper.TABLE_NAME,
			 * appList.get(i) .getAppId());
			 * */
			List<AppBean> appListInstall = new AppBeanDao(mContext)
					.getOrderInstallApp();

			// 已下载列表数据
			// List<String> listInstall = new ArrayList<String>();
			// for (int i = 0; i < appListInstall.size(); i++) {
			// listInstall.add(appListInstall.get(i).getAppId());
			// }
			List<String> listInstall = Tools.getList(appListInstall);
			// 网络appid
			// List<String> listNet = new ArrayList<String>();
			// for (int i = 0; i < serverList.size(); i++) {
			// listNet.add(serverList.get(i).getAppId());
			// }
			List<String> listNet = Tools.getList(serverList);

			// 未下载列表数据
			List<AppBean> noDownLoadapp = new AppBeanDao(mContext)
					.getallAppList();
			// List<String> noDownLoadappList = new ArrayList<String>();
			// for (int i = 0; i < noDownLoadapp.size(); i++) {
			// noDownLoadappList.add(noDownLoadapp.get(i).getAppId());
			// }
			List<String> noDownLoadappList = Tools.getList(noDownLoadapp);

			// 删除未下载下架子应用
			// for (int k = 0; k < listInstall.size(); k++) {
			// String installAppId = listInstall.get(k);
			// if (!listNet.contains(installAppId)) {
			// Log.i("installAppId", "installAppId======" + installAppId);
			// new AppBeanDao(mContext).deleteApp2(
			// AppDbHelper.TABLE_APP_LIST, installAppId);
			// new AppBeanDao(mContext).deleteApp2(AppDbHelper.TABLE_NAME,
			// installAppId);
			// // 更新下载状态
			// CommonUtility.getProgressStatus(mContext, installAppId);
			// CommonUtility.saveProgress2(mContext, installAppId, 0);
			// }
			// }

			Tools.deleteUnWidget(mContext, listNet, listInstall);

			// 删除未下载下架子应用
			// for (int h = 0; h < noDownLoadappList.size(); h++) {
			// String noDownLoadAppId = noDownLoadappList.get(h);
			// if (!listNet.contains(noDownLoadAppId)) {
			// Log.i("installAppId", "noDownLoadAppId======"
			// + noDownLoadAppId);
			// new AppBeanDao(mContext).deleteApp2(
			// AppDbHelper.TABLE_APP_LIST, noDownLoadAppId);
			// new AppBeanDao(mContext).deleteApp2(AppDbHelper.TABLE_NAME,
			// noDownLoadAppId);
			// // 更新下载状态
			// CommonUtility.getProgressStatus(mContext, noDownLoadAppId);
			// CommonUtility.saveProgress2(mContext, noDownLoadAppId, 0);
			// }
			// }

			Tools.deleteUnWidget(mContext, listNet, noDownLoadappList);

			// 如果当前服务器的版本与已安装的应用版本号相同，则更新其描述

			for (int i = 0; i < installList.size(); i++) {

				for (int j = 0; j < serverList.size(); j++) {
					if (installList.get(i).getAppId().trim()
							.equals(serverList.get(j).getAppId())
							&& installList.get(i).getAppVer().trim()
									.equals(serverList.get(j).getAppVer())) {
						new AppBeanDao(mContext).updateInstallDescription(
								serverList.get(j), serverList.get(j).getId());
						new AppBeanDao(mContext).updateInstallPkgSize(
								serverList.get(j), serverList.get(j).getId());
					}
				}
			}

			for (int i = 0, size = appList.size(); i < size; i++) {
				AppBean app = appList.get(i);
				int index = serverList.indexOf(app);

				if (index == -1) {
					// 服务器返回应用列表，如果列表中不含本地应用，从数据库删除当前对象
					new AppBeanDao(mContext).deleteApp(
							AppDbHelper.TABLE_APP_LIST, appList.get(i).getId());
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
							if (!serverApp.getIconLoc()
									.equals(app.getIconLoc())) {
								new AppBeanDao(mContext).updateIconUrl(
										serverApp, app.getId());
							}
						}

						// 更新默认
						if (!TextUtils.isEmpty(app.getIconLoc())) {

							if (!serverApp.getIconLoc()
									.equals(app.getIconLoc())) {
								new AppBeanDao(mContext).updateInstallMain(
										AppDbHelper.TABLE_APP_LIST, app);
							}
						}

						// 更新本地的子应用的描述
						if (!TextUtils.isEmpty(serverApp.getDescription())) {
							if (!serverApp.getDescription().equals(
									app.getDescription())) {
								new AppBeanDao(mContext).updateDescription(
										serverApp, app.getId());
							}
						}

						// 将本地应用的默认版本与服务器应用版本同步，以便删除应用时恢复本地应用版本
						if (TextUtils.isEmpty(serverApp.getAppVer())) {

							if (!serverApp.getAppVer().equals(app.getAppVer())) {
								new AppBeanDao(mContext)
										.updateDefaultAppVer(serverApp);
							}
						}

						/**
						 * 将本地未下载版本更新时间与服务器上的更新时间同步
						 */
						if (serverApp.getUpdateTime() != app.getUpdateTime()) {
							new AppBeanDao(mContext).updateTime(serverApp);
						}

						// 更新名字
						if (TextUtils.isEmpty(serverApp.getAppName())) {

							if (!serverApp.getAppName()
									.equals(app.getAppName())) {
								new AppBeanDao(mContext)
										.updateAppName(serverApp);
							}
						}

						if (TextUtils.isEmpty(app.getWgtAppKey())
								&& !TextUtils.isEmpty(serverApp.getWgtAppKey())) {
							new AppBeanDao(mContext).updateWgtAppKey(serverApp);
						}
						// 修改文件大小
						if (serverApp.getPkgSize() != app.getPkgSize()) {
							new AppBeanDao(mContext).updateAppSize(serverApp);
						}

						if (app.getDefaultApp() != serverApp.getDefaultApp()) {
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
					new AppBeanDao(mContext).addApp(AppDbHelper.TABLE_APP_LIST,
							serverList.get(i));
				}
			}
		}
		List<AppBean> list = new AppBeanDao(mContext).getAppList();

		List<AppBean> localApps = new AppBeanDao(mContext).getInstallAppList();
		if (localApps != null && list != null) {
			for (int i = 0, size = localApps.size(); i < size; i++) {
				int index = list.indexOf(localApps.get(i));
				if (index > -1
						&& AppBean.STATE_DOWNLOADED != list.get(index)
								.getState()) {
					new AppBeanDao(mContext).updateAppState(localApps.get(i)
							.getId(), localApps.get(i).getState(), localApps
							.get(i).getInstallPath());
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
					new AppBeanDao(mContext).addDefaultAppBean(app);// /
					// widgetone/widgets/aaaah10031/
					String pathSandbox = Tools.getSandbox();
					File file = new File(pathSandbox);
					// File dir = Environment.getExternalStorageDirectory();
					// File file = new File(dir + "/widgetone/widgets");

					if (!file.exists()) {
						file.mkdirs();
					}
					new AppBeanDao(mContext).addUpdate(app.getId(),
							app.getAppId(), app.getAppName(), app.getAppVer(),
							"", file.getAbsolutePath() + "/" + app.getAppId());
					String[] s = new AppBeanDao(mContext).getUpdate1(app);
				}
			}

		}

		// 检查，当本地默认的子应用在服务器中是非默认的时候，则在已下载列表中修改状态，
		List<AppBean> allInstall = new AppBeanDao(mContext).getInstallAppList();

		for (int i = 0; i < allInstall.size(); i++) {
			AppBean install = allInstall.get(i);
			if (serverList != null) {
				for (int j = 0; j < serverList.size(); j++) {
					AppBean server = serverList.get(j);
					if (install.getAppId().trim().equals(server.getAppId())) {
						// if (Boolean.parseBoolean(server.getMainApp())==false
						// && Boolean.parseBoolean(install.getMainApp())==true)
						// {

						new AppBeanDao(mContext).updateInstallMain(
								AppDbHelper.TABLE_NAME, server);
						// }
					}
				}
			}

		}

		List<AppBean> LISTAPP = new AppBeanDao(mContext).getallAppList();

		// 如果
		return LISTAPP;
	}

	public void closeDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
}
