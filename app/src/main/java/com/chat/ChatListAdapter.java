package com.chat;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {
    private final List<ChatItem> chatItems;
    private final LayoutInflater inflater;

    public ChatListAdapter(List<ChatItem> chatItems, Context context) {
        this.chatItems = chatItems;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatItems.size();
    }

    @Override
    public Object getItem(int i) {
        return chatItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(contentView == null){
            if(getItemViewType(position) == 0){
                holder = new ViewHolder();
                contentView = inflater.inflate(R.layout.chatview_bot,null);
                holder.title = (TextView) contentView.findViewById(R.id.text_in);
            }else {
                holder = new ViewHolder();
                contentView = inflater.inflate(R.layout.chatview_me,null);
                holder.title = (TextView) contentView.findViewById(R.id.text_in);
            }
            contentView.setTag(holder);
        }else {
            holder = (ViewHolder) contentView.getTag();
        }
        holder.title.setText(chatItems.get(position).getText());
        return contentView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
    @Override
    public int getItemViewType(int position) {
        ChatItem bean = chatItems.get(position);
        return bean.getType();
    }

    public static class ViewHolder{
        public TextView title;
    }
}