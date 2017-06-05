package org.zywx.wbpalmstar.plugin.uexappmarket.down;

import java.util.LinkedList;

import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AppTaskList {

	private LinkedList<AppDownTask> taskList = new LinkedList<AppDownTask>();

	private Toast mToast = null;

	public boolean isExistTask(String taskId, Context context) {
		if (mToast == null) {
			// mToast = Toast.makeText(context, "\""+name+"\"暂停下载!",
			// Toast.LENGTH_SHORT);
		}
		synchronized (this) {
			for (int i = 0, size = taskList.size(); i < size; i++) {
				if (taskList.get(i).getTaskId().equals(taskId)) {
					AppBean appBean = taskList.get(i).appBean;
					boolean isDownloaded = taskList.get(i).appBean.getState() == AppBean.STATE_DOWNLOADED;

					if (AppBean.TYPE_NATIVE == appBean.getType()) {

					} else if (AppBean.TYPE_WIDGET == appBean.getType()) {
						// if(!taskList.get(i).getWait()&&!isDownloaded){

						AppDownTask task = taskList.get(i);

						task.setWait(!task.getWait());
						task.cancel(true);
						Log.e(EUExAppMarketEx.TAG, appBean.getAppName()
								+ "    暂停下载");
						if (!isDownloaded && task.isCancelled()) {
							// mToast.show();
						}
						// }
					}

					return true;
				}
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return taskList.size() == 0;
	}

	public void addTask(AppDownTask downTask) {
		System.out.println("AppDownTask == " + downTask.hashCode());
		if (taskList.size() > 0) {
			downTask.setWait(true);
		}
		taskList.add(downTask);
	}

	public void removeTask(AppDownTask task) {
		taskList.remove(task);
		if (taskList.size() > 0) {
			taskList.getFirst().setWait(false);
		}
	}

	public AppBean getTaskInfoByAppId(String appId) {
		for (AppDownTask task : taskList) {
			if (task.getTaskId().equals(appId)) {
				return task.getAppBean();
			}
		}
		return null;
	}

	public AppDownTask getTask(int index) {
		if (index < 0) {
			return null;
		}
		return taskList.get(index);
	}

	public int size() {
		return taskList.size();
	}

	public int indexOf(AppDownTask task) {
		return taskList.indexOf(task);
	}

	public AppDownTask getFirst() {

		return taskList.getFirst();
	}

}
