package com.vangogh.downloader.utilities;

import java.util.Date;

public class TimeUtils {
    public static long timeDiffMillis(Date date1, Date date2) {
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());

        return diffInMillis;
    }
}
