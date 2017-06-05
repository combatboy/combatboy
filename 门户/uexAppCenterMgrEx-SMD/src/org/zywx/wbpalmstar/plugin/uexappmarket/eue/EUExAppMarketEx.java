package org.zywx.wbpalmstar.plugin.uexappmarket.eue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.NewMsg;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateDownTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnWidgetClickedCallback;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnWidgetInstalledCallback;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.MyAsyncTask;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.DONUT)
@SuppressWarnings("deprecation")
public class EUExAppMarketEx extends EUExBase implements
		OnWidgetInstalledCallback {

	public static final String TAG = "EUExAppMarketEx";
	private static final String CB_SOFTTOKEN = "uexAppCenterMgrEx.cbSoftToken";
	private static final String CB_START_NATIVE = "uexAppCenterMgrEx.CbStartNative";
	private static final String CB_START_WAP = "uexAppCenterMgrEx.CbStartWap";
	private static final String CB_CLICK_MORE = "uexAppCenterMgrEx.cbClickMore";
	private static final String CB_INSTALL_APP = "uexAppCenterMgrEx.cbInstallApp";
	private static final String CB_DELETE_APP = "uexAppCenterMgrEx.cbDeleteApp";
	private static final String CB_IS_INSTALL_APP = "uexAppCenterMgrEx.cbAppInstalled";
	private static final String CB_HEIGHT_CHANGE = "uexAppCenterMgrEx.cbHeightChanged";
	private static final String CB_START_WIDGET = "uexAppCenterMgrEx.startWidget";

	private View marketDecorView;
	private AppMarketMainActivity mActivity;

	// 在更新时需要传递的参数
	public static String appId;
	public static String appKey;

	public static int x, y, width, height;

	public static int webWidth, webHeight;

	public static OnWidgetClickedCallback onCallBack;

	private LocalActivityManager localAM;
	public static WWidgetData widgetData;
	public EBrowserView inParent1;

	public EUExAppMarketEx(Context context, EBrowserView inParent) {
		super(context, inParent);
		if (widgetData == null) {
			widgetData = inParent.getCurrentWidget();
		}

		if (inParent1 == null) {
			inParent1 = inParent;
		}
	}

	public void open(String[] params) {

		if (params.length < 6) {
			return;
		}

		// if (onCallBack == null) {
		setCallBack();
		// }

		try {
			x = (int) Float.parseFloat(params[0]);
			y = (int) Float.parseFloat(params[1]);

			width = (int) Float.parseFloat(params[2]);
			height = (int) Float.parseFloat(params[3]);

			Float f = getWebScale(inParent1);
			webWidth = (int) (f * width);
			webHeight = (int) (f * height);
			Tools.DebugI("EUExAppMarketEx", "width====" + width
					+ "=====height====" + height);

			String softToken = Tools.getSoftToken(mContext);
			CommonUtility.URL_APP_LIST = params[4]
					+ "store/searchAppList?iswantTiles=t&softToken=";
			CommonUtility.CHECKUPDATE_URL = params[4] + "/store/" + softToken
					+ "/checkUpdate";
			CommonUtility.Report_url = params[4] + "/store/downCntReport";
			CommonUtility.appId = mBrwView.getCurrentWidget().m_appId;
			String app = params[5];
			try {
				JSONObject json = new JSONObject(app);
				appId = json.optString("appId");
				appKey = json.optString("key");

				Log.e(TAG, "更新请求头部需要的参数:  appId=" + appId + "   appKey="
						+ appKey);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "更新头部请求数据解析时出现异常，解析失败，可能在更新时不会有提示框，导致更新失败！");
				e.printStackTrace();
			}
		} catch (NumberFormatException e) {
			errorCallback(0, 0, "参数错误！");
			e.printStackTrace();
			return;
		}

		openAppMarketOneTime(x, y, width, height);
	};

	public void openAppMarketOneTime(final int x, final int y, final int width,
			final int height) {

		if (mActivity == null) {

			((Activity) mContext).runOnUiThread(new Runnable() {

				@Override
				public void run() {

					if (mActivity != null) {
						close(null);
					}

					Intent intent = new Intent(mContext,
							AppMarketMainActivity.class);
					if (localAM == null) {

						localAM = new LocalActivityManager((Activity) mContext,
								false);
						localAM.dispatchCreate(null);
					}

					localAM.removeAllActivities();

					Window window = localAM.startActivity(
							AppMarketMainActivity.TAG, intent);
					marketDecorView = window.getDecorView();

					// RelativeLayout.LayoutParams lp = new
					// RelativeLayout.LayoutParams(
					// width, height);
					// lp.leftMargin = x;
					// lp.topMargin = y;
					// addViewToCurrentWindow(marketDecorView, lp);

					AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(
							width,height,
							x , y);
					addViewToWebView(marketDecorView, lp, AppMarketMainActivity.TAG);
					
					AppMarketMainActivity activity = (AppMarketMainActivity) localAM
							.getActivity(AppMarketMainActivity.TAG);
					activity.init();
					mActivity = activity;

				}
			});

		}
	}

	/**
	 * 设置显示消息数量
	 * 
	 * @param params
	 */
	public void showNewMsg(final String[] params) {

		if (params.length == 0) {
			return;
		}
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String s = params[0];
				Log.e(TAG, s);
				try {
					List<NewMsg> list = new ArrayList<NewMsg>();
					JSONArray jsona = new JSONArray(s);

					for (int i = 0; i < jsona.length(); i++) {
						NewMsg nm = new NewMsg();
						JSONObject json = jsona.getJSONObject(i);
						nm.setAppId(json.getString("appId"));
						nm.setNum(Integer.parseInt(json.optString("num", "0")));
						list.add(nm);
					}

					if (mActivity != null) {

						mActivity.setNewMsgNotify(list);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	public void close(String[] params) {

		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {

				if (marketDecorView != null && localAM != null) {
					removeViewFromWebView(AppMarketMainActivity.TAG);
					// removeViewFromCurrentWindow(marketDecorView);
					localAM.destroyActivity(AppMarketMainActivity.TAG, true);
				}
			}
		});

	}

	@Override
	protected boolean clean() {
		close(null);
		return true;
	}

	public void setViewFrame(final int x, final int y, final int height) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				// ActivityGroup actGroup = ((ActivityGroup) mContext);
				// WindowManager wm = actGroup.getWindowManager();
				//
				// Activity activity = actGroup.getLocalActivityManager()
				// .getActivity(AppMarketMainActivity.TAG);
				// if (activity != null) {
				// DisplayMetrics dm = new DisplayMetrics();
				// wm.getDefaultDisplay().getMetrics(dm);
				// float density = dm.density;
				float scale = 1.0f;
				// if (density > 2) {
				// scale = density / 1.5f;
				// }
				Float f = getWebScale(inParent1);
				webWidth = (int) (f * width);
				webHeight = (int) (f * height);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, height);
				lp.leftMargin = (int) (x * f * scale);
				lp.topMargin = (int) (y * f * scale);

				marketDecorView.setLayoutParams(lp);
				marketDecorView.invalidate();
				// }
			}
		});

	}

	/**
	 * 判断该子应用是否存在 返回 1 安装 0 未安装
	 * 
	 * @param params
	 */
	public void isAppInstalled(String[] params) {
		if (params.length != 1) {
			Toast.makeText(mContext, "参数错误", Toast.LENGTH_SHORT).show();
		}
		int num = new AppBeanDao(mContext).isAppInstalled(params[0]);

		onIsAppInstalled(params[0], num);
	}

	public void setCallBack() {

		onCallBack = new OnWidgetClickedCallback() {

			@Override
			public void onfinishWidget(String appId) {
				// TODO Auto-generated method stub

				finishWidget("aaa", appId, false);
			}

			/***
			 * 启动子应用时的回调
			 */
			@Override
			public void onWidgetClicked(String installPath, String widgetId,
					String widgetKey) {
				// TODO Auto-generated method stub
				String[] args = new String[] { widgetId, "0", widgetKey };
				callBack(CB_START_WIDGET, args);
			}

			/**
			 * 打开原生应用
			 */
			@Override
			public void onStartNativeWight(String json) {
				// TODO Auto-generated method stub
				String[] args = new String[] { json };
				callBack(CB_START_NATIVE, args);
			}

			// /***
			// * 子应用是否安装的回调
			// */
			// @Override
			// public void onIsAppInstalled(String appId, int status) {
			// // TODO Auto-generated method stub
			// String[] args = new String[] { appId, status + "" };
			// callBack(CB_IS_INSTALL_APP, args);
			// }

			/***
			 * 当高度发生变化时的回调
			 */
			@Override
			public void onHeightChanged(int height) {
				// TODO Auto-generated method stub
				String[] args = new String[] { height + "" };
				callBack(CB_HEIGHT_CHANGE, args);
				setViewFrame(x, y, height);
			}

			/***
			 * 点击更多的回调
			 */
			@Override
			public void onClickMore() {
				// TODO Auto-generated method stub
				String[] args = new String[] {};
				callBack(CB_CLICK_MORE, args);
			}

			@Override
			public void onSoftToken(String token) {
				// TODO Auto-generated method stub
				String[] args = new String[] { Tools.getSoftToken(mContext) };
				callBack(CB_SOFTTOKEN, args);
			}

			@Override
			public void onDeleteApp(String appId, String appName, String version) {
				// TODO Auto-generated method stub
				String[] args = new String[] { appId, appName, version };
				callBack(CB_DELETE_APP, args);
			}

			/***
			 * 安装成功的回调
			 * 
			 * @param appId
			 * @param appName
			 * @param version
			 */
			@Override
			public void onInstallApp(String appId, String appName,
					String version) {
				// TODO Auto-generated method stub
				String[] args = new String[] { appId, appName, version };
				callBack(CB_INSTALL_APP, args);
			}

			@Override
			public void onStartWap(String appId, String url) {
				// TODO Auto-generated method stub
				String[] args = new String[] { appId, url };
				callBack(CB_START_WAP, args);
			}
		};

	}

	public void callBack(String callName, String[] args) {

		String js = SCRIPT_HEADER + " if(" + callName + "){" + callName + "(";
		StringBuffer sb = new StringBuffer();
		sb.append(js);
		for (int i = 0, len = args.length; i < len; i++) {
			sb.append("'");
			sb.append(args[i]);
			sb.append("'");
			if (i + 1 != len) {
				sb.append(",");
			}
		}
		sb.append(");}");
		Log.e(TAG, sb.toString());
		onCallback(sb.toString());

	}

	@Override
	public void onIsAppInstalled(String appId, int status) {
		// TODO Auto-generated method stub
		String[] args = new String[] { appId, status + "" };
		callBack(CB_IS_INSTALL_APP, args);
	}

	/**
	 * getCustomScale：引擎中添加的获取x5内核网页scale的方法，为兼容旧引擎，故使用反射调用
	 * 
	 * @param mBrwView
	 * @return
	 */
	@SuppressWarnings("unused")
	private static float getWebScale(EBrowserView mBrwView) {
		float scale = 1.0f;
		try {
			Method gatScale = EBrowserView.class.getMethod("getCustomScale",
					null);
			try {
				scale = (Float) gatScale.invoke(mBrwView, null);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			scale = getWebScaleOld(mBrwView);
		}

		return scale;
	}

	private static float getWebScaleOld(EBrowserView mBrwView) {
		float nowScale = 1.0f;
		int versionA = Build.VERSION.SDK_INT;
		if (versionA <= 18) {
			nowScale = mBrwView.getScale();
		}
		return nowScale;
	}

}
