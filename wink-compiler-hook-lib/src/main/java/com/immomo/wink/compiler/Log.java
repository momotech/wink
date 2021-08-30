package com.immomo.wink.compiler;

public class Log {

    public static void i(String string) {
        System.out.println("\u001B[33m" + string + "\u001B[0m");
    }
}
