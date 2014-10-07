package com.yongsucho.floatingshortcut.Service;

import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yongsucho.floatingshortcut.Main.ManageActivity;
import com.yongsucho.floatingshortcut.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yongsucho on 2014. 10. 6..
 */
public class FloatingService extends Service implements View.OnClickListener{
    private final String TAG = "FloatingService";

    private WindowManager.LayoutParams layoutParams;
    private LayoutInflater inflater;

    private View mainLayout;
    private Button btnMain;

    private Instrumentation instrumentation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private static final int REBOOT_DELAY_TIMER = 10 * 1000;

    private void registerRestartAlarm() {
        Log.d(TAG, "registerRestartAlarm");
        Intent intent = new Intent(FloatingService.this, FloatingReceiver.class);
        intent.setAction(FloatingReceiver.ACTION_RESTART_MAIN_ACTIVITY);
        PendingIntent sender = PendingIntent.getBroadcast(FloatingService.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += REBOOT_DELAY_TIMER;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
    }

    private void unregisterRestartAlarm() {

        Log.d(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(FloatingService.this, FloatingReceiver.class);
        intent.setAction(FloatingReceiver.ACTION_RESTART_MAIN_ACTIVITY);
        PendingIntent sender = PendingIntent.getBroadcast(FloatingService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnAdd:
                Log.d(TAG, "onClick : btnAdd");
                Intent intent = new Intent();
                intent.setClass(this, ManageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }

    }

    private void getAppListFromJson(View main, String input) {
        pm = getPackageManager();

        LinearLayout llMain = (LinearLayout)main.findViewById(R.id.llAddButtons);

        try{
            JSONObject jsonObject  = new JSONObject(input);
            JSONArray jCate = jsonObject.getJSONArray("CATE");
            for (int i = 0; i < jCate.length(); i++) {
                JSONArray jPack = jCate.getJSONObject(i).getJSONArray("PACK");
                String[] sPackages = new String[jPack.length()];
                for (int j = 0; j < jPack.length(); j++) {
                    JSONObject jObject = jPack.getJSONObject(j);
                    String name = jObject.getString("NAME");
                    sPackages[j] = name;
                }
                ((LinearLayout)main.findViewById(R.id.llAddButtons)).addView(addCategory(sPackages));
            }
        } catch (JSONException e) {
            Log.d(TAG, "ERROR JSON PARSE");
        }

    }
    protected void launchApp(String packageName) {



        Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException err) {
                Toast t = Toast.makeText(getApplicationContext(), R.string.app_not_found, Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }


    PackageManager pm;
    List<ApplicationInfo> packages;

    private LinearLayout addCategory(String[] packageNames) {
        LinearLayout llCate  = (LinearLayout)inflater.inflate(R.layout.floating_add , null);

        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            for (String packageName : packageNames){
                if (packageInfo.packageName.contains(packageName)) {
                    addApps(llCate, packageInfo);
                }
            }
        }
        return llCate;
    }
    private LinearLayout addCategory(String packageName) {
        LinearLayout llCate  = (LinearLayout)inflater.inflate(R.layout.floating_add , null);

        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.contains(packageName)) {
                addApps(llCate, packageInfo);
            }
        }
        return llCate;
    }

    private LinearLayout addApps(LinearLayout main , final ApplicationInfo appInfo) {
        Log.d(TAG, "addApps package :" + appInfo.packageName);
        ImageButton btn = new ImageButton(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchApp(appInfo.packageName);
            }
        });
        btn.setBackground(appInfo.loadIcon(pm));
        btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        main.addView(btn);
        return main;
    }
    private String getPackageList(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        return pref.getString("APPLIST", "{}");
    }
    ImageView image;
    WindowManager windowManager;
    int viewFlag = MotionEvent.ACTION_UP;
    @Override
    public void onCreate() {
        super.onCreate();

        unregisterRestartAlarm();

        image = new ImageView(this);
        image.setImageResource(R.drawable.ic_launcher);

        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mainLayout = inflater.inflate(R.layout.floating_main , null);
        btnMain = (Button)mainLayout.findViewById(R.id.btnMain);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        Button btnAdd = (Button)mainLayout.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        final WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x=0;
        paramsF.y=100;

        windowManager.addView(mainLayout, paramsF);

        String appList = getPackageList();
        getAppListFromJson(mainLayout, appList);

        try{

            btnMain.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams paramsT = paramsF;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:

                            viewFlag = MotionEvent.ACTION_DOWN;

                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (viewFlag != MotionEvent.ACTION_MOVE) {
                                if(mainLayout.findViewById(R.id.llExtends).getVisibility() == View.GONE) {
                                    mainLayout.findViewById(R.id.llExtends).setVisibility(View.VISIBLE);
                                } else {
                                    mainLayout.findViewById(R.id.llExtends).setVisibility(View.GONE);
                                }
                            }
                            viewFlag = MotionEvent.ACTION_UP;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            float diffx = Math.abs(event.getRawX() - initialTouchX);
                            float diffy = Math.abs(event.getRawY() - initialTouchY);

                            if (diffx > 30 || diffy > 30) {
                                viewFlag = MotionEvent.ACTION_MOVE;

                                windowManager.updateViewLayout(mainLayout, paramsF);
                            } else {
                                windowManager.updateViewLayout(mainLayout, paramsF);
                            }

                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    boolean isStart = false;
    private boolean isRestart(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        return pref.getBoolean("ISSTART", false);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mainLayout != null) {
            if (windowManager != null )
                windowManager.removeView(mainLayout);
            mainLayout = null;
        }
        if (isRestart()){
            registerRestartAlarm();
        }
        super.onDestroy();
    }
}
