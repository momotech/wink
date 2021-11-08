package com.example.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模式判断：「或」关系
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface ModeJudgerOR {
    String liveMode();
    String connectMode();

    //    ILiveActivity.Mode modeTest() default ILiveActivity.Mode.NONE;
//    ModeJudger[] value() default new ModeJudger[]();
    int judgeSei() default Default.SEI;
}
