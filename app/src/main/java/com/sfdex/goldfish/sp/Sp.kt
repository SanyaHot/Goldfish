package com.sfdex.goldfish.sp

import android.content.Context
import android.content.SharedPreferences
import com.sfdex.goldfish.MyApplication

/**
 *   Author: Holden.Wang
 *   Date:   2022-12月-09 周五, 15:09
 *   Desc:   00
 **/
object Sp {
    private var sp: SharedPreferences? = null
    private fun getSharedPreferences(): SharedPreferences {
        if (sp == null) {
            sp = MyApplication.getContext().getSharedPreferences("auto-apn", Context.MODE_PRIVATE)
        }
        return sp!!
    }

    fun putBoolean(key: String, value: Boolean) {
        getSharedPreferences().edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences().getBoolean(key, defaultValue)
    }

    fun putString(key: String, value: String) {
        getSharedPreferences().edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return getSharedPreferences().getString(key, defaultValue)!!
    }
}