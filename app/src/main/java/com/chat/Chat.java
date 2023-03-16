package com.chat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class Chat extends AppCompatActivity {
    static UUID uuid;
    boolean isBotTalking = false,
            isConnecting = false;
    WebSocketClient webSocketClient;
    List<ChatItem> chatItems;
    ArrayList<String> history;
    ChatItem current_bot_chat;
    ChatListAdapter chatListAdapter;
    ListView result;
    EditText input;
    ImageView help,
            start,
            config,
            del_history,
            connect;
    Handler handler;
    long mBackPressed;
    int BOT_BEGIN = 0,
            BOT_CONTINUE = 1,
            USER_MSG = 2,
            BOT_END = 3,
            CLEAR_HISTORY = 4;
    String serverURL = "",
            bot_record = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mApi.setFullscreen(this);
        chatItems = new ArrayList<>();
        history = new ArrayList<>();
        result = findViewById(R.id.result);
        chatListAdapter = new ChatListAdapter(chatItems,this);
        result.setAdapter(chatListAdapter);
        input = findViewById(R.id.input);
        help = findViewById(R.id.help);
        start = findViewById(R.id.start);
        config = findViewById(R.id.config);
        connect = findViewById(R.id.connect);
        del_history = findViewById(R.id.del_history);
        del_history.setOnClickListener(v->{
            AlertDialog.Builder b=new AlertDialog.Builder(this);
            b.setTitle("是否清除AI之前对话的记忆？");
            b.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            b.setPositiveButton("清除", (dialog, which) -> {
                sendHandlerMsg(CLEAR_HISTORY,"");
            });
            b.show();
        });
        help.setOnClickListener(v->{
            showHelp();
        });
        config.setOnClickListener(v->{
            showConfig();
        });
        start.setOnClickListener(v->{
            if(!mApi.use_vps.equals("不使用中转")){
                if(null == webSocketClient || webSocketClient.isClosed()){
                    mApi.showMsg(this, "未连接至服务器，请先连接");
                }else{
                    chatGPT_vps();
                }
            }else {
                chatGPT_direct();
            }
        });
        connect.setOnClickListener(v->{
            if(mApi.use_vps.equals("不使用中转")){
                mApi.showMsg(this, "未选择中转服务器，无需连接");
            }else{
                if(null == webSocketClient){
                    mApi.showMsg(this, "尝试连接至服务器...");
                    connectToVps();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("已连接至服务器，确定强制重新连接吗？");
                    builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
                    builder.setPositiveButton("确定", (dialog, which) -> {
                        mApi.showMsg(this, "重新连接服务器...");
                        webSocketClient = null;
                        isConnecting = false;
                        isBotTalking = false;
                        connectToVps();
                    });
                    builder.show();
                }
            }
        });
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                        // Bot begin printing
                        Log.e("BOT", "BEGIN");
                        isBotTalking = true;
                        chatItems.add(current_bot_chat);
                        refreshListview();
                        break;
                    case 1:
                        // Bot continue printing
                        current_bot_chat.appendText(msg.obj.toString());
                        refreshListview();
                        break;
                    case 2:
                        // User' msg
                        closeInputMethod();
                        input.setText("");
                        if(history.size() >= mApi.max_history){
                            history.remove(0);
                            history.remove(0);
                        }
                        history.add("Q: " + msg.obj.toString() + "<|endoftext|>\n\n");
                        ChatItem chatItem = new ChatItem();
                        chatItem.setType(1);
                        chatItem.setText(msg.obj.toString());
                        chatItems.add(chatItem);
                        refreshListview();
                        break;
                    case 3:
                        // Bot end printing
                        Log.e("BOT", "END");
                        isBotTalking = false;
                        if(!(null == msg.obj)){
                            history.add("A: " + msg.obj + "<|endoftext|>\n\n");
                        }
                        current_bot_chat = new ChatItem();
                        current_bot_chat.setType(0);
                        current_bot_chat.setText("\t\t");
                        refreshListview();
                        break;
                    case 4:
                        // Delete History
                        if(!history.isEmpty()){
                            history.clear();
                        }
                        mApi.showMsg(Chat.this, "记忆已清除");
                        break;
                    default:
                        break;
                }
            }
        };
        handler.sendEmptyMessage(BOT_END);
        showConfig();
    }
    void closeInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            View v = getWindow().peekDecorView();
            if (null != v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
    void connectToVps(){
        if((null != webSocketClient) && isConnecting){
            return;
        }
        runOnUiThread(()->{
            new Thread(()->{
                uuid = UUID.randomUUID();
                switch (mApi.use_vps) {
                    case "美国 S2":
                        serverURL = "wss://app.i64.cc/webSocket/";
                        break;
                    case "自定义服务器":
                        serverURL = mApi.custom_url;
                        break;
                    case "美国 S1":
                        serverURL = "wss://api.i64.cc/webSocket/";
                        break;
                    default:
                        serverURL = "wss://api.hook.i64.cc/webSocket/";
                        break;
                }
                try {
                    webSocketClient = new WebSocketClientEx(new URI(serverURL + uuid));
                    webSocketClient.setConnectionLostTimeout((int) mApi.RequestTimeout);
                    if(webSocketClient.connectBlocking()){
                        runOnUiThread(()->{
                            mApi.showMsg(this, "成功连接至服务器");
                        });
                    }else{
                        sendHandlerMsg(BOT_BEGIN, null);
                        sendHandlerMsg(BOT_CONTINUE, "Failed to connect to the server");
                        sendHandlerMsg(BOT_END, null);
                    }
                }catch (URISyntaxException | InterruptedException e){
                    e.printStackTrace();
                }
            }).start();
        });
        isConnecting = true;

    }
    void refreshListview(){
        chatListAdapter.notifyDataSetChanged();
        result.setAdapter(chatListAdapter);
    }
    void sendHandlerMsg(int what, String msg){
        Message message = new Message();
        message.what = what;
        if(null == msg){
            msg = "";
        }
        message.obj = msg;
        handler.sendMessage(message);
    }
    void showHelp(){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.layout_help, null);
        TextView help = view.findViewById(R.id.layout_help_text);
        help.setMovementMethod(ScrollingMovementMethod.getInstance());
        b.setView(view);
        b.setNegativeButton("已  阅", (dialog, which) -> dialog.dismiss());
        b.show();
    }
    String buildPrompt(){
        StringBuilder prompt = new StringBuilder();
        if(history.size() > 0){
            for(String s: history){
                prompt.append(s);
            }
        }
        prompt.append("Q: ").append(input.getText().toString()).append("\n\nA:");
        return prompt.toString();
    }
    void chatGPT_direct(){
        String endpoint;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(mApi.RequestTimeout, TimeUnit.SECONDS)
                .writeTimeout(mApi.RequestTimeout, TimeUnit.SECONDS)
                .readTimeout(mApi.RequestTimeout, TimeUnit.SECONDS)
                .build();
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("model",mApi.model);
        jsonObject.put("prompt", buildPrompt());
        jsonObject.put("max_tokens",mApi.max_token);
        jsonObject.put("temperature",mApi.temperature);
        jsonObject.put("top_p",1);
        jsonObject.put("stream",mApi.stream);
        if(mApi.model.equals("gpt-3.5-turbo") || mApi.model.equals("gpt-3.5-turbo-0301")){
            endpoint = "https://api.openai.com/v1/chat/completions";
            List<JSONObject> list = new ArrayList<>();
            JSONObject msg1 = new JSONObject();
            msg1.put("role", "user");
            msg1.put("content", "我是小明");
            list.add(msg1);
            JSONObject msg = new JSONObject();
            msg.put("role", "user");
            msg.put("content", jsonObject.getString("prompt"));
            list.add(msg);
            jsonObject.put("messages", list);
            jsonObject.remove("prompt");
        }else{
            endpoint = "https://api.openai.com/v1/completions";
        }
        sendHandlerMsg(USER_MSG, input.getText().toString());
        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + mApi.API_KEY)
                .post(RequestBody.create(jsonObject.toString(),
                        MediaType.parse("application/json")))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sendHandlerMsg(BOT_BEGIN, null);
                if (response.isSuccessful()) {
                    InputStream inputStream = response.body().byteStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder res = new StringBuilder();
                    try {
                        while ((line = reader.readLine()) != null) {
                            if(line.length()<50){
                                continue;
                            }
                            JSONObject object = JSONObject.parseObject(line.substring(6));
                            JSONObject choices = JSONObject.parseObject(object.getString("choices")
                                    .replace('[',' ')
                                    .replace(']',' '));
                            String s;
                            if(mApi.model.equals("gpt-3.5-turbo") || mApi.model.equals("gpt-3.5-turbo-0301")){
                                s = JSONObject.parseObject(choices.getString("delta")).getString("content");
                            }else {
                                s = choices.getString("text");
                            }
                            res.append(s);
                            sendHandlerMsg(BOT_CONTINUE, s);
                        }
                        reader.close();
                        inputStream.close();
                        sendHandlerMsg(BOT_END, res.toString());
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    sendHandlerMsg(BOT_CONTINUE, "SERVER ERROR 0X2H\n");
                    sendHandlerMsg(BOT_CONTINUE, response.body().source().readUtf8());
                    sendHandlerMsg(BOT_END, "");
                }
            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sendHandlerMsg(BOT_CONTINUE, "SERVER ERROR 0X3H\n");
                sendHandlerMsg(BOT_END, "");
            }
        });

    }
    void chatGPT_vps(){
        if(input.getText().toString().equals("")){
            mApi.showMsg(this, "请先输入文本");
        }else{
            if(!(null == webSocketClient) && webSocketClient.isOpen()){
                if(isBotTalking){
                    mApi.showMsg(this, "请等待 AI 回答结束");
                    return;
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("model",mApi.model);
                jsonObject.put("prompt", buildPrompt());
                jsonObject.put("max_tokens",mApi.max_token);
                jsonObject.put("temperature",mApi.temperature);
                jsonObject.put("top_p",1);
                jsonObject.put("stream",mApi.stream);
                jsonObject.put("api_key",mApi.API_KEY);
                webSocketClient.send(jsonObject.toString());
                sendHandlerMsg(USER_MSG, input.getText().toString());
                sendHandlerMsg(BOT_BEGIN, null);
            }else{
                mApi.showMsg(this, "未连接至服务器");
            }
        }
    }
    void initConfigs(View view){
        ArrayList<ArrayList<?>> list = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(3, 5, 10, 30, 50, 70, 100, 200)),
                new ArrayList<>(Arrays.asList(20, 50, 100, 200, 500, 1000, 2000)),
                new ArrayList<>(Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6,
                        0.7, 0.8, 0.9, 1.0)),
                new ArrayList<>(Arrays.asList("gpt-3.5-turbo", "gpt-3.5-turbo-0301",
                        "text-davinci-003", "text-davinci-002")),
                new ArrayList<>(Arrays.asList(true)),
                new ArrayList<>(Arrays.asList(10, 20, 30, 50, 70, 100)),
                new ArrayList<>(Arrays.asList("不使用中转", "自定义服务器", "英国 S1", "美国 S1", "美国 S2"))
        ));
        ArrayList<Spinner> spinners = new ArrayList<>();
        spinners.add(view.findViewById(R.id.config_timeout));
        spinners.add(view.findViewById(R.id.config_max_token));
        spinners.add(view.findViewById(R.id.config_temperature));
        spinners.add(view.findViewById(R.id.config_model));
        spinners.add(view.findViewById(R.id.config_stream));
        spinners.add(view.findViewById(R.id.config_history));
        spinners.add(view.findViewById(R.id.config_useVps));
        EditText text = view.findViewById(R.id.config_custom_vps);
        text.setText(mApi.custom_url);
        for(int i = 0; i < list.size(); i++){
            setSpinnerAdapter(spinners.get(i), list.get(i), i);
        }
    }
    void setSpinnerAdapter(Spinner sp, ArrayList<?> arrayList, int flag){
        ArrayAdapter<?> starAdapter =
                new ArrayAdapter<>(this, R.layout.item_select, arrayList);
        starAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp.setAdapter(starAdapter);
        switch (flag){
            case 0:
                sp.setSelection(arrayList.indexOf((int)mApi.RequestTimeout));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.RequestTimeout = (long)(int) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 1:
                sp.setSelection(arrayList.indexOf(mApi.max_token));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.max_token = (int) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 2:
                sp.setSelection(arrayList.indexOf(mApi.temperature));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.temperature = (double) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 3:
                sp.setSelection(arrayList.indexOf(mApi.model));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.model = (String) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 4:
                sp.setSelection(arrayList.indexOf(mApi.stream));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.stream = (boolean) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 5:
                sp.setSelection(arrayList.indexOf(mApi.max_history));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.max_history = (int) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
            case 6:
                sp.setSelection(arrayList.indexOf(mApi.use_vps));
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        mApi.use_vps = (String) sp.getSelectedItem();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                break;
        }
    }
    private void showConfig() {
        TextView title = findViewById(R.id.chat_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.layout_config, null);
        initConfigs(view);
        final EditText config_key = view.findViewById(R.id.config_key);
        if(!(null == mApi.API_KEY) && !mApi.API_KEY.equals("")){
            config_key.setText(mApi.API_KEY);
        }
        builder.setView(view);
        builder.setTitle("设置");
        builder.setNegativeButton("取 消", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("确 定", (dialog, which) -> {
            mApi.API_KEY = config_key.getText().toString().trim();
            SharedPreferences.Editor ed = mApi.sharedPreferences.edit();
            ed.putString("key",mApi.API_KEY);
            ed.putString("timeout", String.valueOf(mApi.RequestTimeout));
            ed.putString("max_token", String.valueOf(mApi.max_token));
            ed.putString("temperature", String.valueOf(mApi.temperature));
            ed.putString("model", mApi.model);
            ed.putString("stream", String.valueOf(mApi.stream));
            ed.putString("max_history", String.valueOf(mApi.max_history));
            ed.putString("use_vps", String.valueOf(mApi.use_vps));
            ed.putString("custom_url", mApi.custom_url);
            EditText custom_url = view.findViewById(R.id.config_custom_vps);
            mApi.custom_url = custom_url.getText().toString().trim();
            ed.apply();
            title.setText("Chat : " + mApi.model);
        }).show();
    }
    @Override
    public void onBackPressed(){
        if(mBackPressed>System.currentTimeMillis()-2000){
            ActivityController.getInstance().killAllActivity();
            super.onBackPressed();
        }else{
            mApi.showMsg(this,"连续返回两次退出APP");
            mBackPressed=System.currentTimeMillis();
        }
    }
    class WebSocketClientEx extends WebSocketClient {
        WebSocketClientEx(URI uri){
            super(uri);
        }
        @Override
        public void onOpen(ServerHandshake handshakeData) {
            isConnecting = false;
            isBotTalking = false;
        }
        @Override
        public void onMessage(String message) {
            if(message.equals("///**1100101**///") && isBotTalking){
                sendHandlerMsg(BOT_END, bot_record);
                Log.e("Msg", bot_record);
                bot_record = "";
            }else {
                bot_record += message;
                sendHandlerMsg(BOT_CONTINUE, message);
            }
        }
        @Override
        public void onClose(int code, String reason, boolean remote) {;
            isConnecting = false;
            isBotTalking = false;
            webSocketClient = null;
            mApi.showMsg(Chat.this, "服务器连接断开");
            Log.e("Close", reason);
        }
        @Override
        public void onError(Exception ex) {
            isConnecting = false;
            isBotTalking = false;
            webSocketClient.close();
            webSocketClient = null;
            mApi.showMsg(Chat.this, "");
            mApi.showMsg(Chat.this, "服务器连接错误： " + ex.getMessage());
            Log.e("Exception", ex.getMessage());
        }
    }
}