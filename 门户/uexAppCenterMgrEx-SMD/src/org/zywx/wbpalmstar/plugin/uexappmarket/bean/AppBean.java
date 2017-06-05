package org.zywx.wbpalmstar.plugin.uexappmarket.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AppBean implements Parcelable {

	public int flag = 0;

	public static final int TYPE_WAP = 8;
	public static final int TYPE_WIDGET = 1;
	public static final int TYPE_NATIVE = 7;

	// 未下载
	public static final int STATE_UNLOAD = 0;
	// 下载完毕
	public static final int STATE_DOWNLOADED = 1;
	// 下载中
	public static final int STATE_DOWNLOADING = 2;
	// 下载失败
	public static final int STATE_DOWNLOAD_FAIL = 3;

	public static final int DEFAULT_APP = 1;
	public static final int NON_DEFAULT_APP = 0;

	/**
	 * 正式版剩余应用标识
	 */
	public static final int REMAIN_APP = 3;
	/**
	 * 正式版本首页应用标识
	 */
	public static final int NON_REMAIN_APP = 2;
	/**
	 * 测试版本首页应用标识
	 */
	public static final int INIT_HOME_APP = 0;
	/**
	 * 测试版剩余应用标识
	 */
	public static final int INIT_REMAIN_APP = 1;
	//
	private String id;

	// 应用ID
	private String appId;
	// 类型
	private int type;
	// 应用名称
	private String appName;
	// 图标链接
	private String iconLoc;
	// 应用地址
	private String downloadUrl;
	// 应用状态
	private int state;
	// 安装路径
	private String installPath;
	//
	private String packageName;
	//
	private String appVer;
	//
	private String certificatesPath;
	//
	private String certificatesPwd;
	//
	private String certificatesUrl;
	// 是否主应用
	private String mainApp;

	private int versionCode;

	private String wgtAppId;

	private String applyDefault; // 默认 非默认

	private boolean greatApp; // 精品 。非精品

	private long pkgSize; // 文件大小

	private String description; // 文件描述

	private int position; // 显示位置

	private String upTime; // 上架时间
	private long updateTime;

	/**
	 * 应用版本，用于备份应用版本号，如果删除默认应用恢复此版本号
	 */
	private String defaultAppVer;

	/**
	 * 1为默认应用，0为非默认应用，-1为默认值，旧版本里没有值
	 */
	private int defaultApp = -1;

	/**
	 * 1为默认应用，0为非默认应用，-1为默认值，旧版本里没有值
	 */
	private int remainApp = -1;
	private int homeApp = -1;

	private int newApp;// 服务器新增应用标识

	private boolean isDefaultApp;

	/** 旧应用是否和新应用是同一对象 */
	private boolean isContain = true;

	/**
	 * appkey
	 */
	private String wgtAppKey;

	/**
	 * 添加应用的时间
	 */
	private long modifyTime;

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpTime() {
		return upTime;
	}

	public void setUpTime(String upTime) {
		this.upTime = upTime;
	}

	public long getPkgSize() {
		return pkgSize;
	}

	public void setPkgSize(long pkgSize) {
		this.pkgSize = pkgSize;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean isGreatApp() {
		return greatApp;
	}

	public void setGreatApp(boolean greatApp) {
		this.greatApp = greatApp;
	}

	public String getApplyDefault() {
		return applyDefault;
	}

	public void setApplyDefault(String applyDefault) {
		this.applyDefault = applyDefault;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCertificatesUrl() {
		return certificatesUrl;
	}

	public void setCertificatesUrl(String certificatesUrl) {
		this.certificatesUrl = certificatesUrl;
	}

	public String getAppVer() {
		return appVer;
	}

	public void setAppVer(String appVer) {
		this.appVer = appVer;
	}

	public String getCertificatesPath() {
		return certificatesPath;
	}

	public void setCertificatesPath(String certificatesPath) {
		this.certificatesPath = certificatesPath;
	}

	public String getCertificatesPwd() {
		return certificatesPwd;
	}

	public void setCertificatesPwd(String certificatesPwd) {
		this.certificatesPwd = certificatesPwd;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public AppBean() {

	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getIconLoc() {
		return iconLoc;
	}

	public void setIconLoc(String iconLoc) {
		this.iconLoc = iconLoc;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getInstallPath() {
		return installPath;
	}

	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}

	public void setMainApp(String mainApp) {
		this.mainApp = mainApp;
	}

	public String getMainApp() {
		return mainApp;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getWgtAppId() {
		return wgtAppId;
	}

	public void setWgtAppId(String wgtAppId) {
		this.wgtAppId = wgtAppId;
	}

	public String getDefaultAppVer() {
		return defaultAppVer;
	}

	public void setDefaultAppVer(String defaultAppVer) {
		this.defaultAppVer = defaultAppVer;
	}

	@Override
	public int hashCode() {
		return appId.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AppBean) {
			AppBean other = (AppBean) o;
			if (!TextUtils.isEmpty(other.getId())
					&& other.getId().equals(this.id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appId);
		dest.writeInt(type);
		dest.writeString(appName);
		dest.writeString(iconLoc);
		dest.writeString(downloadUrl);
		dest.writeInt(state);
		dest.writeString(installPath);
		dest.writeString(mainApp);
		dest.writeString(wgtAppId);
		dest.writeInt(defaultApp);
		dest.writeInt(remainApp);
		dest.writeInt(newApp);
		dest.writeString(wgtAppKey);
		dest.writeString(defaultAppVer);
		dest.writeLong(modifyTime);
		dest.writeString(applyDefault);
		dest.writeString(description);
		dest.writeLong(pkgSize);
		dest.writeInt(position);
		dest.writeLong(updateTime);

	}

	public AppBean(Parcel source) {
		this.appId = source.readString();
		this.type = source.readInt();
		this.appName = source.readString();
		this.iconLoc = source.readString();
		this.downloadUrl = source.readString();
		this.state = source.readInt();
		this.installPath = source.readString();
		this.mainApp = source.readString();
		this.wgtAppId = source.readString();
		this.defaultApp = source.readInt();
		this.remainApp = source.readInt();
		this.newApp = source.readInt();
		this.wgtAppKey = source.readString();
		this.defaultAppVer = source.readString();
		this.modifyTime = source.readLong();
		this.applyDefault = source.readString();
		this.description = source.readString();
		this.pkgSize = source.readLong();
		this.position = source.readInt();
		this.updateTime = updateTime;

	}

	public static final Parcelable.Creator<AppBean> CREATOR = new Creator<AppBean>() {

		@Override
		public AppBean[] newArray(int size) {
			return new AppBean[size];
		}

		@Override
		public AppBean createFromParcel(Parcel source) {
			return new AppBean(source);
		}
	};

	public void setContain(boolean isContain) {
		this.isContain = isContain;
	}

	public boolean isContain() {
		return this.isContain;
	}

	public int getDefaultApp() {
		return defaultApp;
	}

	public int getRemainApp() {
		return remainApp;
	}

	public void setRemainApp(int remainApp) {
		this.remainApp = remainApp;
	}

	public void setDefaultApp(int defaultApp) {
		this.defaultApp = defaultApp;
	}

	public void setNewApp(int newApp) {
		this.newApp = newApp;
	}

	public int getNewApp() {
		return newApp;
	}

	public boolean isDefaultApp() {
		return isDefaultApp;
	}

	public void setDefaultApp(boolean isDefault) {
		this.isDefaultApp = isDefault;
	}

	public String getWgtAppKey() {
		return wgtAppKey;
	}

	public void setWgtAppKey(String wgtAppKey) {
		this.wgtAppKey = wgtAppKey;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(long modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getHomeApp() {
		return homeApp;
	}

	public void setHomeApp(int homeApp) {
		this.homeApp = homeApp;
	}

	@Override
	public String toString() {
		return "AppBean [id=" + id + ", appId=" + appId + ", type=" + type
				+ ", appName=" + appName + ", iconLoc=" + iconLoc
				+ ", downloadUrl=" + downloadUrl + ", applyDefault="
				+ applyDefault + ", state=" + state + ", installPath="
				+ installPath + ", packageName=" + packageName + ", appVer="
				+ appVer + ", certificatesPath=" + certificatesPath
				+ ", certificatesPwd=" + certificatesPwd + ", certificatesUrl="
				+ certificatesUrl + ", mainApp=" + mainApp + ", versionCode="
				+ versionCode + ", wgtAppId=" + wgtAppId + ", defaultAppVer="
				+ defaultAppVer + ", defaultApp=" + defaultApp + ", remainApp="
				+ remainApp + ", homeApp=" + homeApp + ", newApp=" + newApp
				+ ", isDefaultApp=" + isDefaultApp + ", isContain=" + isContain
				+ ", wgtAppKey=" + wgtAppKey + ", modifyTime=" + modifyTime
				+ ",greatApp=" + greatApp + ",description=" + description
				+ ",position=" + position + ",pkgSize=" + pkgSize
				+ ",updateTime=+" + updateTime + "]";
	}

}
