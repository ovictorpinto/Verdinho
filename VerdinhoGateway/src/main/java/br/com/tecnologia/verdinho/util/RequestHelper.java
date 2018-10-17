package br.com.tecnologia.verdinho.util;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RequestHelper {

    static MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client;

    public RequestHelper() {
        client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = SslUtil.trustAllSslClient(client).newCall(request).execute();
        return response.body().string();
    }
}
