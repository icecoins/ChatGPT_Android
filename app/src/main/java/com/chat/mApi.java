package com.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Objects;

public class mApi {
    public static List<ChatItem> chatItems;
    public static SharedPreferences sharedPreferences;
    private static Toast t1;
    public static long RequestTimeout = 3600;
    public static int max_token = 1000;
    public static int max_history = 30;
    public static double temperature = 0.5;
    public static String
            model = "text-davinci-003",
            use_vps = "None",
            custom_url = "",
            vits_speaker = "派蒙";
    public static boolean
            stream = true,
            use_vits = false;
    public static String API_KEY = "";
    public static void showMsg(Context ct, String s){
        new Thread(()->{
            try{
                if(Looper.myLooper() == null){
                    Looper.prepare();
                    if (t1 != null) {
                        t1.cancel();
                        t1 = null;
                    }
                    t1= Toast.makeText(ct,s, Toast.LENGTH_SHORT);
                    t1.show();
                    Looper.loop();
                }else{
                    if (t1 != null) {
                        t1.cancel();
                        t1 = null;
                    }
                    t1= Toast.makeText(ct,s, Toast.LENGTH_SHORT);
                    t1.show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    public static void setFullscreen(AppCompatActivity activity) {
        Objects.requireNonNull(activity.getSupportActionBar()).hide();
        if (activity.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE && Build.VERSION.SDK_INT >= 28){
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Window _window = activity.getWindow();
        WindowManager.LayoutParams params = _window.getAttributes();
        params.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        _window.setAttributes(params);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        ActivityController.getInstance().addActivity(activity);
    }
}
