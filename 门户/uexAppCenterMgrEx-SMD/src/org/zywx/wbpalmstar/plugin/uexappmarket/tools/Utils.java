package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Utils {

	private static Context mContext;
	private static Utils sInstance = null;
	// private static AsyncTask<Void, Integer, Object> downloadTask = null;
	static final String subDir = "/Download/";
	static String downPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + subDir;

	public static Utils getInstance(Context context) {

		if (sInstance == null) {
			sInstance = new Utils(context);
		}
		return sInstance;
	}

	private Utils(Context context) {
		mContext = context;
	}

	private static DownloadTask mDownloadTask = null;

	static class DownloadTask extends AsyncTask<Void, Integer, Object> {
		String url;
		Context mContext;
		ProgressDialog progressDialog;

		public DownloadTask(Context context, String url) {
			this.mContext = context;
			this.url = url;
		}

		@Override
		protected void onPreExecute() {
			// progressDialog = ProgressDialog.show(this.mContext, "下载",
			// "下载进度");
			progressDialog = new ProgressDialog(mContext);
			// progressDialog.setMessage("0%");

			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// STYLE_SPINNER
																				// //STYLE_HORIZONTAL
			progressDialog.setProgress(0);
			progressDialog.show();
		}

		@Override
		protected Object doInBackground(Void... params) {
			String fileName = url.substring(url.lastIndexOf("/"));

			File tmpFile = new File(downPath);
			if (!tmpFile.exists()) {
				tmpFile.mkdirs();
			}

			File file = new File(downPath + fileName);
			FileOutputStream fos = null;
			InputStream is = null;
			HttpUriRequest request = null;
			try {

				request = new HttpGet(url);
				HttpClient httpClient = new DefaultHttpClient();
				HttpResponse httpResponse = httpClient.execute(request);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
				long total = httpEntity.getContentLength();

				int progress = 0;
				fos = new FileOutputStream(file);
				byte[] buf = new byte[256];
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (HttpStatus.SC_OK != statusCode) {
					Toast.makeText(mContext, "连接错误", Toast.LENGTH_SHORT).show();
				} else {
					while (true) {
						if (is != null) {
							int numRead = is.read(buf);
							if (numRead <= 0) {
								break;
							} else {
								fos.write(buf, 0, numRead);
								progress += numRead;
								int percent = (int) ((double) progress * 100 / (double) total);
								publishProgress(percent);
							}

						} else {
							break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
					if (is != null) {
						is.close();
					}
					if (request != null) {
						request.abort();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return file;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setProgress(values[0]);
			// progressDialog.setMessage( values[0]+"%");
		}

		@Override
		protected void onPostExecute(Object result) {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
			File file = (File) result;
			if (file != null && file.exists()) {
				Toast.makeText(mContext, "下载成功 ", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, "下载失败 ", Toast.LENGTH_SHORT).show();
				return;
			}
			openInstallFile(mContext, file);

			mDownloadTask = null;
		}

	}

	public static void download(Context ctx, String url) {
		if (mDownloadTask == null) {
			mDownloadTask = new DownloadTask(ctx, url);
			mDownloadTask.execute();
		}
	}

	public static File downloadFile(String httpUrl) {
		String fileName = httpUrl.substring(httpUrl.lastIndexOf("/"));
		File tmpFile = new File(downPath);
		if (!tmpFile.exists()) {
			tmpFile.mkdirs();
		}

		File file = new File(downPath + fileName);
		FileOutputStream fos = null;
		InputStream is = null;
		HttpUriRequest request = null;
		try {

			request = new HttpGet(httpUrl);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(request);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
			long total = httpEntity.getContentLength();

			// URL url = new URL(httpUrl);
			// HttpURLConnection conn = (HttpURLConnection)
			// url.openConnection();
			// InputStream is = conn.getInputStream();

			int progress = 0;
			fos = new FileOutputStream(file);
			byte[] buf = new byte[256];
			// conn.connect();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (HttpStatus.SC_OK != statusCode) {
				Toast.makeText(mContext, "连接错误", Toast.LENGTH_SHORT).show();
			} else {
				while (true) {
					if (is != null) {
						int numRead = is.read(buf);
						if (numRead <= 0) {
							break;
						} else {
							fos.write(buf, 0, numRead);
							progress += numRead;
							int percent = (int) ((double) progress * 100 / (double) total);
						}

					} else {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
				if (request != null) {
					request.abort();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	private static void openInstallFile(Context context, File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static File rename(File tmpFile) {
		if (tmpFile == null) {
			return null;
		}
		if (tmpFile.exists()) {
			if (!tmpFile.getAbsolutePath().endsWith(".zip")) {
				String path = tmpFile.getAbsolutePath();
				path = path.substring(0, path.lastIndexOf(".")) + ".zip";
				File newFile = new File(path);
				if (tmpFile.renameTo(newFile)) {
					tmpFile = newFile;
				}
			}
			return tmpFile;
		}
		return null;
	}

	public static File renameApk(File tmpFile) {
		return rename(tmpFile, "apk");
	}

	/**
	 */
	public static File rename(File tmpFile, String suffix) {
		if (tmpFile == null) {
			return null;
		}
		if (tmpFile.exists()) {
			if (!tmpFile.getAbsolutePath().endsWith(".zip")) {
				String path = tmpFile.getAbsolutePath();
				path = path.substring(0, path.lastIndexOf(".")) + "." + suffix;
				File newFile = new File(path);
				if (tmpFile.renameTo(newFile)) {
					tmpFile = newFile;
				}
			}
			return tmpFile;
		}
		return null;
	}

	/**
	 * 0xffebeff8, 0xffd9dce5;
	 * 
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @param rows
	 * @param columns
	 * @param paintWidth
	 * @param bodyColor
	 * @param strokeColor
	 * @return
	 */
	public static Bitmap drawGridBackgroung(int left, int top, int width,
			int height, int rows, int columns, float paintWidth, int bodyColor,
			int strokeColor) {
		if (rows == 0 || columns == 0) {
			return null;
		}
		if (bitmap == null) {

			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);

			// 绘制背景
			Paint paint = new Paint();
			paint.setColor(bodyColor);
			paint.setStyle(Paint.Style.FILL);
			Rect r = new Rect(left, top, width + left, height + top);

			canvas.drawRect(r, paint);
			// 绘制边框
			paint.setColor(strokeColor);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(paintWidth);

			// 绘制行
			for (int i = 0; i <= rows; i++) {
				int h = (int) ((height - paintWidth) / rows);
				int y = (int) (h * i + top + paintWidth / 2);
				// canvas.drawLine(left, y, left + width, y, paint);
			}
			// 绘制行
			for (int i = 0; i <= columns; i++) {
				int w = (int) ((width - paintWidth) / columns);
				int x = (int) (w * i + left + paintWidth / 2);
				// canvas.drawLine(x, top, x, top + height, paint);
			}
		}
		return bitmap;
	}

	private static Bitmap bitmap;

}
