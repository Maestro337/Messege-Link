package com.messagelink.net;

import org.json.JSONObject;

import java.io.IOException;

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

    public static void post(String url, JSONObject payload) {
        if (url == null) return;
        String u = url.trim();
        if (u.isEmpty()) return;
        RequestBody body = RequestBody.create(payload == null ? "{}" : payload.toString(), JSON);
        Request req = new Request.Builder().url(u).post(body).build();
        client.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException { response.close(); }
        });
    }
}
