package com.way.locus;

import java.util.zip.Inflater;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.EUExLockPattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.way.locus.LocusPassWordView.OnCompleteListener;
import com.way.util.CallBackErrorCount;
import com.way.util.UtilModel;

public class LoginActivity extends Activity implements OnClickListener {

	private LocusPassWordView lpwv;
	private Toast toast;
	private String result = "";
	private String afreshOrOpen;
	public static final String INTENT_KEY_AFRESH_OR_OPEN = "intentKeyOpen";
	public static final String INTENT_KEY_TEXTVIEW = "titleTab";
	public static final String INTENT_KEY_AFRESH_LOGIN = "loginIsTrueOrFalse";

	/**
	 * data:image/gif;base64,base64编码的gif图片数据
	 * data:image/png;base64,base64编码的png图片数据
	 * data:image/jpg;base64,base64编码的jpeg图片数据
	 * data:image/x-icon;base64,base64编码的icon图片数据
	 */
	public static final String IMAGE_GIF = "data:image/gif;base64,";
	public static final String IMAGE_PNG = "data:image/png;base64,";
	public static final String IMAGE_JPEG = "data:image/jpg;base64,";
	public static final String IMAGE_X_ICON = "data:image/x-icon;base64,";

	private ImageView backIcon;
	// private Toast toastTv;
	private TextView textView;

	private TextView forgetTextView;
	// private TextView otherTextView;

	private boolean isSuccess = true;
	private boolean isLogin = false;
	private final static String TAG = "LoginActivity";
	private final static int forgetId = 1234567890, otherId = 1234567891;

	private LinearLayout lineBotton;

	private UtilModel utilModel;

	private int loginCount = 0;

	String patternStringCount;

	private String app_Id;
	private String lockBack;

	public static final String CHECK_PASSWORK = "check_passwork";
	public static final String LOCKBACK = "lockBack";

	@SuppressWarnings("unused")
	private void showToast(CharSequence message) {
		if (null == toast) {
			toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	private CallBackErrorCount callError = new CallBackErrorCount() {

		@Override
		public void getErrorCount(final int count) {
			// TODO Auto-generated method stub
			Log.i(TAG, "loginCount=" + loginCount);
			// toastTv.setTextColor(Color.parseColor("#eb5353"));
			// Animation shake =
			// AnimationUtils.loadAnimation(LoginActivity.this,
			// EUExUtil.getResAnimID("shake"));// 加载动画资源文件
			// toastTv.startAnimation(shake); // 给组件播放动画效果
			// toastTv.setText("密码错了,还可以输入" + (loginCount - count) + "次");
			// toastTv.show();
			Toast toast = Toast.makeText(getApplicationContext(), "密码错了,还可以输入"
					+ (loginCount - count) + "次", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			EUExLockPattern.con.getError(count + "");
		}

		@Override
		public void getError(String error) {
			textView.setText(error);
			textView.setTextColor(Color.RED);
		}
	};

	@TargetApi(Build.VERSION_CODES.FROYO)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(EUExUtil.getResLayoutID("login_activity"));
		app_Id = getIntent().getStringExtra(CHECK_PASSWORK);
		lockBack = getIntent().getStringExtra(LOCKBACK);
		String check = Utils.getCheckPasswork(this);
		Log.i("onCreate", "check=====" + check);
		if (check == null) {
			String cipher = ShaPrefUtils.getCheckPasswork(LoginActivity.this,
					Contains.FILLOCKNAME, Contains.KEYELOCKNAME);
			String md5Str = MD5.getMD5Code(cipher + app_Id);
			Log.i("onCreate", "md5Str=====" + md5Str);
			Utils.setString(this, md5Str);
			Utils.setCheckPasswork(this, app_Id);
		}
		backIcon = (ImageView) findViewById(EUExUtil.getResIdID("backIcon"));
		backIcon.setOnClickListener(this);

		if (lockBack.equals("0")) {
			backIcon.setVisibility(View.INVISIBLE);
		} else if (lockBack.equals("1")) {
			backIcon.setVisibility(View.VISIBLE);
		}

		// toastTv = (TextView)
		// findViewById(EUExUtil.getResIdID("login_errorId"));
		textView = (TextView) findViewById(EUExUtil.getResIdID("imageViewId"));
		forgetTextView = (TextView) findViewById(EUExUtil
				.getResIdID("textViewForId"));
		// otherTextView = (TextView) findViewById(EUExUtil
		// .getResIdID("textViewOtherId"));

		lineBotton = (LinearLayout) findViewById(EUExUtil
				.getResIdID("layoutId"));

		// 获取到开标志位。
		afreshOrOpen = getIntent().getStringExtra(INTENT_KEY_AFRESH_OR_OPEN);
		isLogin = getIntent().getBooleanExtra(INTENT_KEY_AFRESH_LOGIN, false);

		textView.setBackgroundResource(EUExUtil.getResDrawableID("aa"));
		lpwv = (LocusPassWordView) this.findViewById(EUExUtil
				.getResIdID("mLocusPassWordView"));
		loginCount = Integer.parseInt(ShaPrefUtils.getCheckPasswork(
				LoginActivity.this, Contains.ERRORNAME, Contains.ERRORNAME));
		;

		utilModel = new UtilModel();

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory
					.decodeResource(
							LoginActivity.this.getResources(),
							EUExUtil.getResDrawableID("plugin_uexlocalpattern_local_toal"),
							options);
			DisplayMetrics displayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			int height = (int) (options.outHeight);
			if (lpwv != null) {
				lpwv.setUtilModel(utilModel);
				lpwv.setCallError(callError);
				lpwv.height = height / 2 - (60 * displayMetrics.density);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isLogin) {
			LinearLayout.LayoutParams lineBu = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			LinearLayout.LayoutParams lineButton = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);

			LinearLayout line = new LinearLayout(LoginActivity.this);
			line.setGravity(Gravity.CENTER);
			forgetTextView = new TextView(LoginActivity.this);
			forgetTextView.setText("忘记手势密码？");
			forgetTextView.setTextColor(Color
					.parseColor(ConstantFinal.LOCKPATTERN_SKIP_COLOR));
			forgetTextView.setTextSize(15);
			forgetTextView.setId(forgetId);
			forgetTextView.setGravity(Gravity.CENTER);
			forgetTextView.setOnClickListener(LoginActivity.this);
			line.addView(forgetTextView, lineButton);

			// otherTextView = new TextView(LoginActivity.this);
			// otherTextView = new TextView(LoginActivity.this);
			// otherTextView.setText("用其他用户登录");
			// otherTextView.setTextSize(15);
			// otherTextView.setId(otherId);
			// otherTextView.setTextColor(Color.parseColor("#555555"));
			// otherTextView.setGravity(Gravity.CENTER);
			// otherTextView.setOnClickListener(LoginActivity.this);
			// line.addView(otherTextView, lineButton);
			lineBotton.addView(line, lineBu);
		}

		lpwv.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				String cipher = ShaPrefUtils.getCheckPasswork(
						LoginActivity.this, Contains.FILLOCKNAME,
						Contains.KEYELOCKNAME);
				if (cipher.equals(MD5.getMD5Code(mPassword + app_Id))) {
					// showToast("登陆成功！");
					// Toast.makeText(LoginActivity.this, "登陆成功",
					// Toast.LENGTH_SHORT).show();
					result = 0 + "";
					isSuccess = true;

				} else {
					lpwv.setError(true);
					Log.i("lpwv", "lpwv.setError(true)=" + lpwv.isError());
					int k = utilModel.getCountError();
					k++;
					utilModel.setCountError(k);
					isSuccess = false;
					lpwv.markError();
					callError.getErrorCount(utilModel.getCountError());
				}
				if (isSuccess) {
					EUExLockPattern.con.getResult(result);
				}
			}

			@Override
			public void setTextView(String str) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@SuppressWarnings("deprecation")
	public void changeViewToDrawPassword(String logString) {
		if (logString != null && logString.startsWith("http://")) {
			if (GetWorkNetBitmap.isNetworkAvailable(this)) {
				visitNetWork(logString);
			}
		} else {
			if (getBase64(logString) != null) {
				textView.setBackgroundDrawable(new BitmapDrawable(
						stringtoBitmap(getBase64(logString))));
			}
		}
	}

	public String getBase64(String strBase) {
		if (strBase.startsWith(IMAGE_GIF)) {
			return strBase.replace(IMAGE_GIF, "");
		} else if (strBase.startsWith(IMAGE_PNG)) {
			return strBase.replace(IMAGE_PNG, "");
		} else if (strBase.startsWith(IMAGE_JPEG)) {
			return strBase.replace(IMAGE_JPEG, "");
		} else if (strBase.startsWith(IMAGE_X_ICON)) {
			return strBase.replace(IMAGE_X_ICON, "");
		}
		return null;
	}

	// 用于访问网络获取数据。
	public void visitNetWork(String url) {
		Log.i(TAG, "url=" + url);
		MyAsyncTask task = null;
		try {
			Log.i(TAG, "TextUtils=" + TextUtils.isEmpty(url));
			// 判断url路径
			task = new MyAsyncTask();
			task.execute(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyAsyncTask extends AsyncTask<String, String, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			Log.i(TAG, "params[0]=" + params[0]);
			try {
				return GetWorkNetBitmap.getHttpBitmap(params[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result != null) {
				textView.setBackgroundDrawable(new BitmapDrawable(result));
			}
		}
	}

	// data:image/jpg;base64,xxxxxxxxxx
	@SuppressLint("NewApi")
	public Bitmap stringtoBitmap(String string) {
		// 将字符串转换成Bitmap类型
		Bitmap bitmap;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
			return GetWorkNetBitmap.toRoundBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == forgetId) {
			Log.i("forgetId", "forgetId===forgetId");
			EUExLockPattern.con.getForget();
		} else if (v.getId() == otherId) {
			EUExLockPattern.con.getother();
		} else if (v.getId() == EUExUtil.getResIdID("backIcon")) {
			bList.backClick();
		}
	}

	private static lockBackList bList = null;

	public void setLockBackList(lockBackList list) {
		this.bList = list;
	}

	public interface lockBackList {
		void backClick();
	}
}
