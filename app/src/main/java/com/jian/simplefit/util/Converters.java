package com.jian.simplefit.util;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jian.simplefit.data.local.entity.UserEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Lớp chứa các phương thức chuyển đổi kiểu dữ liệu cho Room Database
 * Sử dụng TypeConverter để chuyển đổi giữa các kiểu dữ liệu phức tạp và kiểu dữ liệu cơ bản mà SQLite hỗ trợ
 */
public class Converters {

    private static final Gson gson = new Gson();

    /**
     * Chuyển đổi danh sách chuỗi thành chuỗi JSON
     * @param list Danh sách chuỗi
     * @return Chuỗi JSON
     */
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Chuyển đổi chuỗi JSON thành danh sách chuỗi
     * @param value Chuỗi JSON
     * @return Danh sách chuỗi
     */
    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Chuyển đổi danh sách đối tượng WeightHistoryEntry thành chuỗi JSON
     * @param list Danh sách WeightHistoryEntry
     * @return Chuỗi JSON
     */
    @TypeConverter
    public static String fromWeightHistoryList(List<UserEntity.WeightHistoryEntry> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Chuyển đổi chuỗi JSON thành danh sách đối tượng WeightHistoryEntry
     * @param value Chuỗi JSON
     * @return Danh sách WeightHistoryEntry
     */
    @TypeConverter
    public static List<UserEntity.WeightHistoryEntry> toWeightHistoryList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<UserEntity.WeightHistoryEntry>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Chuyển đổi Date thành Long timestamp
     * @param date Date object
     * @return Long timestamp hoặc null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Chuyển đổi Long timestamp thành Date
     * @param value Long timestamp
     * @return Date object hoặc null
     */
    @TypeConverter
    public static Date timestampToDate(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Chuyển đổi Double thành Float
     * @param value Double
     * @return Float
     */
    @TypeConverter
    public static Float fromDouble(Double value) {
        if (value == null) {
            return null;
        }
        return value.floatValue();
    }

    /**
     * Chuyển đổi Float thành Double
     * @param value Float
     * @return Double
     */
    @TypeConverter
    public static Double toDouble(Float value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    /**
     * Chuyển đổi danh sách số nguyên thành chuỗi JSON
     * @param list Danh sách Integer
     * @return Chuỗi JSON
     */
    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Chuyển đổi chuỗi JSON thành danh sách số nguyên
     * @param value Chuỗi JSON
     * @return Danh sách Integer
     */
    @TypeConverter
    public static List<Integer> toIntegerList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Chuyển đổi danh sách số thực thành chuỗi JSON
     * @param list Danh sách Double
     * @return Chuỗi JSON
     */
    @TypeConverter
    public static String fromDoubleList(List<Double> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Chuyển đổi chuỗi JSON thành danh sách số thực
     * @param value Chuỗi JSON
     * @return Danh sách Double
     */
    @TypeConverter
    public static List<Double> toDoubleList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Double>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}