package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.DONUT)
public class AppUtils {

	public static void openApp(Context context, String packageName, String key,
			String[] params) {

		try {

			PackageInfo pi = context.getPackageManager().getPackageInfo(
					packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			// resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = context.getPackageManager()
					.queryIntentActivities(resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				// String packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				// intent.addCategory(Intent.CATEGORY_LAUNCHER);
				if (params != null && !TextUtils.isEmpty(key)) {

					intent.putExtra(key, params);
				}

				ComponentName cn = new ComponentName(
						ri.activityInfo.packageName, className);
				intent.setComponent(cn);
				context.startActivity(intent);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void openApp(Context context, String packageName,
			Map<String, Object> map) {

		try {

			PackageInfo pi = context.getPackageManager().getPackageInfo(
					packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			// resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
			List<ResolveInfo> apps = context.getPackageManager()
					.queryIntentActivities(resolveIntent, 0);
			ResolveInfo ri = apps.iterator().next();
			if (ri != null) {
				// String packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
				Intent intent = new Intent(Intent.ACTION_MAIN);
				// intent.addCategory(Intent.CATEGORY_LAUNCHER);

				if (map != null) {
					for (Entry<String, Object> entry : map.entrySet()) {
						String key = entry.getKey();
						Object o = entry.getValue();
						if (o instanceof String) {
							intent.putExtra(key, (String) o);
						} else if (o instanceof String[]) {
							String[] strArray = (String[]) o;
							intent.putExtra(key, strArray);
						} else if (o instanceof ArrayList<?>) {
							ArrayList<String> list = (ArrayList<String>) o;
							intent.putStringArrayListExtra(key, list);
						}
					}
				}

				ComponentName cn = new ComponentName(
						ri.activityInfo.packageName, className);
				intent.setComponent(cn);
				context.startActivity(intent);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 启动浏览器打开Widget(Wap和Widget模式)
	 * 
	 * @param url
	 */

	public static boolean isInstalled(Context context, String packagename) {

		if (TextUtils.isEmpty(packagename)) {
			return false;
		}
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packagename, 0);

		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			return false;
		} else {
			return true;
		}
	}

}
