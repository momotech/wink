package com.immomo.wink.util

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.*

class KaptEncodeUtils {
    companion object {
        @JvmStatic
        fun encodeList(options: Map<String, String>): String {
            val os = ByteArrayOutputStream()
            val oos = ObjectOutputStream(os)

            oos.writeInt(options.size)
            for ((key, value) in options.entries) {
                oos.writeUTF(key)
                oos.writeUTF(value)
            }

            oos.flush()
            val base64Str = Base64.getEncoder().encodeToString(os.toByteArray())
            println("base64Str : $base64Str")
            return base64Str
        }
    }
}