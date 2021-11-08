package com.example.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模式判断：单组条件
 * 正数判断 == 某模式，负数表示 !=
 */
//@Repeatable(ModeJudgerAnd.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ModeJudger {
    int rType() default com.example.lib.Default.INT;
    int pkType() default com.example.lib.Default.INT;
    int seiType() default com.example.lib.Default.INT;
    int subMode() default com.example.lib.Default.INT;
    int linkMode() default com.example.lib.Default.INT;
    int arenaType() default com.example.lib.Default.INT;
    int fullTimeMode() default com.example.lib.Default.INT;
    String[] src() default {};
    String[] srcExclude() default {};
    int judgeSei() default Default.SEI;
    String liveMode() default ""; // 对应 ILiveActivity.Mode
}
