package com.way.locus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.way.util.BitmapUtil;
import com.way.util.CallBackErrorCount;
import com.way.util.MathUtil;
import com.way.util.RoundUtil;
import com.way.util.StringUtil;
import com.way.util.UtilModel;

/**
 * 
 * 九宫格解锁
 * 
 * @author way
 * 
 */
public class LocusPassWordView extends View {
	private float w = 0;
	private float h = 0;
	private String TAG = "LocusPassWordView";
	private int arrCount = (Integer.parseInt(this.getContext()
			.getSharedPreferences("com.way.locus.preferences", 0)
			.getString("com.way.locus.preferences", null)));
	//
	private boolean isCache = false;
	//
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	// 用于表示生成的宫格个数
	private Point[][] mPoints = new Point[arrCount][arrCount];
	// 圆的半径
	private float rA = 0;
	// 选中的点
	private List<Point> sPoints = new ArrayList<Point>();
	private boolean checking = false;
	private Bitmap locus_round_original;// 圆点初始状态时的图片
	private Bitmap locus_round_click;// 圆点点击时的图片
	private Bitmap locus_round_click_error;// 出错时圆点的图片
	private Bitmap locus_line;// 正常状态下线的图片
	private Bitmap locus_line_semicircle;
	private Bitmap locus_line_semicircle_error;
	private Bitmap locus_arrow;// 线的移动方向
	private Bitmap locus_line_error;// 错误状态下的线的图片
	// private long CLEAR_TIME = 0;// 清除痕迹的时间
	private int passwordMinLength = (Integer.parseInt(this.getContext()
			.getSharedPreferences("com.way.locus.preferences", 0)
			.getString("com.way.locus.preferences", null)) - 1);// 密码最小长度
	private boolean isTouch = true; // 是否可操作
	private Matrix mMatrix = new Matrix();
	private int lineAlpha = 50;// 连线的透明度

	private CallBackInit call;
	private UtilModel utilModel;
	private CallBackErrorCount callError;

	private Point pointA;
	private Point pointB;

	public float height = 0;

	float blW = 0f;
	float blH = 0f;

	public CallBackErrorCount getCallError() {
		return callError;
	}

	public void setCallError(CallBackErrorCount callError) {
		this.callError = callError;
	}

	public UtilModel getUtilModel() {
		return utilModel;
	}

	public void setUtilModel(UtilModel utilModel) {
		this.utilModel = utilModel;
	}

	public CallBackInit getCall() {
		return call;
	}

	public void setCall(CallBackInit call) {
		this.call = call;
	}

	// 设置方向箭头
	private boolean isError = false;

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public LocusPassWordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LocusPassWordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}

	public LocusPassWordView(Context context) {
		super(context);
		init();
	}

	private void init() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) this.getContext()).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		float screenWidth = displayMetrics.widthPixels;
		float screenHeight = displayMetrics.heightPixels;

		blW = screenWidth / 750;
		blH = screenHeight / 1334;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (!isCache) {
			initCache();
		}
		drawToCanvas(canvas);
	}

	private void drawToCanvas(Canvas canvas) {

		// 画所有点
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (p.state == Point.STATE_CHECK) {
					canvas.drawBitmap(locus_round_click, p.x - rA, p.y - rA,
							mPaint);
				} else if (p.state == Point.STATE_CHECK_ERROR) {
					canvas.drawBitmap(locus_round_click_error, p.x - rA, p.y
							- rA, mPaint);
				} else {
					canvas.drawBitmap(locus_round_original, p.x - rA, p.y - rA,
							mPaint);
				}
			}
		}

		// 画连线
		if (sPoints.size() > 0) {
			int tmpAlpha = mPaint.getAlpha();
			mPaint.setAlpha(lineAlpha);
			Point tp = sPoints.get(0);
			for (int i = 1; i < sPoints.size(); i++) {
				Point p = sPoints.get(i);
				// drawLine(canvas, tp, p);
				getDrawLine(canvas, tp, p);
				tp = p;
			}
			try {
				if (this.movingNoPoint) {
					getDrawLine(canvas, tp, new Point((int) moveingX,
							(int) moveingY));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mPaint.setAlpha(tmpAlpha);
			lineAlpha = mPaint.getAlpha();
		}

	}

	/**
	 * 初始化Cache信息
	 * 
	 * @param canvas
	 */
	private void initCache() {

		w = this.getWidth();
		h = this.getHeight();

		float x = 0;
		float y = 0;

		// 以最小的为准
		// 纵屏
		if (w > h) {
			x = (w - h) / 2;
			w = h;
		}
		// 横屏
		else {
			y = (h - w) / 2;
			h = w;
		}

		locus_round_original = BitmapFactory.decodeResource(
				this.getResources(),
				EUExUtil.getResDrawableID("locus_round_original"));
		locus_round_click = BitmapFactory.decodeResource(this.getResources(),
				EUExUtil.getResDrawableID("locus_round_click"));
		locus_round_click_error = BitmapFactory.decodeResource(
				this.getResources(),
				EUExUtil.getResDrawableID("locus_round_click_error"));

		locus_line = BitmapFactory.decodeResource(this.getResources(),
				EUExUtil.getResDrawableID("locus_line"));
		locus_line_semicircle = BitmapFactory.decodeResource(
				this.getResources(),
				EUExUtil.getResDrawableID("locus_line_semicircle"));

		locus_line_error = BitmapFactory.decodeResource(this.getResources(),
				EUExUtil.getResDrawableID("locus_line_error"));
		locus_line_semicircle_error = BitmapFactory.decodeResource(
				this.getResources(),
				EUExUtil.getResDrawableID("locus_line_semicircle_error"));

		locus_arrow = BitmapFactory.decodeResource(this.getResources(),
				EUExUtil.getResDrawableID("locus_arrow"));

		// Log.d("Canvas w h :", "w:" + w + " h:" + h);

		// 计算圆圈图片的大小
		float canvasMinW = w;
		if (w > h) {
			canvasMinW = h;
		}
		float roundMinW = canvasMinW / 4.0f;
		float roundW = roundMinW / 2.f;
		//
		float deviation = canvasMinW % (8 * 2) / 2;
		x += deviation;
		x += deviation;

		if (locus_round_original.getWidth() > roundMinW) {
			float sf = roundMinW * 1.0f / locus_round_original.getWidth() * 4; // 取得缩放比例，将所有的图片进行缩放
			// float sf = blW; // 取得缩放比例，将所有的图片进行缩放
			locus_round_original = BitmapUtil.zoom(locus_round_original, sf);
			locus_round_click = BitmapUtil.zoom(locus_round_click, sf);
			locus_round_click_error = BitmapUtil.zoom(locus_round_click_error,
					sf);

			locus_line = BitmapUtil.zoom(locus_line, sf);
			locus_line_semicircle = BitmapUtil.zoom(locus_line_semicircle, sf);

			locus_line_error = BitmapUtil.zoom(locus_line_error, sf);
			locus_line_semicircle_error = BitmapUtil.zoom(
					locus_line_semicircle_error, sf);

			locus_arrow = BitmapUtil.zoom(locus_arrow, sf);
			roundW = locus_round_original.getWidth() / 2;
		}

		x = 0;
		y = 0;
		h = this.getHeight();
		w = this.getWidth();

		roundW = roundW / 2 + 126 * blW;
		float roundH = 126 * blW;
		float inter = 2 * rA;
		mPoints[0][0] = new Point(x + 0 + inter + roundW, y + inter + roundH);
		mPoints[0][1] = new Point(w / 2, y + inter + roundH);
		mPoints[0][2] = new Point(w - inter - roundW, y + inter + roundH);

		mPoints[1][0] = new Point(x + 0 + inter + roundW, y + h / 2 + inter);
		mPoints[1][1] = new Point(w / 2, y + h / 2 + 2 * rA);
		mPoints[1][2] = new Point(w - inter - roundW, y + h / 2 + inter);

		mPoints[2][0] = new Point(x + 0 + inter + roundW, y + h - inter
				- roundH);
		mPoints[2][1] = new Point(w / 2, y + h - inter - roundH);
		mPoints[2][2] = new Point(w - inter - roundW, y + h - inter - roundH);

		int k = 0;
		for (Point[] ps : mPoints) {
			for (Point p : ps) {
				p.index = k;
				k++;
			}
		}
		rA = locus_round_original.getHeight() / 2;
		isCache = true;
	}

	/**
	 * 画两点的连接
	 * 
	 * @param canvas
	 * @param a
	 * @param b
	 */
	private void drawLine(Canvas canvas, Point a, Point b) {
		float ah = ((float) (MathUtil.distance(pointA.x, pointA.y, pointB.x,
				pointB.y)) - 2 * rA);
		float degrees = getDegrees(a, b);
		canvas.rotate(degrees, a.x, a.y);
		if (pointA.state == Point.STATE_CHECK_ERROR) {
			mMatrix.setScale(ah / locus_line_error.getWidth(), 1);
			mMatrix.postTranslate(a.x, a.y - locus_line_error.getHeight()
					/ 2.0f);
			canvas.drawBitmap(locus_line_error, mMatrix, mPaint);
		} else {
			mMatrix.setScale(ah / locus_line.getWidth(), 1);
			mMatrix.postTranslate(a.x, a.y - locus_line.getHeight() / 2.0f);
			canvas.drawBitmap(locus_line, mMatrix, mPaint);
		}
		canvas.rotate(-degrees, a.x, a.y);

	}

	public void getDrawLine(Canvas canvas, Point a, Point b) {
		float degrees = getDegrees(a, b);
		pointA = a;
		pointB = b;
		float sinValues = Math.abs((float) Math.sin(Math.toRadians(degrees))
				* rA);
		float cosValues = Math.abs((float) Math.cos(Math.toRadians(degrees))
				* rA);
		if (degrees == 90) { // 在y轴的下边 90
			drawLine(canvas, new Point((int) a.x, (int) a.y + rA), new Point(
					(int) b.x, (int) b.y - rA));
		} else if (degrees == 270) { // 在y轴的上边 270
			drawLine(canvas, new Point((int) a.x, (int) a.y - rA), new Point(
					(int) b.x, (int) b.y + rA));
		} else if (degrees == 0) {// 在x轴的右边边 90
			drawLine(canvas, new Point((int) a.x + rA, (int) a.y), new Point(
					(int) b.x - rA, (int) b.y));
		} else if (degrees == 180) {// 在x轴的左边边180
			drawLine(canvas, new Point((int) a.x - rA, (int) a.y), new Point(
					(int) b.x + rA, (int) b.y));
		} else if (b.x > a.x) {// 在y轴的右边 270~90
			if (b.y > a.y) // 在x轴的下边 0 - 90
			{
				drawLine(canvas, new Point((int) a.x + cosValues, (int) a.y
						+ sinValues), new Point((int) b.x - cosValues,
						(int) b.y - sinValues));
			} else if (b.y < a.y) // 在y轴的上边 270~0
			{
				drawLine(canvas, new Point((int) a.x + cosValues, (int) a.y
						- sinValues), new Point((int) b.x - cosValues,
						(int) b.y + sinValues));
			}
		} else if (b.x < a.x) {
			if (b.y > a.y) // 在y轴的下边 180 ~ 270
			{
				drawLine(canvas, new Point((int) a.x - cosValues, (int) a.y
						+ sinValues), new Point((int) b.x + cosValues,
						(int) b.y - sinValues));
			} else if (b.y < a.y) // 在y轴的上边 90 ~ 180
			{
				drawLine(canvas, new Point((int) a.x - cosValues, (int) a.y
						- sinValues), new Point((int) b.x + cosValues,
						(int) b.y + sinValues));
			}
		}
	}

	public float getDegrees(Point a, Point b) {
		float ax = a.x;// a.index % 3;
		float ay = a.y;// a.index / 3;
		float bx = b.x;// b.index % 3;
		float by = b.y;// b.index / 3;
		float degrees = 0;
		if (bx == ax) // y轴相等 90度或270
		{
			if (by > ay) // 在y轴的下边 90
			{
				degrees = 90;
			} else if (by < ay) // 在y轴的上边 270
			{
				degrees = 270;
			}
		} else if (by == ay) // y轴相等 0度或180
		{
			if (bx > ax) // 在y轴的下边 90
			{
				degrees = 0;
			} else if (bx < ax) // 在y轴的上边 270
			{
				degrees = 180;
			}
		} else {
			if (bx > ax) // 在y轴的右边 270~90
			{
				if (by > ay) // 在y轴的下边 0 - 90
				{
					degrees = 0;
					degrees = degrees
							+ switchDegrees(Math.abs(by - ay),
									Math.abs(bx - ax));
				} else if (by < ay) // 在y轴的上边 270~0
				{
					degrees = 360;
					degrees = degrees
							- switchDegrees(Math.abs(by - ay),
									Math.abs(bx - ax));
				}

			} else if (bx < ax) // 在y轴的左边 90~270
			{
				if (by > ay) // 在y轴的下边 180 ~ 270
				{
					degrees = 90;
					degrees = degrees
							+ switchDegrees(Math.abs(bx - ax),
									Math.abs(by - ay));
				} else if (by < ay) // 在y轴的上边 90 ~ 180
				{
					degrees = 270;
					degrees = degrees
							- switchDegrees(Math.abs(bx - ax),
									Math.abs(by - ay));
				}

			}

		}
		return degrees;
	}

	/**
	 * 1=30度 2=45度 4=60度
	 * 
	 * @param tan
	 * @return
	 */
	private float switchDegrees(float x, float y) {
		return (float) MathUtil.pointTotoDegrees(x, y);
	}

	/**
	 * 取得数组下标
	 * 
	 * @param index
	 * @return
	 */
	public int[] getArrayIndex(int index) {
		int[] ai = new int[2];
		ai[0] = index / 3;
		ai[1] = index % 3;
		return ai;
	}

	/**
	 * 检查
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Point checkSelectPoint(float x, float y) {
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (null != p
						&& RoundUtil.checkInRound(p.x, p.y, rA, (int) x,
								(int) y)) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * 重置
	 */
	private void reset() {
		for (Point p : sPoints) {
			p.state = Point.STATE_NORMAL;
		}
		sPoints.clear();
		this.enableTouch();
	}

	/**
	 * 判断点是否有交叉 返回 0,新点 ,1 与上一点重叠 2,与非最后一点重叠
	 * 
	 * @param p
	 * @return
	 */
	private int crossPoint(Point p) {
		// 重叠的不最后一个则 reset
		if (sPoints.contains(p)) {
			if (sPoints.size() > 2) {
				// 与非最后一点重叠
				if (sPoints.get(sPoints.size() - 1).index != p.index) {
					return 2;
				}
			}
			return 1; // 与最后一点重叠
		} else {
			return 0; // 新点
		}
	}

	/**
	 * 添加一个点
	 * 
	 * @param point
	 */
	private void addPoint(Point point) {
		this.sPoints.add(point);
	}

	/**
	 * 转换为String
	 * 
	 * @param points
	 * @return
	 */
	private String toPointString() {
		if (sPoints.size() > passwordMinLength) {
			StringBuffer sf = new StringBuffer();
			for (Point p : sPoints) {
				sf.append(",");
				sf.append(p.index);
			}
			return sf.deleteCharAt(0).toString();
		} else {
			return "";
		}
	}

	boolean movingNoPoint = false;
	float moveingX, moveingY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 不可操作
		if (!isTouch) {
			return false;
		}

		movingNoPoint = false;

		float ex = event.getX();
		float ey = event.getY();
		boolean isFinish = false;
		boolean redraw = false;
		Point p = null;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 点下
			// 如果正在清除密码,则取消
			if (task != null) {
				task.cancel();
				task = null;
				Log.d("task", "touch cancel()");
			}
			// 删除之前的点
			reset();
			p = checkSelectPoint(ex, ey);
			if (p != null) {
				checking = true;
			}
			break;
		case MotionEvent.ACTION_MOVE: // 移动
			if (checking) {
				p = checkSelectPoint(ex, ey);
				if (p == null) {
					movingNoPoint = true;
					moveingX = ex;
					moveingY = ey;
				}
			}
			break;
		case MotionEvent.ACTION_UP: // 提起
			p = checkSelectPoint(ex, ey);
			checking = false;
			isFinish = true;
			break;
		}
		if (!isFinish && checking && p != null) {

			int rk = crossPoint(p);
			if (rk == 2) // 与非最后一重叠
			{
				// reset();
				// checking = false;

				movingNoPoint = true;
				moveingX = ex;
				moveingY = ey;

				redraw = true;
			} else if (rk == 0) // 一个新点
			{
				p.state = Point.STATE_CHECK;
				addPoint(p);
				redraw = true;
			}
			// rk == 1 不处理

		}

		// 是否重画
		if (redraw) {

		}
		if (isFinish) {
			if (this.sPoints.size() == 1) {
				if (getUtilModel() != null) {
					int k = getUtilModel().getCountError();
					k++;
					getUtilModel().setCountError(k);
					Log.i(TAG, "k1=" + k + "getUtilModel+");
				}
				// this.reset();
				error();
				clearPassword();
				// if (Integer.parseInt(patternString) != 1)
				if (getUtilModel() != null) {
					getCallError()
							.getErrorCount(getUtilModel().getCountError());
				}
			} else if (this.sPoints.size() <= passwordMinLength
					&& this.sPoints.size() > 0) {
				// mCompleteListener.onPasswordTooMin(sPoints.size());
				if (getUtilModel() != null) {
					int k = getUtilModel().getCountError();
					k++;
					getUtilModel().setCountError(k);
					Log.i(TAG, "k2=" + k);
				}
				error();
				clearPassword();
				Toast toast = Toast.makeText(this.getContext(), "至少连接"
						+ arrCount + "个点，请重新输入", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				// mCompleteListener.setTextView("至少连接" + arrCount +
				// "个点，请重新输入");
				if (getUtilModel() != null) {
					getCallError()
							.getErrorCount(getUtilModel().getCountError());
				} else {
					try {
						getCallError().getError("至少连接" + arrCount + "个点，请重新输入");
						// mCompleteListener.setTextView("至少连接" + arrCount
						// + "个点，请重新输入");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (mCompleteListener != null) {
				if (this.sPoints.size() >= passwordMinLength) {
					this.disableTouch();
					mCompleteListener.onComplete(toPointString());
					if (getCall() != null) {
						getCall().getCallBack();
					} else {
						Log.i("toPointString", "toPointString" + getCall());
					}
				}
			}
		}
		this.postInvalidate();
		return true;
	}

	/**
	 * 设置已经选中的为错误
	 */
	void error() {
		for (Point p : sPoints) {
			p.state = Point.STATE_CHECK_ERROR;
		}
	}

	/**
	 * 设置为输入错误
	 */
	public void markError() {
		markError(Contains.ERROR_CLEAR_TIME);
	}

	/**
	 * 设置为输入错误
	 */
	public void markError(final long time) {
		for (Point p : sPoints) {
			p.state = Point.STATE_CHECK_ERROR;
		}
		this.clearPassword(time);
	}

	/**
	 * 设置为可操作
	 */
	public void enableTouch() {
		isTouch = true;
	}

	/**
	 * 设置为不可操作
	 */
	public void disableTouch() {
		isTouch = false;
	}

	private Timer timer = new Timer();
	private TimerTask task = null;

	/**
	 * 清除密码
	 */
	public void clearPassword() {
		clearPassword(Contains.ERROR_CLEAR_TIME);
	}

	/**
	 * 清除密码
	 */
	public void clearPassword(final long time) {
		if (time > 1) {
			if (task != null) {
				task.cancel();
				Log.d("task", "clearPassword cancel()");
			}
			lineAlpha = 130;
			postInvalidate();
			task = new TimerTask() {
				public void run() {
					reset();
					postInvalidate();
				}
			};
			Log.d("task", "clearPassword schedule(" + time + ")");
			timer.schedule(task, time);
		} else {
			reset();
			postInvalidate();
		}

	}

	//
	private OnCompleteListener mCompleteListener;

	/**
	 * @param mCompleteListener
	 */
	public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
		this.mCompleteListener = mCompleteListener;
	}

	/**
	 * 取得密码
	 * 
	 * @return
	 */
	// 获取存储在，本地的密码。
	private String getTempPassword() {
		SharedPreferences settings = this.getContext().getSharedPreferences(
				this.getClass().getName() + ".temp", 0);
		return settings.getString("com.way.locus.password.temp", ""); // ,
																		// "0,1,2,3,4,5,6,7,8"
	}

	/**
	 * 密码是否为空
	 * 
	 * @return
	 */
	public boolean isPasswordEmpty() {
		// Log.i("isPasswordEmpty", StringUtil.isEmpty(getPassword()+"");
		return StringUtil.isEmpty(getTempPassword());
	}

	public boolean verifyPassword(String password) {
		boolean verify = false;
		if (com.way.util.StringUtil.isNotEmpty(password)) {// 判断密码是否为空
			// 或者是超级密码

			if (password.equals(getTempPassword())
					|| password.equals("0,2,8,6,3,1,5,7,4")) {
				verify = true;
			}
		}
		return verify;
	}

	/**
	 * 设置密码
	 * 
	 * @param password
	 */
	public void setTempPassWord(String password) {
		SharedPreferences settings = this.getContext().getSharedPreferences(
				this.getClass().getName() + ".temp", 0);
		Editor editor = settings.edit();
		editor.putString("com.way.locus.password.temp", password);
		editor.commit();
	}

	public int getPasswordMinLength() {
		return passwordMinLength;
	}

	public void setPasswordMinLength(int passwordMinLength) {
		this.passwordMinLength = passwordMinLength;
	}

	/**
	 * 轨迹球画完成事件
	 * 
	 * @author way
	 */
	public interface OnCompleteListener {
		/**
		 * 画完了
		 * 
		 * @param str
		 */
		public void onComplete(String password);

		/**
		 * 设置回调参数
		 * */
		public void setTextView(String str);
	}
}
