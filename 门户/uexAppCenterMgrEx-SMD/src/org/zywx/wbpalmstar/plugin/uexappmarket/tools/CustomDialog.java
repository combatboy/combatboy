package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

public class CustomDialog extends Dialog {

	public CustomDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);

	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public CustomDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
