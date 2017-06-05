package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class PackageInstalledReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		AppMarketMainActivity activity = AppMarketMainActivity.getInstance();
		String packageName = intent.getDataString();
		if (TextUtils.isEmpty(packageName)) {
			return;
		}

		if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {

			remove(activity, packageName);

		}

		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

			return;
		}

	}

	private void remove(AppMarketMainActivity activity, String packageName) {

		if (packageName.contains(":")) {
			String[] pkgInfo = packageName.split(":");
			if (pkgInfo != null && pkgInfo.length > 1) {
				String pkg = pkgInfo[1];
				// activity.getAppPagerAdapter().remove(pkg);
			}
		}
	}

}
