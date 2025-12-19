package com.messagelink.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsStore {
    private static final String PREFS = "ml_prefs";
    private static final String KEY_PI_ENABLED = "pi_enabled";
    private static final String KEY_PI_URL = "pi_url";

    private SettingsStore() {}

    public static boolean isPiEnabled(Context c) {
        return prefs(c).getBoolean(KEY_PI_ENABLED, false);
    }

    public static String getPiUrl(Context c) {
        return prefs(c).getString(KEY_PI_URL, "");
    }

    public static void setPiEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean(KEY_PI_ENABLED, enabled).apply();
    }

    public static void setPiUrl(Context c, String url) {
        prefs(c).edit().putString(KEY_PI_URL, url == null ? "" : url.trim()).apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
