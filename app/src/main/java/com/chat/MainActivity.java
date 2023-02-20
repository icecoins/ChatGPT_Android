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
        mApi.API_KEY = mApi.sharedPreferences.getString("key", "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");
        mApi.RequestTimeout = Long.parseLong(mApi.sharedPreferences.getString("timeout", "3600"));
        mApi.max_token = Integer.parseInt(mApi.sharedPreferences.getString("max_token", "200"));
        mApi.temperature = Double.parseDouble(mApi.sharedPreferences.getString("temperature", "0.5"));
        mApi.model = mApi.sharedPreferences.getString("model", "text-davinci-003");
        mApi.stream = Boolean.parseBoolean((mApi.sharedPreferences.getString("stream", "true")));
        mApi.max_history = Integer.parseInt(mApi.sharedPreferences.getString("max_history", "20"));
        new Handler().postDelayed(()->{
            startActivity(new Intent(this, Chat.class));
        }, 500);
    }

}