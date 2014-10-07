package com.yongsucho.floatingshortcut.Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yongsucho.floatingshortcut.R;
import com.yongsucho.floatingshortcut.Service.FloatingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ManageActivity extends Activity implements View.OnClickListener{

    Button btnStart;
    Button btnStop;
    Button btnNew;
    Intent iService;

    ListView lvAppList;
    AppListAdapter lvAdapter;
    boolean isRunning = false;
    ArrayList<String> alApps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        iService = new Intent(this, FloatingService.class);
        setPackagesInfo();
        initLayout();
        initList();
    }
    private void initList() {
        alApps = new ArrayList<String>();
        lvAppList = (ListView)findViewById(R.id.lvApplist);

        if (alApps.size() > 0) {
            btnNew.setVisibility(View.GONE);
            lvAppList.setVisibility(View.VISIBLE);
        } else {
            btnNew.setVisibility(View.VISIBLE);
            lvAppList.setVisibility(View.GONE);
        }
        lvAdapter = new AppListAdapter(this, R.layout.app_cell, alApps);
        lvAppList.setAdapter(lvAdapter);

    }

    private void initLayout() {
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnEnd);
        btnNew = (Button)findViewById(R.id.btnNew);

        btnNew.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNew:
                break;
            case R.id.btnStart:
                ManageActivity.this.startService(iService);
                break;
            case R.id.btnEnd:
                ManageActivity.this.stopService(iService);
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    HashMap<String, ApplicationInfo> AppInfo;
    PackageManager pm;

    private void setPackagesInfo() {
        pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        AppInfo = new HashMap<String, ApplicationInfo>();

        for (ApplicationInfo packageInfo : packages) {
            AppInfo.put(packageInfo.packageName, packageInfo);
        }
    }

    class AppInfoHolder {
        ImageView ivIcon;
        TextView tvTitle;
    }

    class AppListAdapter extends ArrayAdapter<String> {
        LayoutInflater inflater;
        int resId;
        public AppListAdapter(Context context , int ResID, List<String> items) {
            super(context, ResID, items);
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            resId = ResID;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppInfoHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(resId, null);
                holder = new AppInfoHolder();
                holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
                holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
            } else {
                holder = (AppInfoHolder)convertView.getTag();
            }

            String app = (String)getItem(position);

            ApplicationInfo appInfo =  AppInfo.get(app);

            holder.ivIcon.setImageDrawable(appInfo.loadIcon(pm));
            holder.tvTitle.setText(appInfo.name);

            return convertView;
        }

    }
}
