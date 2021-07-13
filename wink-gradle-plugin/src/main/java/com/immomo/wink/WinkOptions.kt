package com.immomo.wink

import com.immomo.wink.util.WinkLog
import java.io.Serializable

open class WinkOptions(
    @JvmField
    var moduleWhitelist: Array<String>? = null,
    @JvmField
    var moduleBlacklist: Array<String>? = null,
    @JvmField
    var kotlinSyntheticsEnable: Boolean = false,
    @JvmField
    var logLevel: Int = 4
) : Serializable {

    fun copy(): WinkOptions {
        val obj = WinkOptions()
        obj.moduleBlacklist = this.moduleWhitelist
        obj.moduleBlacklist = this.moduleBlacklist
        obj.kotlinSyntheticsEnable = this.kotlinSyntheticsEnable
        obj.logLevel = this.logLevel
        return obj
    }
}