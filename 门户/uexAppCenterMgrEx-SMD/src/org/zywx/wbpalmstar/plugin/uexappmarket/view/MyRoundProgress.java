package org.zywx.wbpalmstar.plugin.uexappmarket.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class MyRoundProgress extends View {

	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor = Color.parseColor("#10000000");

	/**
	 * 圆环的宽度
	 */

	/**
	 * 当前进度
	 */
	private int progress;

	private int max = 100;

	private float roundWidth;

	public MyRoundProgress(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyRoundProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MyRoundProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// this.setRotation((float)90);
		paint = new Paint();
		roundColor = Color.parseColor("#10000000");

		// roundWidth = getWidth()*3/2;
		// float density=getResources().getDisplayMetrics().density;
		// 标题 90dp item 50dp
		// int height = DisplayUtil.dip2px(90+50*4, density);
		roundWidth = getWidth() *5 / 14;
		//Toast.makeText(this.getContext(),getWidth()+"roundWidth==="+roundWidth,Toast.LENGTH_LONG).show();;
		/**
		 * 画最外层的大圆环
		 */
		// int centre = getWidth()/2; //获取圆心的x坐标
		// int radius = (int) (centre - roundWidth/2); //圆环的半径
		// paint.setColor(roundColor); //设置圆环的颜色
		// paint.setStyle(Paint.Style.FILL); //设置空心
		// paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		// paint.setAntiAlias(true); //消除锯齿
		// canvas.drawCircle(centre, centre, radius, paint); //画出圆环
		//
		// paint.setStrokeWidth(0);
		//
		// /**
		// * 画圆弧 ，画圆环的进度
		// */
		//
		// //设置进度是实心还是空心
		// paint.setStrokeWidth(10); //设置圆环的宽度
		// paint.setColor(roundProgressColor); //设置进度的颜色
		// RectF oval = new RectF(centre - radius, centre - radius, centre
		// + radius, centre + radius); //用于定义的圆弧的形状和大小的界限

		// paint.setStyle(Paint.Style.STROKE);
		// canvas.drawArc(oval, 0, 360 * progress / max, false, paint);
		// //根据进度画圆弧

		// canvas.drawColor(Color.parseColor("#50000000"));
		int centre = getWidth() / 2; // 获取圆心的x坐标
		int yZ=getHeight()*2 / 5;
		int radius = yZ*4/5; // 圆环的半径
		// paint.setStrokeWidth(10);
		paint.setAlpha(0);
		paint.setAntiAlias(true);
		// paint.setColor(Color.parseColor("#10000000"));

		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Config.ARGB_8888);
		Canvas tempcCanvas = new Canvas(bitmap);
		tempcCanvas.drawColor(Color.parseColor("#50000000"));

		tempcCanvas.drawCircle(centre, yZ, radius, paint);

		RectF rf1 = new RectF(centre - radius, yZ - radius,
				centre + radius, yZ + radius);
		// canvas.drawArc(rf1, 0, 360, true, paint);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Paint p2 = new Paint();
		// p2.setAntiAlias(true);
		// p2.setColor(Color.parseColor("#50000000"));
		// RectF rf2 = new RectF(centre-radius+10, centre-radius+10,
		// centre+radius-10, centre+radius-10);
		// canvas.drawArc(rf2, 0, (360 * progress / max), true, p2);

		Paint p3 = new Paint();
		p3.setAntiAlias(true);
		p3.setColor(Color.parseColor("#50000000"));
		// canvas.drawArc(rf3, 0, 360 * progress / max, true, p3);

		// p3.setStyle(Style.FILL_AND_STROKE);
		// p3.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Bitmap bitmap2=Bitmap.createBitmap(getWidth()-20, getHeight()-20,
		// Config.ARGB_8888);

		// Canvas tempcCanvas2=new Canvas(bitmap);
		// tempcCanvas2.drawColor(Color.parseColor("#10000000"));
		// tempcCanvas2.drawCircle(centre, centre, radius, p3);
		// 圆环的宽度
		int w = (int) roundWidth / 5;
		RectF rf3 = new RectF(centre - radius + w, yZ - radius + w, centre
				+ radius - w, yZ + radius - w);
		canvas.drawArc(rf3, -450, (360 * progress / max) - 360, true, p3);
		// canvas.drawBitmap(bitmap2, 0, 0, null);

	}

	/**
	 * 获取进度.需要同步
	 * 
	 * @return
	 */
	public synchronized int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * 
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (progress > max) {
			progress = max;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}

	}

}
