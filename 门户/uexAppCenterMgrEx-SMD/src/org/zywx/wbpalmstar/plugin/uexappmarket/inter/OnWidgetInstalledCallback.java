package org.zywx.wbpalmstar.plugin.uexappmarket.inter;

public interface OnWidgetInstalledCallback {
	/**
	 * 判断时候安装
	 * 
	 * @param appId
	 * @param status
	 */
	void onIsAppInstalled(String appId, int status);
}
