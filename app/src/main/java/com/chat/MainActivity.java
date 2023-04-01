package com.chat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApi.showMsg(this, "开始载入本地配置");
        mApi.setFullscreen(this);
        mApi.sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        mApi.API_KEY = mApi.sharedPreferences.getString("key", "");
        mApi.RequestTimeout = Long.parseLong(mApi.sharedPreferences.getString("timeout", "5"));
        mApi.max_token = Integer.parseInt(mApi.sharedPreferences.getString("max_token", "1000"));
        mApi.temperature = Double.parseDouble(mApi.sharedPreferences.getString("temperature", "0.6"));
        mApi.model = mApi.sharedPreferences.getString("model", "gpt-3.5-turbo");
        mApi.stream = Boolean.parseBoolean((mApi.sharedPreferences.getString("stream", "true")));
        mApi.max_history = Integer.parseInt(mApi.sharedPreferences.getString("max_history", "50"));
        mApi.use_vps = mApi.sharedPreferences.getString("use_vps", "英国 S1");
        mApi.custom_url = mApi.sharedPreferences.getString("custom_url", "");
        mApi.use_vits = Boolean.parseBoolean((mApi.sharedPreferences.getString("use_vits", "false")));
        mApi.vits_speaker = mApi.sharedPreferences.getString("vits_model", "派蒙");
        new Handler().postDelayed(()->{
            startActivity(new Intent(this, Chat.class));
        }, 500);
    }

}