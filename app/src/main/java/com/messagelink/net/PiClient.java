package com.messagelink.net;

import org.json.JSONObject;

import java.io.IOException;

import android.util.Log;
import android.content.Context;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class PiClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    private PiClient() {}

    public static void post(Context context, String url, JSONObject payload) {
        if (url == null) return;
        String u = url.trim();
        if (u.isEmpty()) return;
        RequestBody body = RequestBody.create(payload == null ? "{}" : payload.toString(), JSON);
        Request req = new Request.Builder().url(u).post(body).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e("PiClient", "Failed to post to Pi: " + e.getMessage());
                if (context != null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Ошибка отправки на Pi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("PiClient", "Pi response error: " + response.code());
                    if (context != null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "Ошибка от Pi: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }
                response.close();
            }
        });
    }
}
