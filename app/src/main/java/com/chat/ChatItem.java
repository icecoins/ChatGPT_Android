package com.chat;

public class ChatItem {
    private int type;
    private String text = "";
    private String soundPath = "";
    private boolean isSoundDownload = false;
    private boolean isCurrentBot = false;

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public boolean isSoundDownloaded() {
        return isSoundDownload;
    }

    public void setSoundDownload(boolean soundDownload) {
        isSoundDownload = soundDownload;
    }

    public boolean isCurrentBot() {
        return isCurrentBot;
    }
    public void setCurrentBot(boolean currentBot) {
        isCurrentBot = currentBot;
    }

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