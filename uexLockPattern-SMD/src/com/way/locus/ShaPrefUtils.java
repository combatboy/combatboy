package com.way.locus;

import android.content.Context;
import android.content.SharedPreferences;

public class ShaPrefUtils {
	public static String getCheckPasswork(Context mContext, String fileName,
			String keyName) {
		SharedPreferences preferences = mContext.getSharedPreferences(fileName,
				0);
		String initDataCount = preferences.getString(keyName, null);
		return initDataCount;
	}

	public static void setCheckPassword(Context mContext, String intStrCount,
			String fileName, String keyName) {
		SharedPreferences.Editor token = mContext.getSharedPreferences(
				fileName, 0).edit();
		token.putString(keyName, intStrCount);
		token.commit();
	}

}
