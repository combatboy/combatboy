package org.zywx.wbpalmstar.plugin.uexlockpattern;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

public class LoclPatterCustomDialog extends Dialog {

	public LoclPatterCustomDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public LoclPatterCustomDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	public LoclPatterCustomDialog(Context context) {
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
