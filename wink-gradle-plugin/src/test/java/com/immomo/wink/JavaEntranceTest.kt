package com.immomo.wink

import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlin.collections.HashMap

class JavaEntranceTest {
    @Test
    fun test() {
        JavaEntrance.main(arrayOf("../"))
    }

    @Test
    fun test1() {
        var map = HashMap<String, String>()
        map["AROUTER_MODULE_NAME"] = "wink-demo-app"
        var result1 = encodeList(map)
        println("========>>> 111 $result1")
        try {
            val map: HashMap<String, String> = HashMap()
            map["AROUTER_MODULE_NAME"] = "wink-demo-app"
            val s = encodeList1(map)
            println("========>>> 222 $s")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun encodeList(options: Map<String, String>): String {
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

    private fun encodeList1(options: Map<String, String>): String? {
        val os = ByteArrayOutputStream()
        val stream = ObjectOutputStream(os)
        stream.writeInt(options.size)
        for ((key, value) in options) {
            stream.writeUTF(key)
            stream.writeUTF(value)
        }
        stream.flush()
        return Base64.getEncoder().encodeToString(os.toByteArray())
    }
}