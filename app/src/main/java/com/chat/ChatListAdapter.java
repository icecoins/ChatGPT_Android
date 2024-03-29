package com.chat;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
public class ChatListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final Chat chat;
    Bitmap say_available;
    public ChatListAdapter(Context context, Chat chat) {
        inflater = LayoutInflater.from(context);
        say_available = BitmapFactory.decodeResource(context.getResources(), R.mipmap.say_ex);
        this.chat = chat;
    }

    @Override
    public int getCount() {
        return mApi.chatItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mApi.chatItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        ViewHolder holder;
        ChatItem currentItem = mApi.chatItems.get(position);
        if(contentView == null){
            if(getItemViewType(position) == 0){
                holder = new ViewHolder();
                contentView = inflater.inflate(R.layout.chatview_bot,null);
                holder.text = contentView.findViewById(R.id.text_bot_in);
                holder.say = contentView.findViewById(R.id.bot_say);
            }else {
                holder = new ViewHolder();
                contentView = inflater.inflate(R.layout.chatview_user,null);
                holder.text = contentView.findViewById(R.id.text_user_in);
                holder.say = contentView.findViewById(R.id.user_say);
            }
            contentView.setTag(holder);
        }else {
            holder = (ViewHolder) contentView.getTag();
        }
        if(currentItem.isSoundDownloaded()){
            holder.say.setImageBitmap(say_available);
        }
        holder.say.setOnClickListener(v->{
            chat.fetchSound(position);
        });
        holder.text.setText(currentItem.getText().trim());
        return contentView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
    @Override
    public int getItemViewType(int position) {
        ChatItem bean = mApi.chatItems.get(position);
        return bean.getType();
    }

    public static class ViewHolder{
        public TextView text;
        public ImageView say;
    }
}