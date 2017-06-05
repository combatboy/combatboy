package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.DateFormat;

import android.text.TextUtils;
import android.util.Log;

public class AppListJsonParser {

	public static final String JK_ID = "id";
	public static final String JK_NAME = "name";
	public static final String JK_TYPE = "appCategory";
	public static final String JK_ICON_LOC = "iconLoc";
	public static final String JK_ICON = "icon";
	public static final String JK_DOWNLOAD_URL = "pkgUrl";
	public static final String JK_MAIN_APP = "greatApp";
	public static final String JK_WGT_APP_ID = "wgtAppId";
	public static final String JK_ANDROID_PKG_NAME = "pkgName";
	// public static final String JK_ANDROID_PKG_URL = "androidPkgUrl";
	public static final String JK_DEFAULT_LIST = "appList";
	public static final String JK_REMAIN_LIST = "remainList";
	public static final String JK_DEFAULT_APP = "greatApp";
	public static final String JK_APPLYDEFAULT_APP = "applyDefault";

	public static final String JV_TYPE_WAP = "AppCanWap";
	public static final String JV_TYPE_NATIVE = "AppCanNative";
	public static final String JV_TYPE_WIDGET = "AppCanWgt";
	public static final String JV_NATIVE = "Native";
	// public static final String JV_PACKAGE_NAME = "packageName";
	public static final String JV_WGTAPPID = "appId";
	public static final String JV_WGT_VERSION = "curVersion";
	private static final String JK_WGT_APP_KEY = "appKey";
	private static final String JK_UP_TIME = "createdTime"; // 上架时间
	private static final String JK_UPDATE_TIME = "updateTime";

	public static final String APPID = CommonUtility.appIdArray;

	public static final String JK_WGT_SIZE = "pkgSize";
	public static final String JK_WGT_DESCRIPTION = "description";
	public static final String JK_WGT_POSITION = "position";

	public static List<AppBean> parseJsonArray(String key, String json) {
		if (TextUtils.isEmpty(json) || TextUtils.isEmpty(key)) {
			return null;
		}

		// if (json.contains("\\")) {
		// json = json.replaceAll("\\", "");
		// }
		String[] AppIdArray = APPID.split(",");
		List<AppBean> list = null;
		try {
			JSONObject obj = new JSONObject(json);

			JSONArray array = obj.optJSONArray(key);
			if (array == null || array.length() == 0) {
				return null;
			}

			list = new ArrayList<AppBean>();
			for (int i = 0, size = array.length(); i < size; i++) {
				JSONObject jsonItem = array.getJSONObject(i);
				String category = jsonItem.optString(JK_TYPE);
				// for (int j = 0; j < AppIdArray.length; j++) {
				// if (AppIdArray[j].trim().equals(
				// jsonItem.getString(JV_WGTAPPID))) {
				AppBean appBean = new AppBean();
				if (JV_TYPE_NATIVE.equals(category)) {
					appBean.setType(AppBean.TYPE_NATIVE);
				} else if (JV_TYPE_WAP.equals(category)) {
					appBean.setType(AppBean.TYPE_WAP);
				} else if (JV_TYPE_WIDGET.equals(category)) {
					appBean.setType(AppBean.TYPE_WIDGET);
				} else if (JV_NATIVE.equals(category)) {
					appBean.setType(AppBean.TYPE_NATIVE);
				} else {
					continue;
				}

				appBean.setAppId(jsonItem.optString(JV_WGTAPPID));
				appBean.setAppVer(jsonItem.optString(JV_WGT_VERSION));
				appBean.setDefaultAppVer(jsonItem.optString(JV_WGT_VERSION));
				appBean.setId(jsonItem.optString(JV_WGTAPPID));
				appBean.setAppName(jsonItem.optString(JK_NAME));
				appBean.setDownloadUrl(jsonItem.optString(JK_DOWNLOAD_URL));
				Log.i(EUExAppMarketEx.TAG,
						jsonItem.optString(JK_DOWNLOAD_URL)
								+ "====appBean.getDownloadUrl()====>>>>"
								+ appBean.getDownloadUrl());
				// appBean.setDownloadUrl(jsonItem.optString(JK_ANDROID_PKG_URL));
				// appBean.setIconLoc(jsonItem.optString(JK_ICON_LOC));
				// appBean.setIconLoc("http://avatar.csdn.net/9/C/0/1_mfc5158.jpg");
				appBean.setMainApp(jsonItem.optString(JK_MAIN_APP));
				appBean.setWgtAppId(jsonItem.optString(JK_WGT_APP_ID));
				appBean.setDefaultApp(jsonItem.optBoolean(JK_DEFAULT_APP));
				appBean.setDescription(jsonItem.optString(JK_WGT_DESCRIPTION));
				appBean.setPkgSize(jsonItem.optLong(JK_WGT_SIZE));
				appBean.setUpdateTime(DateFormat.getDate(jsonItem
						.getString(JK_UPDATE_TIME)));
				String uptime = jsonItem.getString(JK_UP_TIME);

				appBean.setUpTime(uptime);
				appBean.setPosition(100);
				boolean b = jsonItem.getBoolean(JK_APPLYDEFAULT_APP);
				if (b) {
					boolean def = jsonItem.optBoolean(JK_DEFAULT_APP);
					if (def) {
						appBean.setDefaultApp(def);
					} else {
						appBean.setDefaultApp(false);
					}

				} else {
					appBean.setDefaultApp(false);
				}

				// boolean b = jsonItem.getBoolean(JK_APPLYDEFAULT_APP);
				if (b) {
					appBean.setApplyDefault("0");

				} else {
					appBean.setApplyDefault("1");
				}
				appBean.setDefaultApp(appBean.isDefaultApp() ? AppBean.DEFAULT_APP
						: AppBean.NON_DEFAULT_APP);

				appBean.setRemainApp(appBean.isDefaultApp() ? AppBean.NON_REMAIN_APP
						: AppBean.REMAIN_APP);

				appBean.setWgtAppKey(jsonItem.optString(JK_WGT_APP_KEY));
				if (appBean.getType() == AppBean.TYPE_WAP) {
					// wap应用不用下载
					appBean.setState(AppBean.STATE_DOWNLOADED);
				} else {
					// 其他应用默认没下载
					appBean.setState(AppBean.STATE_UNLOAD);
				}
				if (appBean.getType() == AppBean.TYPE_NATIVE) {
					// appBean.setPackageName(jsonItem.optString(JV_PACKAGE_NAME));
					appBean.setPackageName(jsonItem
							.optString(JK_ANDROID_PKG_NAME));
				}

				if (JK_DEFAULT_LIST.equals(key)) {
					appBean.setDefaultApp(AppBean.DEFAULT_APP);
				}

				if (JK_REMAIN_LIST.equals(key)) {
					appBean.setRemainApp(AppBean.REMAIN_APP);
				}

				if (jsonItem.has("iconLoc")) {
					String iconLoc = jsonItem.optString(JK_ICON_LOC, "");
					if (!TextUtils.isEmpty(iconLoc)) {
						appBean.setIconLoc(iconLoc);
						Log.i("icon", "iconLoc==1111==" + iconLoc);
					} else {
						JSONArray jsona = jsonItem.getJSONArray("tileList");
						for (int j = 0; j < jsona.length(); j++) {
							JSONObject jsono = jsona.getJSONObject(j);
							String icon1 = jsono.optString(JK_ICON, "");
							appBean.setIconLoc(icon1);
							Log.i("icon", "iconLoc==222==" + icon1);
						}
					}
				}

				// if (jsonItem.has("tileList")) {
				//
				// int count = jsona.length();
				// if (count != 0) {
				// for (int j = 0; j < jsona.length(); j++) {
				// JSONObject jsono = jsona.getJSONObject(j);
				// appBean.setIconLoc(jsono.getString(JK_ICON));
				// }
				// } else {
				// appBean.setIconLoc(jsonItem.optString(JK_ICON_LOC));
				// }
				// } else {
				// appBean.setIconLoc(jsonItem.optString(JK_ICON_LOC));
				// }

				list.add(appBean);
			}
			// }
			// }

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<AppBean> parseDefaultList(String json) {
		return parseJsonArray(JK_DEFAULT_LIST, json);
	}

	public static List<AppBean> parseRemainList(String json) {
		return parseJsonArray(JK_REMAIN_LIST, json);
	}
}
