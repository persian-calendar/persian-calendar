package com.byagowi.persiancalendar.view.deviceinfo;

import android.os.Build;

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
public class DeviceInfoUtils {

    public static String getManuFacturer() {
        return Build.MANUFACTURER;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getSystemModel() {
        return Build.MODEL;
    }

    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    public static String getSerialNo() {
        return Build.SERIAL;
    }

    public static String getBuildHost() {
        return Build.HOST;
    }

    public static String getBuildUser() {
        return Build.USER;
    }

    public static String getBuildBoard() {
        return Build.BOARD;
    }

    public static String getBuildId() {
        return Build.ID;
    }

    public static String getBootloader() {
        return Build.BOOTLOADER;
    }

}
