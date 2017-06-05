package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Arcs extends View {

	private Paint mArcPaint;
	private Paint mArcBGPaint;
	private Paint mTextPaint;

	private RectF mOval;
	private RectF mMaxOval;
	private Rect mTextRect;
	private float mSweep = 0;
	private int mThreshold = 100;
	private int mCurrentSpeedValue = 0;
	// private float mSpeedArcWidth;
	private boolean mUserCenter = true;

	// private float density = 1.0f;

	public Arcs(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();

	}

	public Arcs(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}

	public Arcs(Context context) {
		super(context);
		init();

	}

	private void init() {
		float density = getResources().getDisplayMetrics().density;
		// density = AppMarketMainActivity.density;
		mArcPaint = new Paint();
		mArcPaint.setStyle(Paint.Style.FILL);
		mArcPaint.setColor(0x55000000);
		mArcPaint.setAntiAlias(true);

		mArcBGPaint = new Paint();
		mArcBGPaint.setStyle(Paint.Style.FILL);
		mArcBGPaint.setColor(0x10000000);
		mArcBGPaint.setAntiAlias(true);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.WHITE);
		if (density <= 1.0) {
			mTextPaint.setTextSize(16.0f / 1.5f);
		} else if (density <= 1.5f) {
			mTextPaint.setTextSize(16.0f);
		} else {
			mTextPaint.setTextSize(16.0f * density / 1.5f);
		}

		mTextPaint.setAntiAlias(true);
		mTextRect = new Rect();
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		// mCenterX = w * 0.5f; // remember the center of the screen
		// mCenterY = h - mSpeedArcWidth;
		// mOval = new RectF(mCenterX - mCenterY, mSpeedArcWidth, mCenterX
		// + mCenterY, mCenterY * 2);

		mOval = new RectF(0, 0, w, h);
		mMaxOval = new RectF(0, 0, w + 10, h + 10);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// canvas.drawColor(Color.parseColor("#50000000"));
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(0x55000000);
		canvas.drawArc(mMaxOval, 0, 360, mUserCenter, p);

		drawSpeed(canvas);
	}

	private void drawSpeed(Canvas canvas) {
		// canvas.drawArc(mOval, 0, 360, mUserCenter, mArcBGPaint);

		canvas.drawArc(mOval, 0, 360, mUserCenter, mArcBGPaint);

		mSweep = (float) mCurrentSpeedValue / mThreshold * 360;
		if (mCurrentSpeedValue > mThreshold) {
			mArcPaint.setColor(0x10000000);
			mArcBGPaint.setColor(0x10000000);
		} else {
			mArcPaint.setColor(0x10000000);
		}

		// canvas.drawArc(mOval, 0, mSweep, mUserCenter, mArcPaint);
		canvas.drawArc(mOval, -90, mSweep, mUserCenter, mArcPaint);
		setText(canvas, mCurrentSpeedValue + "%");
		invalidate();
	}

	public int getCurrentSpeedValue() {
		return mCurrentSpeedValue;
	}

	public void setCurrentSpeedValue(int currentSpeedValue) {
		this.mCurrentSpeedValue = currentSpeedValue;
	}

	private void setText(Canvas canvas, String text) {
		float width = mOval.right - mOval.left;
		float height = mOval.bottom - mOval.top;
		mTextPaint.getTextBounds(text, 0, text.length() - 1, mTextRect);
		float textWidth = mTextPaint.measureText(text);
		int textHeight = mTextRect.height();
		canvas.drawText(mCurrentSpeedValue + "%",
				(float) (width - textWidth) / 2,
				(float) (height + textHeight) / 2, mTextPaint);
	}

	public void setProgress(int progress) {
		this.mCurrentSpeedValue = progress;
	}

}
