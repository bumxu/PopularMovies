package com.bumxu.android.popularmovies.app.util;

public class TimeUtils {
    public static String humanizeSeconds(int seconds) {
        String out;

        int h = seconds / 3600;
        seconds -= h * 3600;

        int m = seconds / 60;
        seconds -= m * 60;

        int s = seconds;

        out = pad(s, 2);

        if (h > 0) {
            out = h + ":" + pad(m, 2) + ":" + out;
        } else {
            out = m + ":" + out;
        }

        return out;
    }

    private static String pad(int n, int len) {
        String out = String.valueOf(n);

        while (out.length() < len) {
            out = "0" + out;
        }

        return out;
    }
}
