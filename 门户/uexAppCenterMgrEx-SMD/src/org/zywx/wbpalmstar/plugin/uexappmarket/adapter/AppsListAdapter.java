package org.zywx.wbpalmstar.plugin.uexappmarket.adapter;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AddAppActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppDbHelper;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.ViewDataManager;
import org.zywx.wbpalmstar.plugin.uexappmarket.bitmap.util.ImageFetcher;
import org.zywx.wbpalmstar.plugin.uexappmarket.bitmap.util.ImageCache.ImageCacheParams;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.UpdateTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.AppUtils;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.ImageFetcherFactory;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridBaseAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.DragGridView;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.MyRoundProgress;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AppsListAdapter extends BaseAdapter implements DragGridBaseAdapter {

	private List<AppBean> list;
	public LayoutInflater inflater;
	private DragGridView mGridView = null;
	private static boolean isShowDeleteView = false;

	public int mChildsInParent;
	private Context mContext;
	public ViewDataManager viewDataManager;
	AddAppActivity activity;

	private AppBeanDao appbean;
	private int mHidePosition = -1;

	public int gridItemHeight;

	private ImageFetcher mImageFetcher;

	public AppsListAdapter(Context context, List<AppBean> beans,
			DragGridView gridView) {
		this.mContext = context;
		this.mGridView = gridView;
		this.list = beans;
		appbean = new AppBeanDao(context);

		viewDataManager = AppMarketMainActivity.mViewDataManager;

		ImageCacheParams cacheParams = new ImageCacheParams(context,
				AppMarketMainActivity.TAG);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		mImageFetcher = ImageFetcherFactory.getAppImageFetcher(context);

	}

	@Override
	public int getCount() {

		return list.size();
	}

	@Override
	public AppBean getItem(int position) {

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getItemPosition(AppBean appBean) {
		return list.indexOf(appBean);
	}

	public void updateViewProgress(int itemIndex, int progress,
			boolean isUnziped, boolean isInVisible) {
		// 得到第一个可显示控件的位置，
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新

		View view = mGridView.getChildAt(itemIndex);
		if (view != null) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.plugin_iamge_app = (ImageView) view.findViewById(EUExUtil
					.getResIdID("plugin_iamge_app"));

			holder.plugin_view_update_Progress = (MyRoundProgress) view
					.findViewById(EUExUtil
							.getResIdID("plugin_view_update_Progress"));

			holder.plugin_view_update_Progress.setProgress(progress);
			if (progress == 100) {
				holder.plugin_view_update_Progress.setVisibility(View.GONE);

				list.get(itemIndex).setState(AppBean.STATE_DOWNLOADED);
			} else {
				CommonUtility.saveUpdateStatus(mContext, list.get(itemIndex)
						.getAppId(), true);
				holder.plugin_view_update_Progress.setVisibility(View.VISIBLE);
				CommonUtility.saveUpdateProgress(mContext, list.get(itemIndex),
						progress);
			}
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		AppBean appBean = getItem(position);
		convertView = null;
		Log.e(EUExAppMarketEx.TAG, "position==" + position);
		if (convertView == null) {
			holder = new ViewHolder();
			inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(
					EUExUtil.getResLayoutID("plugin_zdtq_ex_grid_item"), null);
//			holder.viewBg = (View) convertView.findViewById(EUExUtil
//					.getResIdID("plugin_view_vertical_spacing_left"));
			holder.plugin_iamge_app = (ImageView) convertView
					.findViewById(EUExUtil.getResIdID("plugin_iamge_app"));
			holder.plugin_image_app_name = (TextView) convertView
					.findViewById(EUExUtil.getResIdID("plugin_image_app_name"));

			holder.plugin_view_update_Progress = (MyRoundProgress) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_view_update_Progress"));

			holder.plugin_appstore_grid_new_layout = (RelativeLayout) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_appstore_grid_new_layout"));
			holder.plugin_appstore_grid_new_text = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_appstore_grid_new_text"));
			convertView.setTag(holder);

		}

		gridItemHeight = EUExAppMarketEx.webWidth;
		// int gridItemWidth = EUExAppMarketEx.width / Tools.ITEM_HEIGHT;

		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				gridItemHeight / Tools.ITEM_HEIGHT, (int)(gridItemHeight*11/12));
		convertView.setLayoutParams(params);

		// gridItemHeight = EUExAppMarketEx.width / Tools.ITEM_NUMBER;
		// int gridItemWidth = EUExAppMarketEx.width / Tools.ITEM_NUMBER;
		//
		// AbsListView.LayoutParams params = new AbsListView.LayoutParams(
		// EUExAppMarketEx.width / Tools.ITEM_NUMBER, gridItemHeight);
		// convertView.setLayoutParams(params);

		// LinearLayout.LayoutParams paramsimage = new
		// LinearLayout.LayoutParams(
		// gridItemWidth * 3 / 5, gridItemWidth * 3 / 5);
		// holder.plugin_iamge_app.setLayoutParams(paramsimage);

		// 给GridViewe设置初始化位置
		appbean.setAppLocation(position, appBean.getAppId());

//		holder.viewBg.setVisibility(View.VISIBLE);
//		if ((position) % Tools.ITEM_HEIGHT == 0) {
//			holder.viewBg.setVisibility(View.GONE);
//		}

		if (true && position == (list.size() - 1)) {// ViewPager最后一页且GridView最后一项

			holder.plugin_iamge_app
					.setBackgroundDrawable(mContext
							.getResources()
							.getDrawable(
									EUExUtil.getResDrawableID("plugin_appmarket_ex_add_app")));
			holder.plugin_iamge_app.setImageDrawable(null);

			holder.plugin_image_app_name.setText("更多");
//			holder.plugin_image_app_name.setVisibility(View.GONE);
			if (isShowDeleteView) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}

			holder.plugin_view_update_Progress.setVisibility(View.GONE);
		} else {

			int progress = CommonUtility.getUpdateProgress(mContext, appBean);
			if (!CommonUtility.getUpdateStatus(mContext, appBean.getAppId())) {
				holder.plugin_view_update_Progress.setVisibility(View.GONE);

			} else {
				holder.plugin_view_update_Progress.setVisibility(View.VISIBLE);
				holder.plugin_view_update_Progress.setProgress(progress);
			}

			holder.plugin_iamge_app.setBackgroundDrawable(null);
			// AppMarketMainActivity.imageLoader.displayImage(appBean.getIconLoc(),
			// holder.plugin_iamge_app, AppMarketMainActivity.options);
			mImageFetcher.loadImage(appBean.getIconLoc(),
					holder.plugin_iamge_app);
			holder.plugin_image_app_name.setText(appBean.getAppName());
		}

		if (position == mHidePosition) {
			convertView.setVisibility(View.INVISIBLE);
		}

		int newNum = appBean.getNewApp();
		Log.e(EUExAppMarketEx.TAG, "appId=" + appBean.getAppId() + "    num="
				+ newNum);
		if (newNum == 0) {
			holder.plugin_appstore_grid_new_layout.setVisibility(View.GONE);
		} else {
			holder.plugin_appstore_grid_new_layout.setVisibility(View.VISIBLE);
			if (newNum > 99) {

				holder.plugin_appstore_grid_new_text.setText(99 + "");
			} else {
				holder.plugin_appstore_grid_new_text.setText(newNum + "");
			}

		}

		return convertView;
	}

	public static class ViewHolder {

		public TextView plugin_image_app_name;
		public ImageView plugin_iamge_app;
		public MyRoundProgress plugin_view_update_Progress;
		RelativeLayout plugin_appstore_grid_new_layout;
		TextView plugin_appstore_grid_new_text;
//		public View viewBg;
	}

	public class ConvertClick {
		private List<AppBean> appBeans;
		DragGridView gridView;
		int position;

		public ConvertClick(List<AppBean> beans, DragGridView gridView, int pos) {
			appBeans = beans;
			this.gridView = gridView;
			this.position = pos;
		}

		public void init() {

			int gridItems = gridView.getAdapter().getCount();

			/** 如果当前显示是ViewPager最后一页，且点击的是GridView的最后一项 */
			if (position == (gridItems - 1)) {
				viewDataManager.openMore();

				mContext.startActivity(new Intent(mContext,
						AddAppActivity.class));

			} else {
				UpdateTaskList appsTaskList = ((AppMarketMainActivity) mContext)
						.getAppsTaskList();
				AppBean appBean = appBeans.get(position);
				String sid = appBean.getId();

				if (appBean != null) {
					switch (appBean.getType()) {
					case AppBean.TYPE_WAP:
						Log.e(EUExAppMarketEx.TAG, "widgetType   WAP");
						EUExAppMarketEx.onCallBack.onStartWap(
								appBean.getAppId(), appBean.getDownloadUrl());
						break;
					case AppBean.TYPE_NATIVE:
						Log.e(EUExAppMarketEx.TAG, "widgetType   Native");
						if (AppUtils.isInstalled(mContext,
								appBean.getPackageName())) {

							viewDataManager.launch(appBean, gridView, 3,
									position);

						} else {
							if (appsTaskList.isExistTask(sid, mContext)) {
								return;
							}

							// 安装文件存在？ true提示安装：false下载

							String[] downResult = viewDataManager
									.getDownFileFromDb(appBean.getDownloadUrl());
							if (downResult != null && downResult.length > 1) {
								String path = downResult[0];
								String fileSize = downResult[1];

								if (!TextUtils.isEmpty(path)) {
									File file = new File(path);
									if (file.exists()) {
										if (file.length() == Long
												.parseLong(fileSize)) {
											CommonUtility.installApp(mContext,
													file);
											return;
										} else {
											Toast.makeText(mContext, "文件不完整",
													Toast.LENGTH_SHORT).show();
											viewDataManager
													.deleteUpdateFromDb(appBean
															.getDownloadUrl());
											file.delete();

										}
									}
								}
							}

							viewDataManager.launch(appBean, gridView, 0,
									position);

						}
						break;
					case AppBean.TYPE_WIDGET:
						Log.e(EUExAppMarketEx.TAG, "widgetType   widget");
						if (appsTaskList.isExistTask(sid, mContext)) {// 存在下载列表中
							if (CommonUtility.getUpdateStatus(mContext,
									appBean.getAppId())) {
								Toast.makeText(
										mContext,
										"\"" + appBean.getAppName() + "\"暂停下载!",
										Toast.LENGTH_SHORT).show();
							}
							return;
						}

						String appInstallPath = (appBean != null) ? appBean
								.getInstallPath() : null;
						String pathSandbox = Tools.getSandbox();
						String titlePath = pathSandbox.substring(0, 10);
						Log.i("aaaaa", appInstallPath
								+ "=========titlePath====" + titlePath);
						if (!TextUtils.isEmpty(appInstallPath)
								&& !appInstallPath.startsWith(titlePath)) {
							Log.i(EUExAppMarketEx.TAG, "=========appBean===="
									+ appBean.getState());
							appBean.setState(AppBean.STATE_UNLOAD);
						}

						Log.e(EUExAppMarketEx.TAG, "====下载应用地址====>>>>>===="
								+ appBean.getDownloadUrl());
						if (AppBean.STATE_DOWNLOADED == appBean.getState()) {// 已经下载，直接启动

							/**
							 * 1、查询更新数据库，如有未解压的数据，是执行解压 2、否，打开widget
							 * 3、打开widget，后台请于求更新
							 */

							// open widget

							String[] updateInfo = new AppBeanDao(mContext)
									.getUpdate(appBean);
							if (updateInfo != null) {
								String filePath = updateInfo[0];
								String appVer = updateInfo[1];
								Log.e(EUExAppMarketEx.TAG,
										"widget      path   =" + filePath
												+ "     ver=" + appVer);
								if (!TextUtils.isEmpty(filePath)
										&& filePath.endsWith(".zip")
										&& !TextUtils.isEmpty(appVer)) {
									if (new File(filePath).exists()) {
										AppBean bean = appBean;
										bean.setAppVer(appVer);
										viewDataManager.unzip(filePath,
												appBean.getAppVer(), 1, bean,
												gridView, position);
										return;
									} else {
										new AppBeanDao(mContext)
												.deleteUpdate(appBean);
									}
								}
							}
							// open widget, down , if file not exist, open else
							String installPath = (appBean != null) ? appBean
									.getInstallPath() : null;
							if (!TextUtils.isEmpty(installPath)) {
								Log.e(EUExAppMarketEx.TAG, "installPath     ="
										+ installPath);
								File file = new File(installPath);
								if (file.exists()) {
									File[] files = file.listFiles();
									if (files != null && files.length > 1) {
										for (int i = 0, length = files.length; i < length; i++) {

											String name = files[i].getName();

											if ("config.xml".endsWith(name)) {
												viewDataManager.launch(appBean,
														gridView, 1, position);
												return;
											}
										}
									}
								}
							}

							appBean.setState(AppBean.STATE_UNLOAD);
							viewDataManager.deleteUpdateFromDb(appBean
									.getInstallPath());
							new AppBeanDao(mContext).deleteApp(
									AppDbHelper.TABLE_NAME, appBean.getId());
							new AppBeanDao(mContext).updateAppState(
									appBean.getId(), AppBean.STATE_UNLOAD, "");
							// Toast.makeText(mContext, "文件不存在",
							// Toast.LENGTH_SHORT).show();
							viewDataManager.launch(appBean, gridView, 0,
									position);

							/**
							 * 1、查询更新数据表取出文件，如果文件以.zip文件，1、是安装应用2、否打开当前应用，续传文件
							 * 2、检查更新
							 * 
							 */

						} else {// 尚未下载，验证登陆，开始下载

							Log.e(EUExAppMarketEx.TAG,
									"尚未下载，验证登陆，开始下载     name="
											+ appBean.getAppName());

							Log.e(EUExAppMarketEx.TAG, "====下载应用地址===="
									+ appBean.getDownloadUrl());

							String[] result = viewDataManager
									.getFilePahFromDownload(mContext,
											appBean.getDownloadUrl());
							if (result != null) {
								String path = result[0];
								String size = result[1];
								if (!TextUtils.isEmpty(path)
										&& !TextUtils.isEmpty(size)) {
									int fileSize = Integer.parseInt(size);
									File file = new File(path);
									if (file.exists()
											&& file.length() == fileSize) {
										viewDataManager.unzip(path,
												appBean.getAppVer(), 0,
												appBean, gridView, position);
										return;
									}
								}
							}
							viewDataManager.launch(appBean, gridView, 0,
									position);

						}
						break;
					}
				}
			}
		}

	}

	@Override
	public void reorderItems(int oldPosition, int newPosition) {
		// TODO Auto-generated method stub
		Log.e(EUExAppMarketEx.TAG, "oldP=" + oldPosition + "   newP="
				+ newPosition);
		if (oldPosition == -1) {
			return;
		}
		AppBean temp = list.get(oldPosition);

		if (oldPosition < newPosition) {
			for (int i = oldPosition; i < newPosition; i++) {
				Collections.swap(list, i, i + 1);
			}
		} else if (oldPosition > newPosition) {
			for (int i = oldPosition; i > newPosition; i--) {
				Collections.swap(list, i, i - 1);
			}
		}
		list.set(newPosition, temp);
	}

	@Override
	public void setHideItem(int hidePosition) {
		// TODO Auto-generated method stub
		this.mHidePosition = hidePosition;

		notifyDataSetChanged();

	}

}