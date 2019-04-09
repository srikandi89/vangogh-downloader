package com.vangogh.downloader.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageUtils {
    public static void setImage(final byte[] data, final ImageView view) {
        if (view != null) {
            Log.d(ImageUtils.class.getSimpleName(), "Width: "+view.getWidth()+", height: "+view.getHeight());

            view.post(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    view.setImageBitmap(Bitmap.createScaledBitmap(
                            bmp,
                            view.getWidth(),
                            view.getHeight(),
                            false)
                    );
                }
            });
        }
    }
}
