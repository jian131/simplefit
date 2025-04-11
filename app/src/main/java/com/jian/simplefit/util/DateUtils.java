package com.jian.simplefit.util;

import android.content.Context;
import com.jian.simplefit.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Lớp tiện ích xử lý và định dạng ngày tháng
 */
public class DateUtils {

    private static final String DATE_FORMAT_FULL = "dd/MM/yyyy HH:mm";
    private static final String DATE_FORMAT_SIMPLE = "dd/MM/yyyy";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATE_FORMAT_MONTH_YEAR = "MM/yyyy";
    private static final String DATE_FORMAT_WEEKDAY = "EEEE, dd/MM";

    /**
     * Định dạng timestamp thành chuỗi ngày tháng đầy đủ
     * @param timestamp Timestamp cần định dạng
     * @return Chuỗi ngày tháng định dạng dd/MM/yyyy HH:mm
     */
    public static String formatDateFull(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FULL, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Định dạng timestamp thành chuỗi ngày tháng đơn giản
     * @param timestamp Timestamp cần định dạng
     * @return Chuỗi ngày tháng định dạng dd/MM/yyyy
     */
    public static String formatDateSimple(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SIMPLE, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Định dạng đối tượng Date thành chuỗi ngày tháng đơn giản
     * @param date Đối tượng Date cần định dạng
     * @return Chuỗi ngày tháng định dạng dd/MM/yyyy
     */
    public static String formatDateSimple(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SIMPLE, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Định dạng timestamp thành chuỗi thời gian
     * @param timestamp Timestamp cần định dạng
     * @return Chuỗi thời gian định dạng HH:mm
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Định dạng timestamp thành chuỗi tháng và năm
     * @param timestamp Timestamp cần định dạng
     * @return Chuỗi tháng và năm định dạng MM/yyyy
     */
    public static String formatMonthYear(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_MONTH_YEAR, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Định dạng timestamp thành chuỗi thứ trong tuần và ngày tháng
     * @param timestamp Timestamp cần định dạng
     * @return Chuỗi thứ trong tuần và ngày tháng định dạng EEEE, dd/MM
     */
    public static String formatWeekdayDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_WEEKDAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Tạo timestamp bắt đầu của ngày (00:00:00)
     * @param timestamp Timestamp bất kỳ trong ngày
     * @return Timestamp lúc 00:00:00 của ngày đó
     */
    public static long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Tạo timestamp kết thúc của ngày (23:59:59)
     * @param timestamp Timestamp bất kỳ trong ngày
     * @return Timestamp lúc 23:59:59 của ngày đó
     */
    public static long getEndOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * Tạo timestamp bắt đầu của tuần (Thứ Hai, 00:00:00)
     * @param timestamp Timestamp bất kỳ trong tuần
     * @return Timestamp lúc 00:00:00 của ngày Thứ Hai trong tuần đó
     */
    public static long getStartOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return getStartOfDay(calendar.getTimeInMillis());
    }

    /**
     * Tạo timestamp kết thúc của tuần (Chủ Nhật, 23:59:59)
     * @param timestamp Timestamp bất kỳ trong tuần
     * @return Timestamp lúc 23:59:59 của ngày Chủ Nhật trong tuần đó
     */
    public static long getEndOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return getEndOfDay(calendar.getTimeInMillis());
    }

    /**
     * Tạo timestamp bắt đầu của tháng (ngày 1, 00:00:00)
     * @param timestamp Timestamp bất kỳ trong tháng
     * @return Timestamp lúc 00:00:00 của ngày 1 trong tháng đó
     */
    public static long getStartOfMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTimeInMillis());
    }

    /**
     * Tạo timestamp kết thúc của tháng (ngày cuối cùng, 23:59:59)
     * @param timestamp Timestamp bất kỳ trong tháng
     * @return Timestamp lúc 23:59:59 của ngày cuối cùng trong tháng đó
     */
    public static long getEndOfMonth(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(calendar.getTimeInMillis());
    }

    /**
     * Định dạng khoảng thời gian thành chuỗi
     * @param context Context để truy cập chuỗi tài nguyên
     * @param durationMillis Thời gian tính bằng mili giây
     * @return Chuỗi thời gian định dạng "X giờ Y phút"
     */
    public static String formatDuration(Context context, long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;

        if (hours > 0) {
            return context.getString(R.string.hours_minutes, hours, minutes);
        } else {
            return context.getString(R.string.minutes, minutes);
        }
    }
}