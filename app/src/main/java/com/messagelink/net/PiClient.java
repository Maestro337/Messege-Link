package com.messagelink.net;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class PiClient {
    private static final String TAG = "PiClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .callTimeout(8, TimeUnit.SECONDS)
            .build();

    private PiClient() {}

    /**
     * Отправляет JSON POST на указанный URL.
     * Возвращает сразу (асинхронно), результат можно увидеть в Logcat.
     */
    public static void post(String url, JSONObject payload) {
        String u = normalizeUrl(url);
        if (u == null) return;

        String json = payload == null ? "{}" : payload.toString();
        RequestBody body = RequestBody.create(json, JSON);

        Request req = new Request.Builder()
                .url(u)
                .post(body)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w(TAG, "POST failed: " + u + " err=" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                response.close();
                if (code >= 200 && code < 300) {
                    Log.d(TAG, "POST ok: " + u + " code=" + code);
                } else {
                    Log.w(TAG, "POST not ok: " + u + " code=" + code);
                }
            }
        });
    }


    private static String normalizeUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.isEmpty()) return null;

        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "http://" + u;
        }
        return u;
    }
}
