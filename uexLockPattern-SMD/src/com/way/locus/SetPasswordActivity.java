package com.way.locus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.EUExLockPattern;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.way.locus.LocusPassWordView.OnCompleteListener;
import com.way.util.StringUtil;

public class SetPasswordActivity extends Activity implements OnClickListener {
	private String password;
	@SuppressWarnings("unused")
	private boolean needverify = true;

	private ImageView backIcon;
	private TextView textView_setpsd_info;
	private TextView resetTextView;

	private String app_Id;
	private String isShowBack;

	public static final String SETPASSWORD = "password_MD5";
	public static final String SHOWBACK = "showBack";

	private Context mContext;

	private TextView textViewSetUp;
	private GridView gridView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mContext = this;
			app_Id = getIntent().getStringExtra(SETPASSWORD);
			isShowBack = getIntent().getStringExtra(SHOWBACK);
			Utils.setCheckPasswork(this, app_Id);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			setContentView(EUExUtil.getResLayoutID("setpassword_activity"));

			textView_setpsd_info = (TextView) findViewById(EUExUtil
					.getResIdID("setUpId"));
			resetTextView = (TextView) findViewById(EUExUtil
					.getResIdID("resetPassword"));
			resetTextView.setOnClickListener(this);
			resetTextView.setText(ConstantFinal.LOCKPATTERN_SKIP);
			resetTextView.setTextColor(Color
					.parseColor(ConstantFinal.LOCKPATTERN_SKIP_COLOR));
			backIcon = (ImageView) findViewById(EUExUtil.getResIdID("backIcon"));
			backIcon.setOnClickListener(this);
			if (isShowBack.equals("0")) {
				backIcon.setVisibility(View.INVISIBLE);
			} else if (isShowBack.equals("1")) {
				backIcon.setVisibility(View.VISIBLE);
			}
			initView();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 将view添加到LineareLayout中
	public void setPatter(String password1) {
		gridView = (GridView) findViewById(EUExUtil.getResIdID("gridViewId"));
		String localPatter = password1;
		Log.i("localPatter", "localPatter===" + localPatter);
		int numberCou = 3;
		int countK = numberCou * numberCou;
		textViewSetUp = (TextView) findViewById(EUExUtil.getResIdID("setUpId"));

		textViewSetUp.setText("");

		int intDraw[] = new int[countK];
		if (localPatter != null) {
			String patterCount[] = localPatter.split(",");
			for (int i = 0; i < countK; i++) {
				intDraw[i] = (EUExUtil
						.getResDrawableID("plugin_uexlockpatternex_yuandian"));
				for (int j = 0; j < patterCount.length; j++) {
					if (i == Integer.parseInt(patterCount[j])) {
						Log.i("localPatter",
								"EUExUtil===" + i
										+ Integer.parseInt(patterCount[j]));
						intDraw[i] = (EUExUtil
								.getResDrawableID("plugin_uexlockpatternex_pitch"));
					}
				}
			}
		} else {
			for (int i = 0; i < countK; i++) {

				Log.i("localPatter", "localPatter===-----" + i);
				intDraw[i] = (EUExUtil
						.getResDrawableID("plugin_uexlockpatternex_yuandian"));
			}
		}

		List<HashMap<String, Object>> listHashIage = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < countK; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", intDraw[i]);
			listHashIage.add(map);
		}
		SimpleAdapter saImageItems = new SimpleAdapter(this, listHashIage,
				EUExUtil.getResLayoutID("plugin_uexlocpatterex_item"),
				new String[] { "itemImage" },
				new int[] { EUExUtil.getResIdID("imageViewId") });

		gridView.setAdapter(saImageItems);
	}

	// 第一次输入，手势密码。
	public void initView() {
		setPatter(null);
		// 设置手势密码
		try {
			final LocusPassWordView lpwv;
			final LinearLayout layout = (LinearLayout) this
					.findViewById(EUExUtil.getResIdID("mLocusPassWordView"));
			lpwv = new LocusPassWordView(SetPasswordActivity.this);
			if (lpwv != null) {
				lpwv.height = 90;
			}
			final LinearLayout.LayoutParams layoutParm = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layout.addView(lpwv, layoutParm);
			
			int layWidth=lpwv.getWidth();
			int textWidth=textView_setpsd_info.getWidth();

			lpwv.setOnCompleteListener(new OnCompleteListener() {
				@Override
				public void onComplete(String mPassword) {
					password = mPassword;
					Log.i("mPassword", "mPassword=" + mPassword);
					if (StringUtil.isNotEmpty(password)) {
						// setString(mPassword);
						setPatter(password);
						layout.removeAllViews();
						try {
							lpwv.setTempPassWord(MD5.getMD5Code(mPassword
									+ app_Id));
							Thread.sleep(500);
							harvestView();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void setTextView(String str) {
					// TODO Auto-generated method stub
					textView_setpsd_info.setText(str);
				}
			});
			if (lpwv.isPasswordEmpty()) {
				this.needverify = false;
				if(layWidth>textWidth){
					textView_setpsd_info
					.setText(ConstantFinal.LOCKPATTERN_DRAW_INIT_FRISDT);
				}else{
					textView_setpsd_info
					.setText(ConstantFinal.LOCKPATTERN_DRAW_INIT_FRISDT2);
				}
				
				textView_setpsd_info.setTextColor(Color
						.parseColor(ConstantFinal.LOCKPATTERN_SKIP_INIT_COLOR));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 第二次输入密码。
	public void harvestView() {
		textView_setpsd_info
				.setText(ConstantFinal.LOCKPATTERN_DRAW_INIT_SECOND);
		textView_setpsd_info.setTextColor(Color
				.parseColor(ConstantFinal.LOCKPATTERN_SKIP_INIT_COLOR));
		final LocusPassWordView lpwv2 = new LocusPassWordView(
				SetPasswordActivity.this);
		final LinearLayout layout2 = (LinearLayout) this.findViewById(EUExUtil
				.getResIdID("mLocusPassWordView"));
		LinearLayout.LayoutParams layoutParm2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout2.addView(lpwv2, layoutParm2);
		if (lpwv2 != null) {
			lpwv2.height = 90;
		}
		lpwv2.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				password = mPassword;
				Log.i("mPassword", "mPassword=" + mPassword);
				if (StringUtil.isNotEmpty(password)) {
					String md5_passwork = MD5.getMD5Code(mPassword + app_Id);
					boolean isVery = lpwv2.verifyPassword(md5_passwork);
					Log.i("mPassword", "very----------=" + isVery);
					if (isVery) {
						Utils.setString(mContext, md5_passwork);
						String count1 = ShaPrefUtils.getCheckPasswork(
								SetPasswordActivity.this,
								Contains.FILECONTNAME, Contains.KEYECONTNAME);
						int count = Integer.parseInt(count1);
						if (count > 1) {
							EUExLockPattern.con.getResult("1");
						} else {
							EUExLockPattern.con.getResult("2");
						}
					} else {
						textView_setpsd_info
								.setText(ConstantFinal.SETPASSWORD_ERROR);
						lpwv2.setError(true);
						lpwv2.markError();
						// resetTextView.setText("重新设置手势");
						// resetTextView.setOnClickListener(new
						// OnClickListener() {
						//
						// @Override
						// public void onClick(View v) {
						// // TODO Auto-generated method stub
						// // textView.setText("");
						// textView.setTextColor(Color.BLACK);
						// textView.setText("绘制解锁密码 ");
						// layout2.removeAllViews();
						// initView();
						//
						// }
						// });
						// textView.setTextColor(Color.parseColor("#eb5353"));
						// Animation shake = AnimationUtils.loadAnimation(
						// SetPasswordActivity.this,
						// EUExUtil.getResAnimID("shake"));// 加载动画资源文件
						// textView.startAnimation(shake); // 给组件播放动画效果
						// textView.setText("与上一次绘制不一致，请重新绘制 ");
					}
				}
			}

			@Override
			public void setTextView(String str) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == EUExUtil.getResIdID("backIcon")) {
			bList.setPasswordBack();
			EUExLockPattern.con.getResult("3");
		} else if (v.getId() == EUExUtil.getResIdID("resetPassword")) {
			EUExLockPattern.con.getResult("3");
			bList.setPasswordBack();
		}

	}

	private static setPasswordBackList bList = null;

	public void setBackList(setPasswordBackList list) {
		this.bList = list;
	}

	public interface setPasswordBackList {
		void setPasswordBack();
	}
}
