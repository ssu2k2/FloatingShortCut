package com.yongsucho.floatingshortcut.Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.JetPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yongsucho.floatingshortcut.R;
import com.yongsucho.floatingshortcut.Service.FloatingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ManageActivity extends Activity implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    Button btnStart;
    Button btnStop;
    Button btnNew;
    Intent iService;

    ExpandableListView elvAppList;
    ExpandableAppAdapter elvAdpater;
    ArrayList<ArrayList<String>> collections;
    boolean isRunning = false;
    ArrayList<String> alApps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expan_manage);
        iService = new Intent(this, FloatingService.class);
        collections = new ArrayList<ArrayList<String>>();
        LoadAppList();
        setPackagesInfo();
        initLayout();
        initList();
    }
    private void initList() {
        alApps = new ArrayList<String>();
        elvAppList = (ExpandableListView)findViewById(R.id.elvApplist);

        if (collections.size() > 0) {
            btnNew.setVisibility(View.GONE);
            elvAppList.setVisibility(View.VISIBLE);
        } else {
            btnNew.setVisibility(View.VISIBLE);
            elvAppList.setVisibility(View.GONE);
        }
        elvAdpater = new ExpandableAppAdapter(this);
        //lvAdapter = new AppListAdapter(this, R.layout.app_cell, alApps);
        elvAppList.setAdapter(elvAdpater);

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
                AppSelectDialog(collections.size(), null);
                break;
            case R.id.btnStart:
                saveAppList();
                ManageActivity.this.startService(iService);
                saveServiceRestart(true);
                break;
            case R.id.btnEnd:
                ManageActivity.this.stopService(iService);
                saveServiceRestart(false);
                break;

        }
    }
    private final int CALL_APPLISTDIALOG = 0x01;
    String[] SelectedItems = null;
    int      SelectedCate  = 0;

    private void AppSelectDialog(int cate, String[] apps){
        Intent intent = new Intent(this, AppListDialog.class);
        intent.putExtra("CATE", cate);
        intent.putExtra("SELECT", apps);
        startActivityForResult(intent, CALL_APPLISTDIALOG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CALL_APPLISTDIALOG && resultCode == RESULT_OK) {
            SelectedCate = data.getIntExtra("CATE", 0);
            SelectedItems = data.getStringArrayExtra("SELECT");
            Log.d(TAG, "onActivityResult : " + SelectedItems.length);
            if(collections.size() > SelectedCate) {
                collections.get(SelectedCate).clear();
                collections.get(SelectedCate).addAll(Arrays.asList(SelectedItems));
            } else {
                collections.add(new ArrayList<String>(Arrays.asList(SelectedItems)));
            }

            if (collections.size() > 0) {
                btnNew.setVisibility(View.GONE);
                elvAppList.setVisibility(View.VISIBLE);
            } else {
                btnNew.setVisibility(View.VISIBLE);
                elvAppList.setVisibility(View.GONE);
            }

            elvAdpater.notifyDataSetChanged();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            AppSelectDialog(collections.size(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final String testJSON = "{\"CATE\": [ " +
            "{\"PACK\":[{\"NAME\":\"com.kakao.group\"},{\"NAME\":\"com.kakao.talk\"},{\"NAME\":\"com.kakao.story\"}]}," +
            "{\"PACK\":[{\"NAME\":\"net.daum.android.map\"},{\"NAME\":\"com.google.android.apps.maps\"},{\"NAME\":\"com.mnsoft.mappyobn\"}]}," +
            "{\"PACK\":[{\"NAME\":\"com.iloen.melon\"}]}," +
            "{\"PACK\":[{\"NAME\":\"com.plexapp.android\"}]}"+
            "]}";

    private void LoadAppList()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String JSONSelected = pref.getString("APPLIST", "{}");
        Log.d(TAG, "Load App List : " + JSONSelected);
        try{
            JSONObject jsonObject  = new JSONObject(JSONSelected);
            JSONArray jCateArray = jsonObject.getJSONArray("CATE");
            for (int i = 0; i < jCateArray.length(); i++) {
                JSONArray jPack = jCateArray.getJSONObject(i).getJSONArray("PACK");
                ArrayList<String> alPack = new ArrayList<String>();
                for (int j = 0; j < jPack.length(); j++) {
                    JSONObject jObject = jPack.getJSONObject(j);
                    String name = jObject.getString("NAME");
                    alPack.add(name);
                }
                collections.add(alPack);
                Log.d(TAG, "Load App Result CATE: " + i + " Size:" + alPack.size());
            }
        } catch(JSONException je) {

        }
    }

    private void saveAppList()
    {
        try {
            Log.d(TAG, "Save List");

            JSONObject jResultObj = new JSONObject();
            JSONArray jPackageArray = new JSONArray();
            for (int i = 0; i < collections.size(); i++) {
                JSONArray jNameArray = new JSONArray();
                for (int j = 0; j < collections.get(i).size(); j++) {
                    JSONObject jObj = new JSONObject();
                    Log.d(TAG, "Save Package : " + collections.get(i).get(j));
                    jObj.put("NAME", collections.get(i).get(j));
                    jNameArray.put(jObj);
                }
                JSONObject jPackObject = new JSONObject();
                jPackObject.put("PACK", jNameArray);
                jPackageArray.put(jPackObject);
            }
            jResultObj.put("CATE", jPackageArray);

            Log.d(TAG, "Save List : " + jResultObj.toString());
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("APPLIST", jResultObj.toString());
            editor.commit();
        }catch (JSONException je) {

        }
    }
    private void saveServiceRestart(boolean isStart)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("ISSTART", isStart);
        editor.commit();
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

    private String[] getChildData(int groupPosition) {
        ArrayList<String> apps = collections.get(groupPosition);
        return apps.toArray(new String[apps.size()]);
    }

    class ExpandableAppAdapter extends BaseExpandableListAdapter {

        public ExpandableAppAdapter(Context context) {
        }

        @Override
        public Object getGroup(int i) {
            return collections.get(i);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public int getGroupCount() {
            Log.d(TAG, "getGroupCount " + collections.size());
            return collections.size();
        }

        @Override
        public View getGroupView(final int groupPosition, boolean bIsExpanded, View convertView, ViewGroup viewGroup) {
            String GroupName = "App Group " + groupPosition;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cate_cell, null);
            }
            ((TextView)convertView.findViewById(R.id.tvTitle)).setText(GroupName);
            convertView.findViewById(R.id.ivChildAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ManageActivity.this, AppListDialog.class);
                    intent.putExtra("CATE", groupPosition);
                    intent.putExtra("SELECT", getChildData(groupPosition));
                    startActivityForResult(intent, CALL_APPLISTDIALOG);
                }
            });

            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return collections.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return collections.get(groupPosition).size();
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean bIsLastChild, View convertView, ViewGroup viewGroup) {
            String childPackage = collections.get(groupPosition).get(childPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.child_cell, null);
            }

            ApplicationInfo appInfo =  AppInfo.get(childPackage);
            TextView tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
            ImageView ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
            ImageView ivDelete = (ImageView)convertView.findViewById(R.id.ivDelete);

            ivIcon.setImageDrawable(appInfo.loadIcon(pm));
            tvTitle.setText(appInfo.loadLabel(pm));

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // REMOVE ITEM
                    collections.get(groupPosition).remove(childPosition);
                    if (collections.get(groupPosition).size() == 0) {
                        collections.remove(groupPosition);
                    }
                    if (collections.size() == 0){
                        btnNew.setVisibility(View.VISIBLE);
                        elvAppList.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}
