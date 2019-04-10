package com.vangogh.downloader;

import com.vangogh.downloader.utilities.TimeUtils;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class TimeUtilsTest {
    @Test
    public void testTimeDiffMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 03, 10, 10, 9, 10);

        Date date1 = calendar.getTime();

        calendar.set(2019, 03, 10, 10, 9, 20);
        Date date2 = calendar.getTime();

        long actual = TimeUtils.timeDiffMillis(date1, date2);
        long expected = 10000;
        assertEquals(expected, actual);
    }
}
