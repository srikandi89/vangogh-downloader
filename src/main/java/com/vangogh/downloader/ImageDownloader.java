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
                boolean equal = StringUtils.toMD5(downloader.getUrl()).equals(encodedUrl);

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
                Log.e(ImageDownloader.class.getSimpleName(), e.getMessage());
            }

            @Override
            public void onStopped(String encodedUrl) {
                Log.i(ImageDownloader.class.getSimpleName(), "Thread "+encodedUrl+" just stopped");
            }
        });
    }

    /**
     * Directly display the downloaded data into ImageView
     * Convert byte of data into Bitmap and display it into ImageView
     * Have additional listener to control what we need to do when met these conditions:
     * - On Download Started
     * - On Download Finished
     * - On Download Stopped
     * - On Download Finished
     * @param url
     * @param view
     * @param listener
     */
    public void toImageView(String url, final ImageView view, final OnDownloadListener listener) {

        download(url, new Downloader.ResultCallback() {
            @Override
            public void onStarted(Downloader downloader, final String encodedUrl) {
                boolean equal = StringUtils.toMD5(downloader.getUrl()).equals(encodedUrl);

                // Read from cache if exist
                if (cachedData.get(encodedUrl) != null) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] data = cachedData.get(encodedUrl);
                            listener.onDownloadStarted();
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
                        listener.onDownloadFinished();
                        ImageUtils.setImage(data, view);

                        // Cache data if this byte never been cached
                        if (cachedData.get(encodedUrl) == null) {
                            cacheByteData(encodedUrl, data);
                        }

                        if (downloaders.get(encodedUrl) != null) {
                            downloaders.remove(encodedUrl);
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
                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadFailed();
                    }
                });
            }

            @Override
            public void onStopped(String encodedUrl) {
                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadStopped();
                    }
                });
            }
        });
    }

    public int getMaxThread() {
        return this.maxThread;
    }

    public int getMaxTotalBytes() {
        return this.maxTotalBytes;
    }

    public interface OnDownloadListener {
        void onDownloadFinished();
        void onDownloadStarted();
        void onDownloadStopped();
        void onDownloadFailed();
    }
}
