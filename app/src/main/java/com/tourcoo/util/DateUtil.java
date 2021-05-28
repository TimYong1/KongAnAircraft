package com.tourcoo.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2021年04月15日16:17
 * @Email: 971613168@qq.com
 */
public class DateUtil {
    public static String parseDateString(String pattern, long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(timestamp);
    }


    /**
     * * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     * *
     * * @param strDate
     * * @return
     */
    public static Date stringParseToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }


    public static String stringForTime(float timeSecond) {
        if (timeSecond <= 0 || timeSecond >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int seconds = (int) (timeSecond % 60);
        int minutes = (int) ((timeSecond / 60) % 60);
        int hours = (int) (timeSecond / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    public static String parseDate(String pattern, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }
}
