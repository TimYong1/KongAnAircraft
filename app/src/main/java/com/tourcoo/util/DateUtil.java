package com.tourcoo.util;

import java.text.SimpleDateFormat;
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
}
