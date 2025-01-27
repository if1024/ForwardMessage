package com.thssh.smsdispatcher.dispatcher;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.thssh.smsdispatcher.App;
import com.thssh.smsdispatcher.model.AppManager;
import com.thssh.smsdispatcher.net.RemoteService;
import com.thssh.smsdispatcher.settings.Settings;
import com.thssh.smsdispatcher.tools.Util;

import java.util.Locale;
import java.util.Set;

/**
 * 荣耀手机
 */
public class HonorDispatcher extends CommonDispatcher {

    private static final String TAG = "HonorDispatcher";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void dispatch(StatusBarNotification sbn, Type type) {

        Bundle extras = sbn.getNotification().extras;
        String packageName = sbn.getPackageName();
        String title = "";
//        String content = Util.o2c(sbn.getNotification().tickerText);
        String content = "";
        String subText = "";
        String name = "";
        long when = sbn.getNotification().when;
        if (extras.get(Notification.EXTRA_TITLE) != null) {
            title = Util.o2c(extras.getCharSequence(Notification.EXTRA_TITLE));
        }
        if (extras.get(Notification.EXTRA_SUB_TEXT) != null) {
            subText = Util.o2c(extras.getCharSequence(Notification.EXTRA_SUB_TEXT));
        }
        if (TextUtils.isEmpty(content) && extras.get(Notification.EXTRA_TEXT) != null) {
            content = Util.o2c(extras.getCharSequence(Notification.EXTRA_TEXT));
        }
        content = beatify(content);

        try {
            PackageManager pm = App.getAppContext().getPackageManager();
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String combinedTitle = String.format(Locale.getDefault(), "%s|%s|%s|%s", Util.getPhoneNumber(), packageName,name, title);
        Log.i(TAG, "dispatch: " + packageName + "[" + combinedTitle + "]" + content + "|" + subText + "|" + print(sbn.getNotification()));
        Set<String> includeSet = getSettings().getIncludeSet();
        Set<String> excludeSet = getSettings().getExcludeSet();
        if (includeSet != null && includeSet.size() < 1
                && excludeSet != null && excludeSet.contains(packageName)) return;
        if (includeSet != null && !includeSet.contains(packageName)) return;

        if (type == Type.POST) {
            RemoteService.get().sendMessage(when, combinedTitle, content);
            NotificationManagerCompat.from(App.getAppContext()).cancel(sbn.getId());
        }
    }

    private String print(Notification notification) {
        StringBuilder sb = new StringBuilder("Notification: \n");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append("category: ").append(notification.category).append("\n");
            sb.append("EXTRA_TITLE: ").append(notification.extras.getCharSequence(Notification.EXTRA_TITLE)).append("\n");
            sb.append("EXTRA_TEXT: ").append(notification.extras.getCharSequence(Notification.EXTRA_TEXT)).append("\n");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sb.append("getChannelId: ").append(notification.getChannelId()).append("\n");
        }
        sb.append("when: ").append(notification.when).append("\n");
        sb.append("tickerText: ").append(notification.tickerText).append("\n");
        sb.append("toString: ").append(notification.toString()).append("\n");
        return sb.toString();
    }

    public Settings getSettings() {
        return AppManager.getInstance().getSettings();
    }

}
