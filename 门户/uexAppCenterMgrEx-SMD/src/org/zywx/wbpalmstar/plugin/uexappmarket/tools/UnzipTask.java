package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class UnzipTask extends AsyncTask<Void, Integer, Object> {

	private Context mContext;
	// private OutputStream mOs;
	ProgressDialog dialog;

	public UnzipTask(OutputStream os, Context context) {
		mContext = context;
		// mOs=os;
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(mContext, "", "aaaaa");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.show();
	}

	@Override
	protected Object doInBackground(Void... params) {
		int i = 0;
		while (i < 100) {
			i++;
			publishProgress(i);
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] <= 100) {
			dialog.setProgress(values[0]);
		} else {
			if (dialog != null) {
				dialog.dismiss();
			}
			if (dialog == null) {
				dialog = ProgressDialog.show(mContext, "", "bbbbbbbbbb");
				dialog.show();
			}

		}

	}

	@Override
	protected void onPostExecute(Object result) {
		Toast.makeText(mContext, "ssssssss", Toast.LENGTH_SHORT).show();
	}

}
