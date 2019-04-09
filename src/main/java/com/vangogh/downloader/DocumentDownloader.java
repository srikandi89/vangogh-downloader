package com.vangogh.downloader;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DocumentDownloader extends DownloadManager {
    private Handler handler;
    private static final int BYTE_SIZE = 1024;
    private static final int DEFAULT_MAX_THREAD = 10;
    private static final int DEFAULT_MAX_TOTAL_BYTE = 500*BYTE_SIZE;

    public DocumentDownloader() {
        super(DEFAULT_MAX_THREAD, DEFAULT_MAX_TOTAL_BYTE);
    }

    public DocumentDownloader(int maxThread, int maxTotalBytes) {
        super(maxThread, maxTotalBytes);
    }

    @Override
    public void download(String url, Downloader.ResultCallback result) {
        Downloader downloader = new Downloader(url, result);

        manageDownloader(downloader);
    }

    public void toJSON(String url, final DocumentResponse response) {
        download(url, new Downloader.ResultCallback() {
            @Override
            public void onStarted(Downloader downloader, String encodedUrl) {
                if (cachedData.get(encodedUrl) != null) {
                    cachedData.put(encodedUrl, null);
                }

                response.onStart();
            }

            @Override
            public void onFinished(byte[] data, String encodedUrl) {
                final String rawResponse = new String(data);

                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (rawResponse.startsWith("[")) {
                                JSONArray jsonArray = new JSONArray(rawResponse);
                                response.onSuccess(rawResponse, jsonArray);
                            }
                            else {
                                JSONObject jsonObject = new JSONObject(rawResponse);
                                response.onSuccess(rawResponse, jsonObject);
                            }

                        } catch (JSONException e) {
                            response.onFailed(e);
                        }
                    }
                });

            }

            @Override
            public void onFailed(final IOException e) {
                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        response.onFailed(e);
                    }
                });
            }

            @Override
            public void onStopped(String encodedUrl) {

            }
        });
    }

    public interface DocumentResponse<T> {
        void onStart();
        void onSuccess(String raw, T response);
        void onFailed(Exception e);
    }
}
