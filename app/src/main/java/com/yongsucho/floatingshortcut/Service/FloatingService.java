package com.yongsucho.floatingshortcut.Service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.yongsucho.floatingshortcut.R;

/**
 * Created by yongsucho on 2014. 10. 6..
 */
public class FloatingService extends Service {
    private final String TAG = "FloatingService";

    private WindowManager.LayoutParams layoutParams;
    private LayoutInflater inflater;

    private View mainLayout;
    private Button btnMain;

    private Instrumentation instrumentation;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
    ImageView image;
    WindowManager windowManager;
    int viewFlag = MotionEvent.ACTION_UP;
    @Override
    public void onCreate() {
        super.onCreate();
        image = new ImageView(this);

        image.setImageResource(R.drawable.ic_launcher);

        inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mainLayout = inflater.inflate(R.layout.floating_main , null);
        btnMain = (Button)mainLayout.findViewById(R.id.btnMain);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

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
                            Log.d(TAG, "Touch Up! ");
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
                            viewFlag = MotionEvent.ACTION_MOVE;

                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(mainLayout, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainLayout != null) {
            if (windowManager != null )
                windowManager.removeView(mainLayout);
            mainLayout = null;
        }
    }
}
