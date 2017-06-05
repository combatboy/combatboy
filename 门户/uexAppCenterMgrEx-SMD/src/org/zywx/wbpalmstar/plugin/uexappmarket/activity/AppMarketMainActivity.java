package org.zywx.wbpalmstar.plugin.uexappmarket.activity;

import java.util.ArrayList;
import java.util.List;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AppsListAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.NewMsg;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.ViewDataManager;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.DataCallBack;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.PackageInstalledReceiver;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AppMarketMainActivity extends Activity implements DataCallBack {

	public static AppMarketMainActivity sInstance;

	/**
	 * 监听安装和卸载的广播
	 */
	private PackageInstalledReceiver mPkgInstallReceiver;

	public static String IS_VISIBLE = "";
	private static View mainView;

	public static final String TAG = "AppMarketMainActivity";
	public static final String INTENT_KEY_URL = "url";
	private String requestUrl = null;
	private UpdateTaskList appsTaskList = new UpdateTaskList();
	List<AppBean> localApps;

	public static ViewDataManager mViewDataManager;

	public DragGridView gridView;

	private AppsListAdapter mAdapter;

	// public static int itemHeight = 0;

	public static final String ACTION = "org.zywx.wbpalmstar.plugin.uexappmarket.BREAK";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mainView = inflater.inflate(
				EUExUtil.getResLayoutID("plugin_appmarket_ex_view_pager_item"),
				null);

		setContentView(mainView);

		sInstance = this;

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// itemHeight = (EUExAppMarketEx.height/ Tools.ITEM_NUMBER);
		//
		// Log.e(EUExAppMarketEx.TAG, "屏幕的大小是 ：   width==" + dm.widthPixels
		// + "        height==" + dm.heightPixels);

		onRegister();
	}

	public void init() {

		String softToken = Tools.getSoftToken(this);

		if (softToken == null || softToken.length() == 0) {
			Toast.makeText(this, "softToken为空!", Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}
		requestUrl = CommonUtility.URL_APP_LIST + softToken;

		Log.e(EUExAppMarketEx.TAG, "requestUrl   " + requestUrl);

		setupView();

		mViewDataManager = new ViewDataManager(this, requestUrl, this,
				appsTaskList);

		localApps = new ArrayList<AppBean>();

		setData(new AppBeanDao(this).getOrderInstallApp());

		mAdapter = new AppsListAdapter(AppMarketMainActivity.this, localApps,
				gridView);
		gridView.setAdapter(mAdapter);

		mViewDataManager.performAsyncLoadAppListAction();

		callBackRes(localApps);

	}

	public boolean isFirstRun(Context context) {
		SharedPreferences sp = context.getSharedPreferences("first_run", 0);
		return sp.getBoolean("firstRun", true);
	}

	private void setupView() {

		gridView = (DragGridView) findViewById(EUExUtil.getResIdID("grid"));
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int positon, long arg3) {
				// TODO Auto-generated method stub
				mAdapter.new ConvertClick(localApps, gridView, positon).init();
			}
		});

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setData(final List<AppBean> mlist) {
		if (localApps != null) {

			localApps.clear();
			localApps.addAll(mlist);
			localApps.add(new AppBean());
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

		// if (localApps != null) {
		// EUExAppMarketEx.onCallBack
		// .onHeightChanged(((localApps.size()) > Tools.ITEM_NUMBER ? 2 *
		// itemHeight
		// : itemHeight));
		// } else {
		// EUExAppMarketEx.onCallBack.onHeightChanged(itemHeight);
		// }
	}

	@Override
	public void callBackRes(List<AppBean> result) {
		setData(result);
	}

	@Override
	protected void onStop() {
		super.onStop();

		SharedPreferences sp = getSharedPreferences("first_run", 0);
		Editor edit = sp.edit();
		edit.putBoolean("firstRun", false);

		edit.commit();
	}

	public void showAppsIndicate(int count) {
	}

	@Override
	protected void onDestroy() {

		if (mPkgInstallReceiver != null) {
			this.unregisterReceiver(mPkgInstallReceiver);
		}

		super.onDestroy();

	}

	public UpdateTaskList getAppsTaskList() {
		return appsTaskList;
	}

	public ViewDataManager getViewDataManager() {
		return mViewDataManager;
	}

	public AppsListAdapter getAppPagerAdapter() {
		return mAdapter;
	}

	public static AppMarketMainActivity getInstance() {
		return sInstance;
	}

	public void onRegister() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION);
		registerReceiver(myBroadcastReceiver, filter);
	}

	/**
	 * 该广播主要是在列表界面中点击返回按钮之后出现不能刷新这个界面而创建广播，
	 */
	public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			setData(new AppBeanDao(AppMarketMainActivity.this)
					.getOrderInstallApp());

			mViewDataManager.performAsyncLoadAppListAction();
		}
	};

	public void setNewMsgNotify(List<NewMsg> mNew) {

		if (mNew == null) {
			return;
		}
		new AppBeanDao(this).setNewMsg(mNew);

		setData(new AppBeanDao(this).getOrderInstallApp());

	}

}
