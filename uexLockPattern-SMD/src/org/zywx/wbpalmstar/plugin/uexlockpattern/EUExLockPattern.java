package org.zywx.wbpalmstar.plugin.uexlockpattern;

import java.util.List;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExEventListener;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import com.way.locus.Contains;
import com.way.locus.LoginActivity;
import com.way.locus.LoginActivity.lockBackList;
import com.way.locus.SetPasswordActivity;
import com.way.locus.SetPasswordActivity.setPasswordBackList;
import com.way.locus.ShaPrefUtils;

import android.R.integer;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.FROYO)
@SuppressWarnings({ "deprecation", "unused" })
public class EUExLockPattern extends EUExBase implements setPasswordBackList,
		lockBackList {
	// 服务器端已定义
	static final String func_on_success = "uexLockPattern.cbLoginSuccess";
	static final String func_on_SET = "";
	static final String func_on_GET = "uexLockPattern.cbGet";
	static final String func_on_fail = "uexLockPattern.cbLoginFail ";

	static final String func_on_forget = "uexLockPattern.cbForgetPassword ";
	static final String func_on_other = "uexLockPattern.cbUseOther";

	static final String TAG = "uexLockPattern";
	private LoginActivity mActivityLockPattern;
	private SetPasswordActivity mSetPassword;
	private static View mLockPatternDecorView;
	private String number;
	private boolean isOpen = false;
	public static Contains con;
	private boolean isLoginTrueOrFalse = false;

	public String isInit = "";

	private int errorCount;

	private int countK = 0;

	private String longinStr;

	private String isShowBack = "0";

	private String lockBack = "0";

	private static final String KEY_APP = "AppCanZYWX";

	private WWidgetData widgetData;

	private LoclPatterCustomDialog dialog;

	private LocalActivityManager localManager;

	public EUExLockPattern(Context context, EBrowserView view) {
		super(context, view);
		widgetData = view.getCurrentWidget();
	}

	@SuppressWarnings("serial")
	public void initCon() {
		con = new Contains() {

			@Override
			public void getResult(String result) {
				Log.d(TAG, "result" + result);
				if (result != null) {
					jsCallback(func_on_success, 0, EUExCallback.F_C_TEXT,
							result);
				}
			}

			@Override
			public void getError(String error) {
				String error1 = ShaPrefUtils.getCheckPasswork(mContext,
						ERRORNAME, ERRORNAME);
				if (error1 != null)
					errorCount = Integer.parseInt(error1);

				if (error != null) {
					jsCallback(func_on_fail, 0, EUExCallback.F_C_TEXT, error);
				}
			}

			@Override
			public void getForget() {
				jsCallback(func_on_forget, 0, EUExCallback.F_C_TEXT, "");
			}

			@Override
			public void getother() {
				jsCallback(func_on_other, 0, EUExCallback.F_C_TEXT, "");
			}
		};
	}

	public void lock(String[] parms) {
		Log.i(TAG, "lock");
		if (parms.length != 0) {
			return;
		}
		// lockBack = parms[0];
		String lock = ShaPrefUtils.getCheckPasswork(mContext,
				Contains.FILLOCKNAME, Contains.KEYELOCKNAME);
		if (lock != null) {
			try {
				isOpen = true;
				isLoginTrueOrFalse = true;
				if (con != null) {
					con = null;
				}
				initCon();
				longinStr = Contains.LOCK;
				openLoclPatter();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Get获取手势键盘内部保存的字符串数据
	public void get(String[] parms) {
		if (parms.length != 0) {
			return;
		}

		String getPasswordData = ShaPrefUtils.getCheckPasswork(mContext, this
				.getClass().getName(), Contains.SETORGETNAME);
		if (con != null) {
			con = null;
		}
		initCon();

		String getEnStr = AESDecode.decode(KEY_APP, getPasswordData);
		jsCallback(func_on_GET, 0, EUExCallback.F_C_TEXT, getEnStr);
	}

	// Set存取手势键盘内部保存的字符串数据
	public void set(String[] parms) {
		Log.i("xindabao", "set=" + "set1");
		if (parms.length < 1) {
			return;
		}
		String setStr = parms[0];
		// aes加密
		String enSetStr = AESDecode.encode(KEY_APP, setStr);

		ShaPrefUtils.setCheckPassword(mContext, enSetStr, this.getClass()
				.getName(), Contains.SETORGETNAME);

		initCon();
	}

	public void init(String[] parms) {
		Log.d(TAG, "init()==" + parms.length);
		if (parms.length < 2) {
			return;
		}
		Log.d(TAG, "init=" + parms.length);
		getInit();
		number = parms[0];
		String loginErrorCount = parms[1];
		isShowBack = parms[2];
		ShaPrefUtils.setCheckPassword(mContext, loginErrorCount,
				Contains.ERRORNAME, Contains.ERRORNAME);
		try {
			// clearView();
			isOpen = true;
			String strData = ShaPrefUtils.getCheckPasswork(mContext,
					Contains.FILECONTNAME, Contains.KEYECONTNAME);
			if (strData == null) {
				ShaPrefUtils.setCheckPassword(mContext, "" + 0,
						Contains.FILECONTNAME, Contains.KEYECONTNAME);
			}
			countK = Integer.parseInt(ShaPrefUtils.getCheckPasswork(mContext,
					Contains.FILECONTNAME, Contains.KEYECONTNAME));
			countK++;
			ShaPrefUtils.setCheckPassword(mContext, "" + countK,
					Contains.FILECONTNAME, Contains.KEYECONTNAME);
			ShaPrefUtils.setCheckPassword(mContext, number,
					Contains.NUMBERNAME, Contains.NUMBERNAME);
			longinStr = Contains.INIT;
			if (con != null) {
				con = null;
			}
			initCon();
			openLoclPatter();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setImg(String[] parms) {
		if (parms.length != 1) {
			return;
		}

		try {
			Thread.sleep(3 * 500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (mActivityLockPattern != null) {
				String url = parms[0];
				mActivityLockPattern.changeViewToDrawPassword(url);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close(String[] parms) {
		// clearView();
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	public void openLoclPatter() {
		{
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					clearView();
					mLockPatternDecorView = null;
					if (mLockPatternDecorView == null) {
						Intent intent = null;
						// LocalActivityManager mgr = ((ActivityGroup) mContext)
						// .getLocalActivityManager();

						if (localManager == null) {

							localManager = new LocalActivityManager(
									(Activity) mContext, false);

							localManager.dispatchCreate(null);
						}

						localManager.removeAllActivities();

						if (longinStr.equals(Contains.LOCK)) {
							intent = new Intent(mContext, LoginActivity.class);
							intent.putExtra(LoginActivity.CHECK_PASSWORK,
									widgetData.m_appId);
							intent.putExtra(LoginActivity.LOCKBACK, lockBack);
							if (isOpen) {
								isOpen = false;
								if (isLoginTrueOrFalse) {
									isLoginTrueOrFalse = false;
									intent.putExtra(
											LoginActivity.INTENT_KEY_AFRESH_LOGIN,
											true);
								}
								intent.putExtra(
										LoginActivity.INTENT_KEY_AFRESH_OR_OPEN,
										"open");
							}
						} else if (longinStr.equals(Contains.INIT)) {
							intent = new Intent(mContext,
									SetPasswordActivity.class);
							intent.putExtra(SetPasswordActivity.SETPASSWORD,
									widgetData.m_appId);
							intent.putExtra(SetPasswordActivity.SHOWBACK,
									isShowBack);
						}
						Window window = localManager.startActivity(TAG, intent);

						window.setFlags(
								WindowManager.LayoutParams.FLAG_FULLSCREEN,
								WindowManager.LayoutParams.FLAG_FULLSCREEN);
						if (longinStr.equals(Contains.LOCK)) {
							mActivityLockPattern = (LoginActivity) window
									.getContext();
							mActivityLockPattern
									.setLockBackList(EUExLockPattern.this);
						} else if (longinStr.equals(Contains.INIT)) {
							mSetPassword = (SetPasswordActivity) window
									.getContext();
							mSetPassword.setBackList(EUExLockPattern.this);
						}
						mLockPatternDecorView = window.getDecorView();
						// RelativeLayout.LayoutParams lp = new
						// RelativeLayout.LayoutParams(
						// LinearLayout.LayoutParams.FILL_PARENT,
						// LinearLayout.LayoutParams.FILL_PARENT);
						// addViewToCurrentWindow(mLockPatternDecorView, lp);
						popAlertDialog(mLockPatternDecorView);
					}
				}
			});
		}
	}

	@SuppressLint("InlinedApi")
	public void popAlertDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setCancelable(false);
		dialog = new LoclPatterCustomDialog(
				mContext,
				EUExUtil.getResStyleID("plugin_uexlockpattern_loading_dialog_style"));
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		dialog.getWindow().setContentView(view, lp);
		dialog.show();

	}

	/**
	 * @param child
	 * @param parms
	 */
	private void addView2CurrentWindow(View child,
			RelativeLayout.LayoutParams parms) {
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.NO_GRAVITY;
		lp.leftMargin = l;
		lp.topMargin = t;
		// adptLayoutParams(parms, lp);
		// Log.i(TAG, "addView2CurrentWindow");
		mBrwView.addViewToCurrentWindow(child, lp);
	}

	public void clearView() {

		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// if (null != mLockPatternDecorView) {
				// removeViewFromCurrentWindow(mLockPatternDecorView);
				// // mActivityLockPattern = null;
				// // mLockPatternDecorView = null;
				// localManager.destroyActivity(TAG, true);
				// }
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		});
	}

	public void getInit() {
		mContext.getSharedPreferences("com.way.locus.LocusPassWordView.temp", 0)
				.edit().clear().commit();
		mContext.getSharedPreferences(Contains.NUMBERNAME, 0).edit().clear()
				.commit();
		mContext.getSharedPreferences("com.way.locus.preferences1", 0).edit()
				.clear().commit();
		mContext.getSharedPreferences("com.way.locus.init", 0).edit().clear()
				.commit();
		mContext.getSharedPreferences(Contains.ERRORNAME, 0).edit().clear()
				.commit();
	}

	@Override
	protected boolean clean() {
		// ((Activity) mContext).runOnUiThread(new Runnable() {
		// @Override
		// public void run() {
		// if (null != mActivityLockPattern) {
		// clearView();
		// mActivityLockPattern = null;
		// }
		// }
		// });
		return true;
	}

	@Override
	public void setPasswordBack() {
		dialog.dismiss();
	}

	@Override
	public void backClick() {
		dialog.dismiss();
	}
}
