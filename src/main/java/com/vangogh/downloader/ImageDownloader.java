package com.vangogh.downloader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.vangogh.downloader.utilities.ImageUtils;
import com.vangogh.downloader.utilities.StringUtils;

import java.io.IOException;

public class ImageDownloader extends DownloadManager {
    private Handler handler;
    private static final int BYTE_SIZE = 1024;
    private static final int DEFAULT_MAX_THREAD = 10;
    private static final int DEFAULT_MAX_TOTAL_BYTE = 500*BYTE_SIZE;
    private static final long SECOND = 1000;
    private static final long MINUTE = 60*SECOND;
    private static final long LIFE_TIME_MILLIS = MINUTE;

    public ImageDownloader() {
        super(DEFAULT_MAX_THREAD, DEFAULT_MAX_TOTAL_BYTE);
        invalidator.setLifeTimeMillis(LIFE_TIME_MILLIS);
        invalidator.start();
    }

    public ImageDownloader(int maxThread, int maxTotalBytes) {
        super(maxThread, maxTotalBytes);
        invalidator.setLifeTimeMillis(LIFE_TIME_MILLIS);
        invalidator.start();
    }

    @Override
    public void download(String url, Downloader.ResultCallback result) {

        Downloader downloader = new Downloader(url, result);

        manageDownloader(downloader);
    }

    public void toImageView(String url, final ImageView view) {

        download(url, new Downloader.ResultCallback() {
            @Override
            public void onStarted(Downloader downloader, final String encodedUrl) {
                // TODO : Do something before download finished
                boolean equal = StringUtils.toMD5(downloader.getUrl()).equals(encodedUrl);
                Log.d(ImageDownloader.class.getSimpleName(), encodedUrl+" starting to download image from: "+downloader.getUrl()+", "+equal);

                // Read from cache if exist
                if (cachedData.get(encodedUrl) != null) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] data = cachedData.get(encodedUrl);
                            ImageUtils.setImage(data, view);
                        }
                    });
                }
            }

            @Override
            public void onFinished(final byte[] data, final String encodedUrl) {
                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Re-render with the newest one
                        ImageUtils.setImage(data, view);

                        // Cache data if this byte never been cached
                        if (cachedData.get(encodedUrl) == null) {
                            cacheByteData(encodedUrl, data);
                        }

                        if (downloaders.get(encodedUrl) != null) {
                            downloaders.remove(encodedUrl);

                            Log.d(ImageDownloader.class.getSimpleName(), "Removing "+encodedUrl+" from worker pool");
                        }

                        if (downloaderQueue.size() > 0) {
                            Downloader fromQueue = downloaderQueue.pop();
                            downloaders.put(encodedUrl, fromQueue);
                        }
                    }
                });
            }

            @Override
            public void onFailed(IOException e) {
                Log.d(ImageDownloader.class.getSimpleName(), e.getMessage());
            }

            @Override
            public void onStopped(String encodedUrl) {
                Log.d(ImageDownloader.class.getSimpleName(), "Thread "+encodedUrl+" just stopped :D");
            }
        });
    }

    public int getMaxThread() {
        return this.maxThread;
    }

    public int getMaxTotalBytes() {
        return this.maxTotalBytes;
    }
}
