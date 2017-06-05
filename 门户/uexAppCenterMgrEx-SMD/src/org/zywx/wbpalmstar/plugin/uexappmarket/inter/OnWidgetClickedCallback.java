package org.zywx.wbpalmstar.plugin.uexappmarket.inter;

public interface OnWidgetClickedCallback {

	void onWidgetClicked(String installPath, String widgetId, String widgetKey);

	/**
	 * 打开原生应用的回调
	 * 
	 * @param json
	 */
	void onStartNativeWight(String json);

	void onStartWap(String appId, String url);

	/**
	 * 点击更多时候的回调
	 */
	void onClickMore();

	/**
	 * 当磁贴的高度发生变化的时候
	 * 
	 * @param height
	 */
	void onHeightChanged(int height);

	/**
	 * 关闭当前打开的子应用
	 * 
	 * @param appId
	 */
	void onfinishWidget(String appId);

	/**
	 * 获取token
	 * 
	 * @param token
	 */
	void onSoftToken(String token);

	/**
	 * 当子应用被删除的时候
	 * 
	 * @param appId
	 * @param appName
	 * @param version
	 */
	void onDeleteApp(String appId, String appName, String version);

	/**
	 * 安装成功的时候的回调
	 * 
	 * @param appId
	 * @param appName
	 * @param version
	 */
	void onInstallApp(String appId, String appName, String version);

}
