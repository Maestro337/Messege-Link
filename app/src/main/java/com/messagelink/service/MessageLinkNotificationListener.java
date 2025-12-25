package com.messagelink.service;

import android.app.Notification;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Handler;
import android.os.Looper;
import android.app.Notification.InboxStyle;

import com.messagelink.data.NotificationRepository;
import com.messagelink.data.SettingsStore;
import com.messagelink.data.db.NotificationEntity;
import com.messagelink.net.PiClient;

import org.json.JSONObject;

public class MessageLinkNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        String pkg = sbn.getPackageName();
        if (pkg == null || pkg.trim().isEmpty()) return;

        Notification n = sbn.getNotification();
        if (n == null) return;

        Bundle ex = n.extras;
        String title = "";
        String text = "";
        if (ex != null) {
            CharSequence[] lines = ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
            if (lines != null && lines.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (CharSequence line : lines) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line);
                }
                text = sb.toString();
            CharSequence t = ex.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence tx = ex.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence big = ex.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (t != null) title = t.toString();
            if (big != null && big.length() > 0) text = big.toString();
            else if (tx != null) text = tx.toString();
        }

        if (title.trim().isEmpty() && text.trim().isEmpty()) return;

        String appName = resolveAppName(pkg);
        long when = sbn.getPostTime();

        NotificationEntity e = new NotificationEntity(pkg, appName, title, text, when);
        NotificationRepository repo = NotificationRepository.get(getApplicationContext());
        repo.insert(e);


        long retentionMs = SettingsStore.getRetentionDays(getApplicationContext()) * 24L * 60 * 60 * 1000;
        long cutoff = System.currentTimeMillis() - retentionMs;
        NotificationRepository repo = NotificationRepository.get(getApplicationContext());
        repo.cleanupOld(cutoff);

        if (SettingsStore.isPiEnabled(this)) {
            String url = SettingsStore.getPiUrl(this);
            try {
                JSONObject o = new JSONObject();
                o.put("packageName", pkg);
                o.put("appName", appName);
                o.put("title", title);
                o.put("text", text);
                o.put("postedAt", when);
                o.put("source", "MessageLink Android");
                PiClient.post(this, url, o);
            } catch (Exception ignored) {}
        }
    }

    private String resolveAppName(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            CharSequence label = pm.getApplicationLabel(ai);
            if (label != null) return label.toString();
        } catch (Exception ignored) {}
        return pkg;
    }
}
