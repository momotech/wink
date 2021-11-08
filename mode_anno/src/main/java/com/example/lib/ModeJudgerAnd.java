package com.example.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模式判断：「与」关系
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModeJudgerAnd {
    String liveMode();
    String connectMode();
//    ILiveActivity.Mode modeTest() default ILiveActivity.Mode.NONE;
    ModeJudger[] value();
    int judgeSei() default Default.SEI;
}
