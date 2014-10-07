package com.yongsucho.floatingshortcut.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by yongsucho on 2014. 10. 7..
 */
public class FloatingReceiver extends BroadcastReceiver {
    private final String TAG = "FloatingReceiver";
    public static final String ACTION_RESTART_MAIN_ACTIVITY = "ACTION.Restart.PersistentService";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 서비스 죽일때 알람으로 다시 서비스 등록
        if(intent.getAction().equals(ACTION_RESTART_MAIN_ACTIVITY)) {
            Log.d(TAG, "ACTION_RESTART_MAIN_ACTIVITY");
            Intent i = new Intent(context, FloatingService.class);
            context.startService(i);
        }
        // 폰 재부팅할때 서비스 등록
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "ACTION_BOOT_COMPLETED");
            Intent startServiceIntent = new Intent(context, FloatingService.class);
            context.startService(startServiceIntent);
        }
    }
}
