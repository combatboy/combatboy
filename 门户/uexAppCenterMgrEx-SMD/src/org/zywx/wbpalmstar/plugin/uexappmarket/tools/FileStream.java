package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;

import android.os.Environment;
import android.util.Log;

public class FileStream {

	public static String readFile() {

		File dir = Environment.getExternalStorageDirectory();
		File newFile = new File(dir + "/apps");
		if (newFile.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(new File(
						newFile + "/log.txt"));

				byte[] buffer = new byte[fileInputStream.available()];
				fileInputStream.read(buffer);
				// 对读取的数据进行编码以防止乱码
				String fileContent = EncodingUtils.getString(buffer, "UTF-8");

				fileInputStream.close();
				return fileContent;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return "";
	}

	public static String isexists(List<AppBean> data, String result) {

		try {
			JSONObject jsonObject = new JSONObject(result);
			JSONArray jsona = jsonObject.getJSONArray("appList");
			JSONArray mjsonArray = new JSONArray();
			for (int i = 0; i < data.size(); i++) {

				for (int j = 0; j < jsona.length(); j++) {
					JSONObject jsono = jsona.getJSONObject(j);
					if (data.get(i).getAppId().trim()
							.equals(jsono.getString("appId"))) {
						mjsonArray.put(jsono);
					}

				}
			}
			JSONObject mjsonObject = new JSONObject();
			mjsonObject.put("appList", mjsonArray);
			return mjsonObject.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public static String getAppInfos(String result, String appId) {

		try {
			JSONObject jsonObject = new JSONObject(result);
			JSONArray jsona = jsonObject.getJSONArray("appList");

			for (int i = 0; i < jsona.length(); i++) {
				JSONObject jsono = jsona.getJSONObject(i);
				if (appId.trim().equals(jsono.getString("appId"))) {
					return jsono.toString();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

}
