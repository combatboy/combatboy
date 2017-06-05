package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.zip.CnZipInputStream;
import org.zywx.wbpalmstar.base.zip.ZipEntry;
import org.zywx.wbpalmstar.engine.EUtil;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.http.Http;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.GridView;

@SuppressLint("NewApi")
public class CommonUtility {

	public static final String TAG = "CommonUtility";
	public static final String DOWNLOAD_PROCESS_FILE = "down_prefs";
	public static final String UPDATE_PROCESS_FILE = "update_prefs";
	public static final String UPDATE_PROCESS_STATUS_FILE = "update_status";
	public static final String DOWNLOAD_PROCESS_STATUS_FILE = "down_status";
	public static final String EMAIL_APP_PACKAGE = "com.fsck.zywxMailk9";
	public static final String MUC_APP_PACKAGE = "com.ceair.muc";
	public static final String KEY_USER = "user";

	public static String KEY_USERNAME = "username";
	public static String KEY_PASSWORD = "password";
	public static String appIdArray = "";
	// public static String URL_APP_LIST =
	// "http://192.168.1.91/mam/store/appList?softToken=";
	// public static String URL_APP_LIST =
	// "http://192.168.1.42/mam/store/appList?softToken=";
	public static String URL_APP_LIST = "";
	public static String URL_AD = "";
	public static String USERNAME = "";
	public static String PASSWORD = "";

	// 3aec349a434d4b43f158f8c973649257

	public static String CHECKUPDATE_URL = "";
	// public static String QUERY_AD_URL =
	// "http://mt.ceair.com:7001/mobileContentManager/rest/mobileContentManager/query";
	public static String Report_url = "";
	public static String appId = null;

	public static final String FILE_DIR = "/widgetone/widgets/";
	public static final String WIDGET_SAVE_PATH = getSdDir()
			+ "/widgetone/widgets/";
	// public static String WIDGET_SAVE_PATH = "";
	/** 缓存目录 */
	public static String INTERNAL_CACHE_DIRECTORY = "";

	public static final String cPath = "file:///android_asset/widget/wgtRes/clientCertificate.p12";

	public static boolean isPad = false;

	public static String sendHttpRequestByGet(String url, Context ctx) {

		String result = null;
		HttpGet httpGet = null;
		HttpClient defaultHttpClient = null;
		try {
			httpGet = new HttpGet(url);

			// 头部校验
			long time = System.currentTimeMillis();
			httpGet.addHeader("appverify",
					HttpHeader.getAppVerifyValue(null, time));
			httpGet.addHeader("x-mas-app-id", EUExAppMarketEx.appId);
			httpGet.addHeader(
					"varifyApp",
					HttpHeader.getMD5Code(EUExAppMarketEx.appId + ":"
							+ EUExAppMarketEx.appKey + ":" + time)
							+ ";" + time + ";");
			BDebug.i(EUExAppMarketEx.TAG, "sendHttpRequestByGet(): " + url);
			Log.e(EUExAppMarketEx.TAG, url);
			// BasicHttpParams httpParams = new BasicHttpParams();
			// HttpConnectionParams.setConnectionTimeout(httpParams, 300);
			// HttpConnectionParams.setSoTimeout(httpParams, 30000);
			if (url.startsWith("https")) {
				String psw = EUtil.getCertificatePsw(ctx, appId);
				defaultHttpClient = Http.getHttpsClientWithCert(psw, cPath,
						10000, ctx);
			} else {
				defaultHttpClient = Http.getHttpClient(10000);
			}

			HttpResponse httpResponse = defaultHttpClient.execute(httpGet);
			//
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpGet != null) {
				httpGet.abort();
				httpGet = null;
			}
			if (defaultHttpClient != null) {
				defaultHttpClient.getConnectionManager().shutdown();
				defaultHttpClient = null;
			}
		}

		return result;
	}

	public static String sendHttpRequestByGet(String url, Context ctx,
			String charset) {
		String result = null;
		HttpGet httpGet = null;
		HttpClient defaultHttpClient = null;
		try {
			httpGet = new HttpGet(url);
			//
			// BasicHttpParams httpParams = new BasicHttpParams();
			// HttpConnectionParams.setConnectionTimeout(httpParams, 300);
			// HttpConnectionParams.setSoTimeout(httpParams, 30000);
			if (url.startsWith("https")) {
				String psw = EUtil.getCertificatePsw(ctx, appId);
				defaultHttpClient = Http.getHttpsClientWithCert(psw, cPath,
						10000, ctx);
			} else {
				defaultHttpClient = Http.getHttpClient(10000);
			}

			HttpResponse httpResponse = defaultHttpClient.execute(httpGet);

			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = EntityUtils
						.toString(httpResponse.getEntity(), charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpGet != null) {
				httpGet.abort();
				httpGet = null;
			}
			if (defaultHttpClient != null) {
				defaultHttpClient.getConnectionManager().shutdown();
				defaultHttpClient = null;
			}
		}
		// result =
		// "[{\"appType\":\"娱乐\",\"category\":\"Wgt\",\"categoryName\":\"Wgt\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":10,\"minutes\":46,\"month\":11,\"nanos\":0,\"seconds\":27,\"time\":1355193987000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 10:46\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 10:46\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"\",\"iconLocStr\":\"\",\"id\":5,\"name\":\"测试Wgt\",\"pkgUrl\":\"http://192.168.1.38:8080/ga/11111978.zip\",\"platform\":\"\"},{\"appType\":\"社交\",\"category\":\"Wgt\",\"categoryName\":\"Wgt\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":10,\"minutes\":48,\"month\":11,\"nanos\":0,\"seconds\":29,\"time\":1355194109000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 10:48\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 10:48\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"000003_1355194074071.jpg\",\"iconLocStr\":\"http://192.168.1.42/uploads/000003_1355194074071.jpg\",\"id\":7,\"name\":\"测试wap应用\",\"pkgUrl\":\"http://192.168.1.38:8080/ga/11111978.zip\",\"platform\":\"\"},{\"appType\":\"社交\",\"category\":\"Wap\",\"categoryName\":\"Wap\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":14,\"minutes\":17,\"month\":11,\"nanos\":0,\"seconds\":59,\"time\":1355206679000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 14:17\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 14:17\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"000004_1355206659886.png\",\"iconLocStr\":\"http://192.168.1.42/uploads/000004_1355206659886.png\",\"id\":8,\"name\":\"QQ邮箱\",\"pkgUrl\":\"mail.qq.com\",\"platform\":\"\"},{\"appType\":\"社交\",\"category\":\"Wap\",\"categoryName\":\"Wap\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":14,\"minutes\":18,\"month\":11,\"nanos\":0,\"seconds\":44,\"time\":1355206724000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 14:18\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 14:18\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"000005_1355206691795.png\",\"iconLocStr\":\"http://192.168.1.42/uploads/000005_1355206691795.png\",\"id\":9,\"name\":\"Google浏览器\",\"pkgUrl\":\"www.google.com.hk\",\"platform\":\"\"},{\"appType\":\"通讯\",\"category\":\"Wap\",\"categoryName\":\"Wap\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":14,\"minutes\":19,\"month\":11,\"nanos\":0,\"seconds\":21,\"time\":1355206761000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 14:19\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 14:19\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"000006_1355206735100.png\",\"iconLocStr\":\"http://192.168.1.42/uploads/000006_1355206735100.png\",\"id\":10,\"name\":\"QQ聊天\",\"pkgUrl\":\"qq.com\",\"platform\":\"\"},{\"appType\":\"社交\",\"category\":\"Wap\",\"categoryName\":\"Wap\",\"createdAt\":{\"date\":11,\"day\":2,\"hours\":14,\"minutes\":19,\"month\":11,\"nanos\":0,\"seconds\":54,\"time\":1355206794000,\"timezoneOffset\":-480,\"year\":112},\"createdAtStr\":\"2012-12-11 14:19\",\"createdDay\":\"2012-12-11\",\"createdStr\":\"2012-12-11 14:19\",\"createdTime\":\"2012-12-11\",\"defaultPriv\":true,\"description\":\"\",\"downloadCnt\":0,\"iconLoc\":\"000007_1355206772285.png\",\"iconLocStr\":\"http://192.168.1.42/uploads/000007_1355206772285.png\",\"id\":11,\"name\":\"手机人人网\",\"pkgUrl\":\"wwww.renren.com\",\"platform\":\"\"}]";
		return result;
	}

	public static String sendHttpRequestByPost(String url,
			List<NameValuePair> nameValuePairs, Context ctx) {
		String result = null;
		HttpPost post = null;
		HttpClient defaultHttpClient = null;
		try {
			post = new HttpPost(url);
			long time = System.currentTimeMillis();

			// post.addHeader("appverify",
			// HttpHeader.getAppVerifyValue(mCurWData,time));
			// post.addHeader("x-mas-app-id", mCurWData.m_appId);
			// post.addHeader("varifyApp",
			// HttpHeader.getMD5Code(mCurWData.m_appId + ":" +
			// mCurWData.m_appkey+ ":" + time)+";"+time+";");

			post.addHeader("appverify",
					HttpHeader.getAppVerifyValue(null, time));
			post.addHeader("x-mas-app-id", EUExAppMarketEx.appId);
			post.addHeader(
					"varifyApp",
					HttpHeader.getMD5Code(EUExAppMarketEx.appId + ":"
							+ EUExAppMarketEx.appKey + ":" + time)
							+ ";" + time + ";");

			defaultHttpClient = Http.getHttpsClientWithCert(
					EUtil.getCertificatePsw(ctx, appId), cPath, 3000, ctx);

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			HttpResponse httpResponse = defaultHttpClient.execute(post);

			int responseCode = httpResponse.getStatusLine().getStatusCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (post != null) {
				post.abort();
				post = null;
			}
			if (defaultHttpClient != null) {
				defaultHttpClient.getConnectionManager().shutdown();
				defaultHttpClient = null;
			}
		}

		return result;
	}

	public static String sendHttpRequestByPost(String url, String json) {
		BDebug.d(TAG, "request:" + json);
		String result = null;
		HttpPost httpPost = new HttpPost(url);

		BDebug.i(TAG, "HttpRequestUrl--------->" + url);
		// 传参服务端获取的方法为request.getParameter("name")
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("body", json));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 30 * 1000);
			HttpConnectionParams.setSoTimeout(httpParams, 30 * 1000);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			BDebug.d(TAG, "sendHttpRequestByPost() rspCode:" + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) {
				result = EntityUtils.toString(httpResponse.getEntity()).trim();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			BDebug.d(TAG, "response:" + result);
		}
		return result;
	}

	public static synchronized String unzip(InputStream inputStream,
			String decompression, String encoding) {
		if (encoding == null || encoding.equals("")) {
			encoding = "UTF-8";
		}
		String installPath = null;
		File dir = new File(decompression);
		CnZipInputStream zis = null;
		try {
			// 检查是否是ZIP文件
			// ZipFile zip = new ZipFile(infile);
			// zip.close();
			// 建立与目标文件的输入连接
			zis = new CnZipInputStream(inputStream, encoding);
			ZipEntry file = zis.getNextEntry();

			installPath = dir.getAbsolutePath() + "/" + file.getName();

			byte[] c = new byte[1024];
			int slen;
			while (file != null) {
				String zename = file.getName();
				if (file.isDirectory()) {
					File files = new File(dir.getAbsolutePath() + "/" + zename); // 在指定解压路径下建子文件夹
					files.mkdirs();// 新建文件夹
				} else {
					File files = new File(dir.getAbsolutePath() + "/" + zename)
							.getParentFile();// 当前文件所在目录
					if (!files.exists()) {// 如果目录文件夹不存在，则创建
						files.mkdirs();
					}
					FileOutputStream out = new FileOutputStream(
							dir.getAbsolutePath() + "/" + zename);
					while ((slen = zis.read(c, 0, c.length)) != -1)
						out.write(c, 0, slen);
					out.close();
				}
				file = zis.getNextEntry();
			}
		} catch (Exception e) {
			installPath = null;
			e.printStackTrace();
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return installPath;
	}

	public static byte[] downloadImage(String url) {
		if (url == null || url.length() == 0) {
			return null;
		}
		byte[] data = null;
		int resCode = -1;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(httpGet);
			resCode = response.getStatusLine().getStatusCode();
			if (resCode == HttpURLConnection.HTTP_OK) {
				baos = new ByteArrayOutputStream(4096);
				is = response.getEntity().getContent();
				byte[] buffer = new byte[4096];
				int actulSize = 0;
				while ((actulSize = is.read(buffer)) != -1) {
					baos.write(buffer, 0, actulSize);
				}
				data = baos.toByteArray();
			}
		} catch (IOException e) {
			BDebug.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			BDebug.e(TAG, "OutOfMemoryError:" + error.getMessage());
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			final Rect src = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);

			return output;
		} catch (Exception e) {
			return null;
		}
	}

	public static File createCacheFile(Activity activity) {
		return new File(activity.getCacheDir(), System.currentTimeMillis()
				+ ".tmp");
	}

	public static File createExternalCacheFile(Activity activity) {

		return new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				System.currentTimeMillis() + ".tmp");

	}

	public static File createSdTempFile(Activity activity) {
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/widgetone/temp/");
		if (!file.exists()) {
			file.mkdirs();
		}
		return new File(file, System.currentTimeMillis() + ".tmp");
	}

	public static boolean isOnline(Context _context) {
		ConnectivityManager cm = (ConnectivityManager) _context
				.getSystemService(Service.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		}

		return false;
	}

	public static String getNetExtras(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		String extra = networkInfo != null ? networkInfo.getExtraInfo() : null;
		return extra;
	}

	public static String getNetType(Context _context) {
		ConnectivityManager cm = (ConnectivityManager) _context
				.getSystemService(Service.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()
				&& networkInfo.isConnected()) {
			String netTypeName = networkInfo.getTypeName();

			Log.e(EUExAppMarketEx.TAG, "网络类型    " + netTypeName);

			return networkInfo.getTypeName();
		}

		return null;
	}

	public static NetworkInfo getNetworkInfo(Context _context) {
		ConnectivityManager cm = (ConnectivityManager) _context
				.getSystemService(Service.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()
				&& networkInfo.isConnected()) {
			return networkInfo;
		}

		return null;
	}

	public static String getSubTypeName(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null) {
			return networkInfo.getSubtypeName();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void changeGridViewHight(GridView gridView, View convertView) {
		if (isPad) {
			int pHight = gridView.getHeight();
			GridView.LayoutParams params = new GridView.LayoutParams(
					LayoutParams.FILL_PARENT, pHight / 3);
			convertView.setLayoutParams(params);
		}
	}

	// 自动安装apk
	public static void autoInstallApp(Context ctx, String appName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(appName)),
				"application/vnd.android.package-archive");
		ctx.startActivity(intent);
	}

	// 自动启动应用
	public static void autoLaunchApp(Context ctx, String appName) {

	}

	public static String getSdDir() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile)
			throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	// 复制文件夹
	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	public static void deleteDirector(String dir) {
		File[] f = new File(dir).listFiles();
		if (f != null && f.length > 0) {
			for (int i = 0; i < f.length; i++) {
				if (f[i].isFile()) {
					f[i].delete();
				}

				if (f[i].isDirectory()) {
					deleteDirector(f[i].getAbsolutePath());
					f[i].delete();
				}
			}
		}
	}

	public static void showConfirm(Context context, String title, String msg,
			String confirmLabel, DialogInterface.OnClickListener confirmListener) {
		showAlert(context, title, msg, confirmLabel, confirmListener, null,
				null);
	}

	public static void showAlert(Context context, String title, String msg,
			String confirmLabel,
			DialogInterface.OnClickListener confirmListener,
			String cancelLabel, DialogInterface.OnClickListener cancelListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(confirmLabel, confirmListener);
		builder.setNegativeButton(cancelLabel, cancelListener);
		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}

	@SuppressWarnings("deprecation")
	public static Dialog showLoadingDialog(Context context) {
		EUExUtil.init(context);
		final CustomDialog dialog = new CustomDialog(context,
				EUExUtil.getResStyleID("plugin_appmarket_loading_dialog_style"));
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.FILL_PARENT;
		setParams(context, lp);
		dialog.setContentView(EUExUtil
				.getResLayoutID("plugin_appmarket_ex_loading_dialog"));
		return dialog;

	}

	private static void setParams(Context context, LayoutParams lay) {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		Rect rect = new Rect();
		View view = ((Activity) context).getWindow().getDecorView();

		view.getWindowVisibleDisplayFrame(rect);
		lay.height = dm.heightPixels - view.getTop();
		lay.width = dm.widthPixels;
	}

	public static HttpUriRequest getHttpRequest(Context context, String url,
			long count) {

		HttpUriRequest request = new HttpGet(url);
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5 * 1000);
		HttpConnectionParams.setSoTimeout(params, 60 * 1000);

		HttpClientParams.setRedirecting(params, true);
		request.setParams(params);

		if ("CTWAP".equalsIgnoreCase(CommonUtility.getNetExtras(context))) {
			request.setHeader("Accept", "*/*");
		}

		if (count > 0) {
			request.setHeader("RANGE", "bytes=" + count + "-");
		}
		return request;

	}

	public static HttpEntity getEntity(HttpUriRequest request, HttpClient client) {
		HttpResponse response = null;
		HttpEntity entity = null;
		try {
			response = client.execute(request);
			if (response != null) {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine != null) {
					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200 || statusCode == 206) {
						entity = response.getEntity();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entity;
	}

	public static void saveProgress(Context context, AppBean appBean,
			int progress) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putInt(appBean.getAppId(), progress);

		edit.commit();
	}

	public static void saveProgress2(Context context, String appId, int progress) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putInt(appId, progress);

		edit.commit();
	}

	public static void saveUpdateProgress(Context context, AppBean appBean,
			int progress) {
		SharedPreferences prefs = context.getSharedPreferences(
				UPDATE_PROCESS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putInt(appBean.getAppId(), progress);

		edit.commit();
	}

	public static void saveProgressStatus(Context context, String appId,
			boolean isdown) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_STATUS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putBoolean(appId, isdown);

		edit.commit();
	}

	public static void saveUpdateStatus(Context context, String appId,
			boolean isdown) {
		SharedPreferences prefs = context.getSharedPreferences(
				UPDATE_PROCESS_STATUS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putBoolean(appId, isdown);

		edit.commit();
	}

	public static boolean getProgressStatus(Context context, String appId) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_STATUS_FILE, Context.MODE_PRIVATE);
		return prefs.getBoolean(appId, false);
	}

	public static boolean getUpdateStatus(Context context, String appId) {
		SharedPreferences prefs = context.getSharedPreferences(
				UPDATE_PROCESS_STATUS_FILE, Context.MODE_PRIVATE);
		return prefs.getBoolean(appId, false);
	}

	public static void removeProgress(Context context, AppBean appBean) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_FILE, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.remove(appBean.getAppId());
		edit.commit();
	}

	public static int getProgress(Context context, AppBean appBean) {
		SharedPreferences prefs = context.getSharedPreferences(
				DOWNLOAD_PROCESS_FILE, Context.MODE_PRIVATE);
		return prefs.getInt(appBean.getAppId(), 0);
	}

	public static int getUpdateProgress(Context context, AppBean appBean) {
		SharedPreferences prefs = context.getSharedPreferences(
				UPDATE_PROCESS_FILE, Context.MODE_PRIVATE);
		return prefs.getInt(appBean.getAppId(), 0);
	}

	public static boolean isExistSdcard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);

	}

	@SuppressWarnings("deprecation")
	public static long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		// return freeBlocks * blockSize; //单位Byte
		// return (freeBlocks * blockSize)/1024; //单位KB
		return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
	}

	@SuppressWarnings("deprecation")
	public static long getSDAllSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// 获取单个数据块的大小(Byte)
		long blockSize = sf.getBlockSize();
		// 获取所有数据块数
		long allBlocks = sf.getBlockCount();
		// 返回SD卡大小
		// return allBlocks * blockSize; //单位Byte
		// return (allBlocks * blockSize)/1024; //单位KB
		return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
	}

	/**
	 * 判断是否是平板
	 * 
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.DONUT)
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * 获得内部存储路径
	 */
	public static String getInternalStorage(Context context) {
		return context.getFilesDir().getAbsolutePath();
	}

	/**
	 * 安装app
	 */
	public static void installApp(Context context, File file) {
		Uri uri = Uri.fromFile(file); // 这里是APK路径
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/**
	 * 卸载app
	 */
	public static void unInstallApp(Context context, String packageName) {
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);

	}

	/**
	 * 判断是否获取root
	 */
	public static synchronized boolean getRootAhth() {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("exit\n");
			os.flush();
			int exitValue = process.waitFor();
			if (exitValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 自动安装, app须安装到系统app目录下，system/app
	 */
	// public static void autoInstall(Context context, String apkPath,String
	// apkName) {
	// File file = new File(apkPath);
	// if( !file.exists())
	// return ;
	// Uri mPackageURI = Uri.fromFile(file);
	// int installFlags = 0;
	// PackageManager pm = context.getPackageManager();
	// PackageInfo info = pm.getPackageArchiveInfo(apkPath,
	// PackageManager.GET_ACTIVITIES);
	// if(info != null){
	// try {
	// PackageInfo pi =
	// pm.getPackageInfo(info.packageName,PackageManager.GET_UNINSTALLED_PACKAGES);
	// if( pi != null){
	// installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
	// }
	// } catch (NameNotFoundException e) {
	// }
	// //把包名和apkName对应起来，后面需要使用
	// map.put(info.packageName, apkName);
	// IPackageInstallObserver observer = new PackageInstallObserver();
	// pm.installPackage(mPackageURI, observer, installFlags, info.packageName);
	//
	// }
	// }

	/**
	 * 获取网络图片，以字节返回
	 * 
	 * @param url
	 * @return
	 */
	public static byte[] getBytes(String url) {
		byte[] result = null;

		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest request = new HttpGet(url);
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				byte[] buf = new byte[1024 * 4];
				int len;
				baos = new ByteArrayOutputStream();
				while ((len = is.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, len);
				}

				result = baos.toByteArray();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}

				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static InputStream loadUrl(String url) {

		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest request = new HttpGet(url);
		InputStream is = null;
		try {
			HttpResponse response = client.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	public static byte[] getBytes(InputStream is) {
		byte[] result = null;

		ByteArrayOutputStream baos = null;
		try {
			byte[] buf = new byte[1024 * 4];
			int len;
			baos = new ByteArrayOutputStream();
			while ((len = is.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, len);
			}
			result = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}

				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return result;
	}

	public static File getFile(String dir, String name, InputStream is) {
		File file = null;
		FileOutputStream fos = null;

		try {
			File dirDir = new File(dir);
			if (!dirDir.exists()) {
				dirDir.mkdirs();
			}

			File tmpFile = File.createTempFile(name, ".tmp", dirDir);

			byte[] buf = new byte[1024 * 4];
			int len;
			fos = new FileOutputStream(tmpFile);
			while ((len = is.read(buf, 0, buf.length)) != -1) {
				fos.write(buf, 0, len);
			}
			file = new File(dirDir, name);
			if (!tmpFile.renameTo(file)) {
				file = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}

				if (fos != null) {
					fos.flush();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return file;
	}

}
