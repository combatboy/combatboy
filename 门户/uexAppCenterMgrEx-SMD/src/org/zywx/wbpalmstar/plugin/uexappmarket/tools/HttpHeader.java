package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.util.Log;

public class HttpHeader {

	/**
	 * 添加验证头
	 * 
	 * @param curWData
	 *            当前widgetData
	 * @param timeStamp
	 *            当前时间戳
	 * @return
	 */
	public static String getAppVerifyValue(WWidgetData curWData, long timeStamp) {
		String value = null;
		String md5 = getMD5Code(EUExAppMarketEx.appId + ":"
				+ EUExAppMarketEx.appKey + ":" + timeStamp);
		// String md5 =
		// getMD5Code("aaaio10008:f617f351-308f-4b1c-b7e5-7344f9ec0026:"+timeStamp);
		value = "md5=" + md5 + ";ts=" + timeStamp + ";";
		return value;
	}

	public static String getMD5Code(String value) {
		if (value == null) {
			value = "";
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(value.getBytes());
			byte[] md5Bytes = md.digest();
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16)
					hexValue.append("0");
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * plugin里面的子应用的appId和appkey都按照主应用为准
	 */
	public static WWidgetData getWidgetData(EBrowserView view) {
		WWidgetData widgetData = view.getCurrentWidget();
		// String indexUrl=widgetData.m_indexUrl;
		// if(widgetData.m_wgtType!=0){
		// if(indexUrl.contains("widget/plugin")){
		// return view.getRootWidget();
		// }
		// }
		return widgetData;
	}

}
