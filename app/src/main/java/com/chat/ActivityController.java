package com.chat;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;
public class ActivityController {
    private ActivityController() {}
    private static ActivityController instance = new ActivityController();
    private static List<Activity> activityList = new ArrayList<>();

    public static ActivityController getInstance() {
        return instance;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void killAllActivity() {
        for(Activity activity : activityList){
            if(activity != null){
                activity.finish();
            }
        }
        activityList.clear();
    }
}
