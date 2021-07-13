package com.immomo.wink.utils;


/**
 * Created by pengwei on 2016/11/2.
 */
public class LogUtil {

    public static void d(String info) {

        System.out.println(info);
    }

    public static void d(String info, Object...args) {

        System.out.println(String.format(info, args));
    }

    public static void d(Object info) {
        System.out.println(info);
    }

}
