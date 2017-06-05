package com.way.locus;

import java.io.Serializable;

public interface Contains extends Serializable {
	int ERROR_CLEAR_TIME = 300;

	public void getResult(String result);

	public void getError(String error);

	public void getForget();

	public void getother();

	public String LOCK = "LOCK";
	public String INIT = "INIT";

	public static final String FILECONTNAME = "org.zywx.wbpalmstar.plugin.uexlockpattern.count";
	public static final String KEYECONTNAME = "org.zywx.wbpalmstar.plugin.init.count";

	public static final String FILLOCKNAME = "com.way.locus.LocusPassWordView";
	public static final String KEYELOCKNAME = "com.way.locus.password";

	public static final String ERRORNAME = "com.way.locus.loginErrorCount";

	public static final String SETORGETNAME = "com.way.locus.setPasswordData";

	public static final String NUMBERNAME = "com.way.locus.preferences";

}
