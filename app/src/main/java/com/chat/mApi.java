package com.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.EOFException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class mApi {
    public static SharedPreferences sharedPreferences;
    private static Toast t1;

    public static long RequestTimeout = 3600;
    public static int max_token = 1000;
    public static int max_history = 30;
    public static double temperature = 0.5;
    public static String model = "text-davinci-003";
    public static boolean stream = true;
    public static String API_KEY = "";
    public static void showMsg(Context ct, String s){
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
    }
    static boolean isNum(String s){
        String[] a = {"0","1","2","3","4","5","6","7","8","9"};
        for(String c : a){
            if(s.equals(c)){
                return true;
            }
        }
        return false;
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
