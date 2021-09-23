package com.sfdex.goldfish.accessibility

import com.sfdex.goldfish.utils.gContext
import java.io.InputStream
import java.util.*

//配置
object PropertiesManager {
    private val properties: Properties

    //读取配置文件
    init {
        val inputStream: InputStream = gContext.assets.open("config.properties")
        properties = Properties()
        properties.load(inputStream)
    }

    fun isContain(pkg: String): Boolean {
        return properties.containsKey(pkg)
    }

    fun getId(pkd: String): String {
        return properties.getProperty(pkd)
    }

}