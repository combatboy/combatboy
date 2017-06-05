package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.util.Comparator;

import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;

public class ComparatorApp implements Comparator<AppBean> {

	public int compare(AppBean arg0, AppBean arg1) {

		AppBean app1 = (AppBean) arg0;
		AppBean app2 = (AppBean) arg1;

		// app1.getUpdateTime().compareTo(app2.getUpdateTime());
		return (int) (app1.getUpdateTime() - app2.getUpdateTime());
	}
}
