package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

@SuppressLint("NewApi")
public class MyAsyncTask extends AsyncTask<Object, Integer, Object> {
	private static final int MSG_ACTION_PRE = 1;
	private static final int MSG_ACTION_COMPLETED = 2;
	private static final int MSG_ACTION_UPDATE_PROGRESS = 3;
	private static final int MSG_ACTION_CANCEL = 4;
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ACTION_PRE:
				handleOnPreLoad(MyAsyncTask.this);
				break;
			case MSG_ACTION_UPDATE_PROGRESS:
				handleOnUpdateProgress(MyAsyncTask.this, msg.arg1);
				break;
			case MSG_ACTION_COMPLETED:
				handleOnCompleted(MyAsyncTask.this, msg.obj);
				break;
			case MSG_ACTION_CANCEL:
				handleOnCanceled(MyAsyncTask.this);
				break;
			}
		};
	};

	@Override
	protected void onPreExecute() {
		handler.sendEmptyMessage(MSG_ACTION_PRE);
	}

	@Override
	protected Object doInBackground(Object... params) {

		return null;
	}

	public void handleOnPreLoad(MyAsyncTask task) {

	};

	public void handleOnUpdateProgress(MyAsyncTask task, int percent) {

	}

	public void handleOnCanceled(MyAsyncTask task) {

	};

	public void handleOnCompleted(MyAsyncTask task, Object result) {

	};

	@Override
	protected void onProgressUpdate(Integer... values) {
		Message msg = handler.obtainMessage(MSG_ACTION_UPDATE_PROGRESS);
		msg.arg1 = values[0];
		handler.sendMessage(msg);
	}

	@Override
	protected void onPostExecute(Object result) {
		Message msg = handler.obtainMessage(MSG_ACTION_COMPLETED);
		msg.obj = result;
		handler.sendMessage(msg);
	}

	@Override
	protected void onCancelled() {
		handler.sendEmptyMessage(MSG_ACTION_CANCEL);
	}
}
