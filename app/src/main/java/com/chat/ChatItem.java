package com.chat;

public class ChatItem {
    private int type;
    private String text = "";
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public void appendText(String text) {
        this.text += text;
    }
}