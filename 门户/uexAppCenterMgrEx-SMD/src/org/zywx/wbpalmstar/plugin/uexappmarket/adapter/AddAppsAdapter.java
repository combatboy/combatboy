package org.zywx.wbpalmstar.plugin.uexappmarket.adapter;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AddAppActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBeanDao;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.DBsql;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.AppUtils;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.ComparatorApp;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.DateFormat;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.FileSize;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.CustomDialog;
import org.zywx.wbpalmstar.plugin.uexappmarket.view.Options;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddAppsAdapter extends BaseExpandableListAdapter {

	private List<List<AppBean>> childList = new ArrayList<List<AppBean>>();
	private List<String> groupList;
	private Context context;
	private ExpandableListView mListView;

	private LayoutInflater childInflater, groupInflater;
	protected ImageLoader imageLoader;
	private DisplayImageOptions options;
	private AddAppActivity mActivity;
	private DBsql bsql;
	private AppBeanDao appbeandao;
	private Map<String, View> childConvertView = new HashMap<String, View>();

	private CustomDialog dialog;

	private int timeStamp = 30;

	public AddAppsAdapter(Context context, List<String> groupList,
			ExpandableListView mListView) {
		this.context = context;
		this.groupList = groupList;
		this.mListView = mListView;
		appbeandao = new AppBeanDao(context);
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		options = Options.getListOptions();

		mActivity = (AddAppActivity) context;
		bsql = mActivity.getDBsql();

		getChildData();

	}

	public void getChildData() {
		childList.clear();
		List<AppBean> appList = appbeandao.getallAppList();
		List<AppBean> unInstallList = new ArrayList<AppBean>();
		List<AppBean> installList = new ArrayList<AppBean>();

		installList.addAll(appbeandao.getInstallAppList());
		if (appList != null) {
			for (int i = 0; i < appList.size(); i++) {
				if (!installList.contains(appList.get(i))) {
					unInstallList.add(appList.get(i));
				}
			}
		}
		// 将已安装的应用先按照创建时间排序，如果创建时间距离当前时间大于30天，则将大于30天的应用按照更新时间排序
		List<AppBean> timeStampList = new ArrayList<AppBean>();
		List<AppBean> sortinstallupdate = new ArrayList<AppBean>();
		for (int i = 0; i < installList.size(); i++) {
			AppBean app = installList.get(i);
			try {

				if (DateFormat.daysBetween(app.getUpTime(),
						DateFormat.getDateFormat()) <= timeStamp) {
					// 小于30天，按照创建时间排序
					timeStampList.add(app);
				} else {
					// 大于30天，按照更新时间排序
					sortinstallupdate.add(app);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		// 将大于30天的数据按照更新时间排序
		Collections.sort(sortinstallupdate, new ComparatorApp());

		// 如上方法，排序未安装的子应用

		List<AppBean> unTimeStampList = new ArrayList<AppBean>();
		List<AppBean> unSortinstallupdate = new ArrayList<AppBean>();
		for (int i = 0; i < unInstallList.size(); i++) {
			AppBean app = unInstallList.get(i);
			try {

				if (DateFormat.daysBetween(app.getUpTime(),
						DateFormat.getDateFormat()) <= timeStamp) {
					// 小于30天，按照创建时间排序
					unTimeStampList.add(app);
				} else {
					// 大于30天，按照更新时间排序
					unSortinstallupdate.add(app);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		// 将大于30天的数据按照更新时间排序
		Collections.sort(unSortinstallupdate, new ComparatorApp());

		for (int i = 0; i < groupList.size(); i++) {
			if (i == 0) {
				unTimeStampList.addAll(unSortinstallupdate);
				childList.add(unTimeStampList);
			} else if (i == 1) {
				timeStampList.addAll(sortinstallupdate);
				childList.add(timeStampList);
			}
		}
		childConvertView.clear();
		for (int j = 0; j < childList.get(0).size(); j++) {
			childConvertView.put(childList.get(0).get(j).getAppId(), null);
		}

	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return childList.get(groupPosition).size();
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewChildHolder holder = null;
		convertView = null;
		if (convertView == null) {

			holder = new ViewChildHolder();
			childInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = childInflater.inflate(EUExUtil
					.getResLayoutID("plugin_appmarket_ex_add_app_list_item"),
					null);
			holder.plugin_add_app_item_image = (ImageView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_image"));
			holder.plugin_add_app_item_new = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_new"));
			holder.plugin_add_app_item_name = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_name"));
			holder.plugin_add_app_item_version = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_version"));
			holder.plugin_add_app_item_size = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_size"));
			holder.plugin_add_app_item_description = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_description"));
			holder.plugin_add_app_item_download = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_download"));
			holder.plugin_add_app_item_del = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_del"));

			holder.plugin_add_app_item_download_layout = (RelativeLayout) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_download_layout"));
			holder.plugin_add_app_item_progressbar = (ProgressBar) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_progressbar"));
			holder.plugin_add_app_item_progress = (TextView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_progress"));
			holder.plugin_view_down = convertView.findViewById(EUExUtil
					.getResIdID("plugin_view_down"));

			convertView.setTag(holder);
		}/*
		 * else{ holder = (ViewChildHolder) convertView.getTag(); }
		 */

		final AppBean appbean = childList.get(groupPosition).get(childPosition);
		holder.plugin_add_app_item_name.setText(appbean.getAppName());
		holder.plugin_add_app_item_version.setText("版本："
				+ appbean.getAppVer().substring(4, 6)
				+ appbean.getAppVer().substring(8));
		long size = appbean.getPkgSize();
		holder.plugin_add_app_item_size.setText(FileSize.FormetFileSize(size));
		String d = appbean.getDescription();
		if (d != null) {
			holder.plugin_add_app_item_description.setText("描述：" + d);
		} else {
			holder.plugin_add_app_item_description.setText("描述：");
		}

		// mImageFetcher.loadImage(appbean.getIconLoc(),
		// holder.plugin_add_app_item_image);
		imageLoader.displayImage(appbean.getIconLoc(),
				holder.plugin_add_app_item_image, options);
		holder.plugin_add_app_item_download.setVisibility(View.GONE);
		holder.plugin_add_app_item_del.setVisibility(View.GONE);
		holder.plugin_add_app_item_download_layout.setVisibility(View.GONE);

		// try {
		// holder.plugin_add_app_item_new.setVisibility(DateFormat
		// .daysBetween(appbean.getUpTime(),
		// DateFormat.getDateFormat()) > timeStamp ? View.GONE
		// : View.VISIBLE);
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		holder.plugin_add_app_item_new.setVisibility(View.GONE);
		int pos = indexOfTask(appbean);
		int progress = CommonUtility.getProgress(context, appbean);

		if (groupPosition == 1) {
			holder.plugin_add_app_item_del.setVisibility(View.VISIBLE);
			holder.plugin_add_app_item_download.setVisibility(View.GONE);
		} else {
			if (progress == 0
					&& !CommonUtility.getProgressStatus(context,
							appbean.getAppId())) {

				holder.plugin_add_app_item_del.setVisibility(View.GONE);
				holder.plugin_add_app_item_download.setVisibility(View.VISIBLE);
			} else {
				holder.plugin_add_app_item_download.setVisibility(View.GONE);
				holder.plugin_add_app_item_download_layout
						.setVisibility(View.VISIBLE);
				holder.plugin_add_app_item_progressbar.setProgress(progress);
				holder.plugin_add_app_item_progress.setText(progress + "%");
			}
		}

		// 如果是默认的应用，则应当不能再删除,并且改变字体颜色
		if (Boolean.parseBoolean(appbean.getMainApp()) && groupPosition == 1) {
			holder.plugin_add_app_item_del.setClickable(false);
			holder.plugin_add_app_item_del.setEnabled(false);
			holder.plugin_add_app_item_del
					.setBackgroundResource(EUExUtil
							.getResDrawableID("plugin_zdtq_add_app_item_button_disable_shape"));
			holder.plugin_add_app_item_del.setTextColor(Color
					.parseColor("#000000"));
			holder.plugin_add_app_item_del.setAlpha(0.3f);
		}

		holder.plugin_add_app_item_del
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub

						showDialog(childList.get(groupPosition), appbean);
					}
				});
		holder.plugin_add_app_item_download_layout
				.setOnClickListener(new ConvertClick(childList
						.get(groupPosition),
						holder.plugin_add_app_item_progressbar,
						holder.plugin_add_app_item_progress, childPosition));
		holder.plugin_add_app_item_download
				.setOnClickListener(new ConvertClick(childList
						.get(groupPosition),
						holder.plugin_add_app_item_progressbar,
						holder.plugin_add_app_item_progress, childPosition));

		if (groupPosition == 0
				&& childPosition == childList.get(groupPosition).size() - 1) {
			holder.plugin_view_down.setVisibility(View.GONE);
		} else {
			holder.plugin_view_down.setVisibility(View.VISIBLE);
		}

		childConvertView.put(appbean.getAppId(), convertView);
		return convertView;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewGroupHolder holder = null;

		if (convertView == null) {

			holder = new ViewGroupHolder();
			groupInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = groupInflater.inflate(
					EUExUtil.getResLayoutID("plugin_zdtq_add_app_item_title"),
					null);
			holder.groupTitle = (TextView) convertView.findViewById(EUExUtil
					.getResIdID("plugin_add_app_item_title"));
//			holder.plugin_add_app_item_group_view_top = convertView
//					.findViewById(EUExUtil
//							.getResIdID("plugin_add_app_item_group_view_top"));
			holder.plugin_add_app_item_group_view_down = convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_group_view_down"));
			holder.plugin_add_app_item_state = (ImageView) convertView
					.findViewById(EUExUtil
							.getResIdID("plugin_add_app_item_state"));
			convertView.setTag(holder);
		} else {
			holder = (ViewGroupHolder) convertView.getTag();
		}
		convertView.setClickable(true);

		holder.groupTitle.setText(groupList.get(groupPosition));

		if (groupPosition == 0) {
			holder.plugin_add_app_item_state
					.setImageResource(EUExUtil
							.getResDrawableID("plugin_appmarket_ex_add_app_group_down"));
		} else {
			holder.plugin_add_app_item_state
					.setImageResource(EUExUtil
							.getResDrawableID("plugin_appmarket_ex_add_app_group_redown"));
		}

//		if (groupPosition == 1) {
//
//			if (childList.get(0).size() == 0) {
//				holder.plugin_add_app_item_group_view_top
//						.setVisibility(View.GONE);
//			} else {
//				holder.plugin_add_app_item_group_view_top
//						.setVisibility(View.VISIBLE);
//			}
//		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	public static class ViewChildHolder {
		ImageView plugin_add_app_item_image;
		TextView plugin_add_app_item_new;
		TextView plugin_add_app_item_name;
		TextView plugin_add_app_item_version;
		TextView plugin_add_app_item_size;
		TextView plugin_add_app_item_description;
		TextView plugin_add_app_item_download;
		TextView plugin_add_app_item_del;
		RelativeLayout plugin_add_app_item_download_layout;
		ProgressBar plugin_add_app_item_progressbar;
		TextView plugin_add_app_item_progress;
		View plugin_view_down;
	}

	public static class ViewGroupHolder {
		TextView groupTitle;
//		View plugin_add_app_item_group_view_top;
		View plugin_add_app_item_group_view_down;
		ImageView plugin_add_app_item_state;
	}

	public void removeChild(AppBean appBean) {

	}

	public void addChild(AppBean appBean) {

	}

	public void showDialog(final List<AppBean> list, final AppBean appBean) {

		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder.setTitle("提\t示");
		customBuilder.setMessage("如果您删除该子应用，将导致相关的数据会一并删除，您是否确定删除?");
		customBuilder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		customBuilder.setPositiveButton("删除", new DeleteClick(list, appBean));
		dialog = customBuilder.create();
		dialog.show();
	}

	class DeleteClick implements DialogInterface.OnClickListener {

		AppBean appBean;
		List<AppBean> list;

		public DeleteClick(List<AppBean> list, AppBean bean) {

			appBean = bean;
			this.list = list;
			// this.convertView=convertView;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {

			remove(list, appBean);

			dialog.dismiss();
		}
	}

	public void remove(List<AppBean> list, AppBean appBean) {
		if (appBean == null) {
			return;
		}

		if (!list.contains(appBean)) {
			return;
		}
		/* 如果是原生应用，如果是安装状态，返回，已卸载删除列表，刷新UI */
		if (AppBean.TYPE_NATIVE == appBean.getType()) {
			if (AppUtils.isInstalled(context, appBean.getPackageName())) {
				CommonUtility.unInstallApp(context, appBean.getPackageName());
				return;
			}
		}
		// boolean isRemove = mList.remove(appBean);
		String tmpPath = new AppBeanDao(context).getDownFileFromDB(appBean);
		if (!TextUtils.isEmpty(tmpPath)) {
			if (new File(tmpPath).exists()) {
				new File(tmpPath).delete();
			}
		}

		Log.e(EUExAppMarketEx.TAG, "删除    " + appBean.getId());
		new AppBeanDao(context).deleteDownFileWithDB(appBean);
		new AppBeanDao(context).deleteDefaultApp(appBean);
		new AppBeanDao(context).deleteDownloadApp(appBean);

		CommonUtility.saveProgress(context, appBean, 0);
		CommonUtility.saveUpdateProgress(context, appBean, 0);
		CommonUtility.saveUpdateStatus(context, appBean.getAppId(), false);

		bsql.onDeleteApp(appBean);
		onRefresh();
	}

	class ConvertClick implements OnClickListener {
		private List<AppBean> appBeans;
		int position;
		ProgressBar progressBar;
		TextView textview;

		public ConvertClick(List<AppBean> beans, ProgressBar progressBar,
				TextView textview, int pos) {
			this.appBeans = beans;
			this.position = pos;
			this.progressBar = progressBar;
			this.textview = textview;
		}

		@Override
		public void onClick(View view) {

			Log.e(EUExAppMarketEx.TAG, "您点击的是："
					+ appBeans.get(position).getAppName());

			// childConvertStatus.put(appBeans.get(position).getAppId(), true);
			AppTaskList appsTaskList = ((AddAppActivity) context)
					.getAppsTaskList();

			AppBean appBean = appBeans.get(position);
			String sid = appBean.getId();

			if (appBean != null) {
				switch (appBean.getType()) {
				case AppBean.TYPE_WAP:
					// AppUtils.startWidgetWithBrowser(context,
					// appBean.getDownloadUrl(), AppBean.TYPE_WAP);
					break;
				case AppBean.TYPE_NATIVE:
					if (appsTaskList.isExistTask(sid, context)) {// 存在下载列表中
						Toast.makeText(context,
								"\"" + appBean.getAppName() + "\"暂停下载!",
								Toast.LENGTH_SHORT).show();
						// invisiable();
						return;
					}

					if (AppUtils.isInstalled(context, appBean.getPackageName())) { // 已安装
						bsql.launch(appBean, 3, position, mListView);
					} else { // 未安装
						// 安装文件存在？ true提示安装：false下载
						String[] downResult = bsql.getDownFileFromDb(appBean
								.getDownloadUrl());
						// 安装
						if (downResult != null && downResult.length > 1) {
							String path = downResult[0];
							String fileSize = downResult[1];

							if (!TextUtils.isEmpty(path)) {
								File file = new File(path);
								if (file.exists()) {
									if (file.length() == Long
											.parseLong(fileSize)) {
										CommonUtility.installApp(context, file);

										return;
									} else {
										Toast.makeText(context, "文件不完整",
												Toast.LENGTH_SHORT).show();
										bsql.deleteUpdateFromDb(appBean
												.getDownloadUrl());
										file.delete();

										CommonUtility.saveProgress(context,
												appBean, 0);
									}
								}
							}
						}
						CommonUtility.saveProgressStatus(context,
								appBean.getAppId(), true);
						// invisiable();
						bsql.launch(appBean, 0, position, mListView);

					}

					break;
				case AppBean.TYPE_WIDGET:
					if (appsTaskList.isExistTask(sid, context)) {// 存在下载列表中
						Toast.makeText(context,
								"\"" + appBean.getAppName() + "\"暂停下载!",
								Toast.LENGTH_SHORT).show();
						// invisiable();
						return;
					}

					if (AppBean.STATE_DOWNLOADED == appBean.getState()) {// 已经下载，直接启动
						/**
						 * 1、查询更新数据库，如有未解压的数据，是执行解压 2、否，打开widget
						 * 3、打开widget，后台请于求更新
						 */

						// open widget

						String[] updateInfo = new AppBeanDao(context)
								.getUpdate(appBean);
						if (updateInfo != null) {
							String filePath = updateInfo[0];
							String appVer = updateInfo[1];
							Log.e(EUExAppMarketEx.TAG, "子应用  path=" + filePath
									+ "   ver=" + appVer);
							if (!TextUtils.isEmpty(filePath)
									&& filePath.endsWith(".zip")
									&& !TextUtils.isEmpty(appVer)) {
								if (new File(filePath).exists()) {
									AppBean bean = appBean;
									bean.setAppVer(appVer);
									bsql.unzip(filePath, appBean.getAppVer(),
											1, bean, position, mListView);
									return;
								} else {
									new AppBeanDao(context)
											.deleteUpdate(appBean);
								}
							}
						}

					} else {// 尚未下载，验证登陆，开始下载
							// createAppDownloadTask(appBean, position, 1);
						Log.e(EUExAppMarketEx.TAG, "尚未下载，验证登陆，开始下载     "
								+ appBeans.get(position).getAppName());
						String[] result = bsql.getFilePahFromDownload(appBean
								.getDownloadUrl());
						if (result != null) {
							String path = result[0];
							String size = result[1];
							// Log.i(EUExAppMarketEx.TAG, "path====" + path
							// + "=======size==" + size);
							if (!TextUtils.isEmpty(path)
									&& !TextUtils.isEmpty(size)) {
								int fileSize = Integer.parseInt(size);
								File file = new File(path);
								if (file.exists() && file.length() == fileSize) {
									bsql.unzip(path, appBean.getAppVer(), 0,
											appBean, position, mListView);

									return;
								}
							}
						}
						CommonUtility.saveProgressStatus(context,
								appBean.getAppId(), true);
						// invisiable();
						bsql.launch(appBean, 0, position, mListView);

					}
					break;
				}
			}
		}

		public void invisiable() {
			progressBar.setVisibility(View.GONE);
			textview.setVisibility(View.VISIBLE);
		}
	}

	private int indexOfTask(AppBean appBean) {
		AppTaskList appTaskList = mActivity.getAppsTaskList();
		for (int i = 0, size = appTaskList.size(); i < size; i++) {
			if (appBean != null
					&& null != appTaskList.getTask(i)
					&& appBean.getId().equals(
							appTaskList.getTask(i).getTaskId())) {
				return appTaskList.getTask(i).getPosition();
			}
		}
		return -1;
	}

	public String getItemPosition(AppBean appBean) {
		String id = "";
		for (int i = 0; i < childList.size(); i++) {
			List<AppBean> list = childList.get(i);
			for (int j = 0; j < list.size(); j++) {
				if (list.contains(appBean)) {
					id = appBean.getAppId();
				}
			}
		}
		return id;
	}

	public void updateViewProgress(String appId, int progress,
			boolean isUnziped, boolean isInVisible) {
		Log.e(EUExAppMarketEx.TAG, "downLoad progress    appId=" + appId
				+ "   progress=" + progress);
		View childView = childConvertView.get(appId);
		if (childView != null) {
			ViewChildHolder holder = (ViewChildHolder) childView.getTag();
			holder.plugin_add_app_item_download_layout
					.setVisibility(View.VISIBLE);
			holder.plugin_add_app_item_progressbar.setProgress(progress);
			holder.plugin_add_app_item_progress.setText(progress + "%");
			if (progress == 100) {
				CommonUtility.saveProgressStatus(context, appId, false);
			}

		}
	}

	public void onRefresh() {
		getChildData();
		notifyDataSetChanged();
	}

}