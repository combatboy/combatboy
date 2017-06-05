package org.zywx.wbpalmstar.plugin.uexappmarket.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.adapter.AddAppsAdapter;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.AppBean;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.DBsql;
import org.zywx.wbpalmstar.plugin.uexappmarket.bean.SendPost;
import org.zywx.wbpalmstar.plugin.uexappmarket.down.AppTaskList;
import org.zywx.wbpalmstar.plugin.uexappmarket.eue.EUExAppMarketEx;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnDateBack;
import org.zywx.wbpalmstar.plugin.uexappmarket.inter.OnWidgetClickedCallback;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.CommonUtility;
import org.zywx.wbpalmstar.plugin.uexappmarket.tools.Tools;

import java.util.ArrayList;
import java.util.List;

public class AddAppActivity extends Activity implements OnClickListener,
        OnDateBack {
    private ExpandableListView mListView;
    private AddAppsAdapter mAddAppAdapter;
    private TextView mTitleText;
    private LinearLayout mBackView;
    public static final String TAG = "AddAppActivity";
    List<String> groudList;
    List<List<AppBean>> childList;

    private LayoutInflater mInflater;
    public static View mView;
    private DBsql dBsql;
    private OnWidgetClickedCallback callback;

    private AppTaskList appsTaskList = new AppTaskList();

    SharedPreferences shared;

    EUExAppMarketEx mEUExAppMarketEx;
    Context exContext;
    SharedPreferences sh;

    SendPost post;

    public static AddAppActivity getIntance() {

        return new AddAppActivity();
    }

    public void setEUExAppMarketEx(EUExAppMarketEx mEUExAppMarketEx) {
        this.mEUExAppMarketEx = mEUExAppMarketEx;
    }

    public void setExContext(Context context) {
        exContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EUExUtil.init(this);
        post = new SendPost(this, AddAppActivity.this);
        String softToken = Tools.getSoftToken(this);
        String mRequestUrl = CommonUtility.URL_APP_LIST + softToken;
        post.performAsyncLoadAppListAction(mRequestUrl);
        init();
    }

    /**
     * 获取SoftToken
     *
     * @return
     */
    private String getSoftToken() {

        SharedPreferences preferences = getSharedPreferences("app",
                Context.MODE_WORLD_READABLE);
        return preferences.getString("softToken", null);
    }

    private void init() {
        setupView();
        sh = AddAppActivity.this.getSharedPreferences("shared",
                Context.MODE_PRIVATE);
        groudList = new ArrayList<String>();
        childList = new ArrayList<List<AppBean>>();
        groudList.add("未下载应用");
        groudList.add("已下载应用");
        dBsql = new DBsql(this, appsTaskList, callback);
        setData();
    }

    public void setEmpty() {
        TextView tv = (TextView) findViewById(EUExUtil
                .getResIdID("plugin_appmarket_ex_listview_empty_text"));
        mListView.setEmptyView(tv);
    }

    private void setupView() {

        mInflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(
                EUExUtil.getResLayoutID("plugin_appmarket_ex_add_app_layout"),
                null);

        setContentView(mView);
        mListView = (ExpandableListView) findViewById(EUExUtil
                .getResIdID("plugin_appmarket_ex_add_app_listView1"));

        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setDivider(null);

        mListView.setGroupIndicator(null); // 设置GROUP前的图标为空

        mTitleText = (TextView) findViewById(EUExUtil
                .getResIdID("plugin_appmarket_ex_title_textView1"));
//		mTitleText.setText("更多");

        mBackView = (LinearLayout) findViewById(EUExUtil
                .getResIdID("plugin_appmarket_ex_add_app_back_layout"));

        mBackView.setOnClickListener(AddAppActivity.this);

    }

    public void setVisible(String target) {

        if ("1".equals(target)) {
            mView.setVisibility(View.VISIBLE);
        } else if ("0".equals(target)) {
            mView.setVisibility(View.GONE);
        }
    }

    ;

    private void setData() {
        mAddAppAdapter = new AddAppsAdapter(this, groudList, mListView);
        mListView.setAdapter(mAddAppAdapter);
        for (int i = 0; i < groudList.size(); i++) {
            mListView.expandGroup(i); // 设置所有的Group为打开的
        }

    }

    // public void onRefresh() {
    // setData();
    // }

    @Override
    public void onClick(View v) {
        onClickBreak();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onClickBreak();
        }
        return true;
    }

    public void onClickBreak() {
        int size = appsTaskList.size();
        for (int i = 0; i < size; i++) {
            String sid = appsTaskList.getTask(i).getAppBean().getId();
            appsTaskList.isExistTask(sid, AddAppActivity.this);
            Toast.makeText(
                    AddAppActivity.this,
                    "\"" + appsTaskList.getTask(i).getAppBean().getAppName()
                            + "\"暂停下载!", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent();
        intent.setAction(AppMarketMainActivity.ACTION);
        sendBroadcast(intent);
        finish();
    }

    public AddAppsAdapter getAddAppsAdapter() {
        return mAddAppAdapter;
    }

    public DBsql getDBsql() {
        return dBsql;
    }

    public AppTaskList getAppsTaskList() {
        return appsTaskList;
    }

    public void setCallback(OnWidgetClickedCallback callback) {
        // TODO Auto-generated method stub
        this.callback = callback;
    }

    @Override
    public void getDate() {
        // TODO Auto-generated method stub
        setData();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        post.closeDialog();
    }
}
