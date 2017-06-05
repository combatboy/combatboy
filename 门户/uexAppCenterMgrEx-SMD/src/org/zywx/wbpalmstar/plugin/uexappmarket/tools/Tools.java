package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppDbHelper;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class Tools {

	private static final boolean DEBUG = true;
	private static final String TAG = "EUExAppMarketEx";
	private static final String appShop = "先生应用商店门户插件=====>>>";

	// public static final boolean XS_SHORSD = true; // true放在沙箱里面，false在SD卡，里面

	public static final int ITEM_HEIGHT = 4;

	// public static final int ITEM_NUMBER = 4;

	/**
	 * 用于判断当前URL 地址
	 */
	public static boolean getURLStatus(String url) {
		if (!(TextUtils.isEmpty(url)) && url.startsWith("http")) {
			return true;
		}
		return false;
	}

	/**
	 * 获取SoftToken
	 * 
	 * @return
	 */
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public static String getSoftToken(Context context) {

		SharedPreferences preferences = context.getSharedPreferences("app",
				Context.MODE_WORLD_READABLE);
		if (DEBUG) {
			return "62f859682d8195f6f5190c7679f54b24";
		} else {
			return preferences.getString("softToken", null);
		}
	}

	/***
	 * 获取数据
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static String getRequest(Context context, String url) {

		String response = "";
		Log.i(EUExAppMarketEx.TAG, "url===  " + url);

//		if (DEBUG) {
//			response = Tools.getData(context);
//		} else {
			response = CommonUtility.sendHttpRequestByGet(url, context);
//		}

		Log.e(EUExAppMarketEx.TAG, "response===  " + response);
		return response;
	}

	/**
	 * 读取本地数据
	 * 
	 * @param context
	 * @return
	 */
	public static String getData(Context context) {
		String response = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					context.getAssets().open("Data")));
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				response = sb.toString();
			} catch (IOException e) {

				e.printStackTrace();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.e("==请求的数据=", "" + response);
		return response;
	}

	public static void DebugI(String tag, String context) {
		if (!TextUtils.isEmpty(tag)) {
			tag = TAG + tag;
		}
		Log.i(tag, appShop + context);
	}

	/**
	 * 沙箱路径
	 * */
	public static String getSandbox() {
		String path = BUtility.makeRealPath("wgts://",
				EUExAppMarketEx.widgetData.getWidgetPath(),
				EUExAppMarketEx.widgetData.m_wgtType);
		Tools.DebugI("aaaaa", "getSandbox====path==" + path);
		return path;
	}

	public static String getTemp(Context mContext) {
		SharedPreferences preferences = mContext.getSharedPreferences(
				"com.temp", 0);
		String initDataCount = preferences.getString("com.value", "");
		return initDataCount;
	}

	public static void setTemp(Context mContext) {
		SharedPreferences.Editor token = mContext.getSharedPreferences(
				"com.temp", 0).edit();
		token.putString("com.value", "0");
		token.commit();
	}

	/**
	 * 转换list集合
	 * */
	public static List<String> getList(List<AppBean> appListInstall) {
		List<String> listInstall = new ArrayList<String>();
		for (int i = 0; i < appListInstall.size(); i++) {
			listInstall.add(appListInstall.get(i).getAppId());
		}
		return listInstall;
	}

	/**
	 * 删除下架子应用
	 * */

	public static void deleteUnWidget(Context mContext, List<String> listNet,
			List<String> listInstall) {
		for (int k = 0; k < listInstall.size(); k++) {
			String installAppId = listInstall.get(k);
			if (!listNet.contains(installAppId)) {
				Log.i("installAppId", "installAppId======" + installAppId);
				new AppBeanDao(mContext).deleteApp2(AppDbHelper.TABLE_APP_LIST,
						installAppId);
				new AppBeanDao(mContext).deleteApp2(AppDbHelper.TABLE_NAME,
						installAppId);
				// 更新下载状态
				CommonUtility.getProgressStatus(mContext, installAppId);
				CommonUtility.saveProgress2(mContext, installAppId, 0);
			}
		}
	}

	/**
	 * 删除缓存在本地的下载地址
	 * */

	public static void deleteCacheLocAdd(Context mContext,
			List<AppBean> appList, List<AppBean> serverList) {

		for (int i = 0; i < appList.size(); i++) {
			AppBean appBeanLoc = appList.get(i);
			String locAppId = appBeanLoc.getAppId();
			// String appLocDownloadUrl = appBeanLoc.getDownloadUrl();
			for (int j = 0; j < serverList.size(); j++) {
				AppBean appBeanNet = serverList.get(j);
				String netAppId = appBeanNet.getAppId();
				if (locAppId.equals(netAppId)) {
					String appNetDownloadUrl = appBeanNet.getDownloadUrl();

					// if (appNetDownloadUrl != null
					// && !appNetDownloadUrl
					// .equals(appLocDownloadUrl)) {

					AppBeanDao appBeanDao = new AppBeanDao(mContext);
					appBeanDao.updateDownloadUrl(AppDbHelper.TABLE_NAME,
							netAppId, appNetDownloadUrl);
					appBeanDao.updateDownloadUrl(AppDbHelper.TABLE_APP_LIST,
							netAppId, appNetDownloadUrl);

					appBeanDao.updateDownloadLoc(AppDbHelper.TABLE_NAME,
							netAppId, appBeanNet.getIconLoc());
					appBeanDao.updateDownloadLoc(AppDbHelper.TABLE_APP_LIST,
							netAppId, appBeanNet.getIconLoc());
					// }
				}
			}
		}
	}

}
