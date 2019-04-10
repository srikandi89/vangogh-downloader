package com.vangogh.downloader;

import com.vangogh.downloader.utilities.StringUtils;

import static org.junit.Assert.*;
import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testToMD5() {
        String actual = StringUtils.toMD5("Otniel");
        String expected = "45311628f094f52efce762b45fdd7f32";
        assertEquals(expected, actual);
    }
}
