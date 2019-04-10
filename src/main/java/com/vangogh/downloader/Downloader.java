package com.vangogh.downloader;

import com.vangogh.downloader.utilities.StringUtils;

import java.io.*;
import java.net.URL;

public class Downloader extends Thread {
    private String url;
    private Boolean running;
    private ResultCallback callback;

    private static final int BUFFER_SIZE = 1024;

    public Downloader(String url, ResultCallback callback) {
        this.url = url;
        this.callback = callback;
        this.running = true;
    }

    @Override
    public void run() {
        super.run();

        download(callback);
    }

    public String getUrl() {
        return url;
    }

    /**
     * Download the file by from specified target URL
     * Given ResultCallback as the parameter to retrieve the result asynchronously
     * @param callback
     */
    private void download(ResultCallback callback) {
        callback.onStarted(Downloader.this, StringUtils.toMD5(url));

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());

            // Change downloaded file to the preferred location
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1 && running) {
                bos.write(buffer, 0, bytesRead);
            }

            byte[] data = bos.toByteArray();

            if (running) {
                callback.onFinished(data, StringUtils.toMD5(url));
            }
            else {
                callback.onStopped(StringUtils.toMD5(url));
            }

            inputStream.close();
            bos.close();
        }
        catch (IOException e) {
            callback.onFailed(e);
        }
    }

    /**
     * Stop the loop and intercept the thread
     */
    public void cancel() {
        this.running = false;
    }

    /**
     * Convert file to array of byte
     * @param file with specified full file path
     * @return array of byte
     */
    private byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }

    /**
     * Encoded url would be either used as downloader and cached data key
     */
    public interface ResultCallback {
        void onStarted(Downloader downloader, String encodedUrl);
        void onFinished(byte[] data, String encodedUrl);
        void onFailed(IOException e);
        void onStopped(String encodedUrl);
    }
}
