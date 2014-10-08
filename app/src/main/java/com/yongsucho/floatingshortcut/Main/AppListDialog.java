package com.yongsucho.floatingshortcut.Main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yongsucho.floatingshortcut.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yongsucho on 2014. 10. 8..
 */
public class AppListDialog extends Activity implements View.OnClickListener {
    private final String TAG  = getClass().getSimpleName();
    ListView lvList;
    Button btnCancel;
    Button btnOk;
    String[] selectItem;
    int selectCate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        Intent intent = getIntent();
        selectItem = intent.getStringArrayExtra("SELECT");
        selectCate = intent.getIntExtra("CATE", 0);
        initLayout();
        initList();
    }

    @Override
    public void onClick(View view) {
        Intent intent = getIntent();
        switch (view.getId()) {
            case R.id.btnCancel:
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
            case R.id.btnOk:
                intent.putExtra("CATE", selectCate);
                intent.putExtra("SELECT", getSelectPackage());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void initLayout() {
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnOk = (Button)findViewById(R.id.btnOk);
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }
    private void initList() {
        setPackagesInfo();;
        lvList = (ListView)findViewById(R.id.lvlist);
        appAdpater = new AppAdpater(this, R.layout.app_select_cell, AppInfo);
        lvList.setAdapter(appAdpater);
    }

    AppAdpater appAdpater;
    PackageManager pm;
    ArrayList<AppSelectInfo> AppInfo;

    /**
     * 선택된 리스트의 PackageName 을 골라냄
     * @return
     */
    private String[] getSelectPackage() {
        ArrayList<String> alSelected = new ArrayList<String>();
        for (AppSelectInfo apps : AppInfo) {
            if (apps.isChecked) {
                alSelected.add(apps.packageName);
            }
        }
        return alSelected.toArray(new String[alSelected.size()]);
    }
    private void setPackagesInfo() {
        pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        AppInfo = new ArrayList<AppSelectInfo>();
        for (ApplicationInfo packageInfo : packages) {
            AppSelectInfo info = new AppSelectInfo();
            info.packageName = packageInfo.packageName;
            info.name = packageInfo.loadLabel(pm).toString();
            info.bmIcon = packageInfo.loadIcon(pm);
            info.isChecked = false;
            if (selectItem != null){
                for (String sel : selectItem){
                    if (sel.equals(info.packageName)){
                        info.isChecked = true;
                    }
                }
            }
            AppInfo.add(info);
        }
        Collections.sort(AppInfo, new NameAscCompare());
    }

    static class NameAscCompare implements java.util.Comparator<AppSelectInfo> {
        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(AppSelectInfo arg0, AppSelectInfo arg1) {
            // TODO Auto-generated method stub
            return arg0.name.compareTo(arg1.name);
        }

    }


    class Holder {
        ImageView ivIcon;
        TextView tvTitle;
        CheckBox ckSelect;
    }
    class AppSelectInfo {
        String packageName;
        String name;
        Drawable bmIcon;
        boolean isChecked;
    }

    class AppAdpater extends ArrayAdapter<AppSelectInfo> {
        int resId;
        LayoutInflater inflater;
        public AppAdpater(Context context , int resId, ArrayList<AppSelectInfo> list) {
            super(context, resId, list);
            this.resId = resId;
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder;

            holder = new Holder();
            convertView = inflater.inflate(resId, null);
            holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
            holder.tvTitle = (TextView)convertView.findViewById(R.id.tvTitle);
            holder.ckSelect = (CheckBox)convertView.findViewById(R.id.ckApp);
            convertView.setTag(holder);
            AppSelectInfo app = (AppSelectInfo)getItem(position);

            holder.tvTitle.setText(app.name);
            holder.ivIcon.setImageDrawable(app.bmIcon);
            holder.ckSelect.setChecked(app.isChecked);

            holder.ckSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Log.d(TAG, "onCheckedChanged : " + (b?"true":"false"));
                    AppSelectInfo appSelect = (AppSelectInfo)getItem(position);
                    appSelect.isChecked = b;
                }
            });
            return convertView;
        }
    }
}
