package com.sfdex.goldfish.utils

import android.os.Build

/**
 *   Author: Holden.Wang
 *   Date:   2022-12月-07 周三, 15:03
 *   Desc:   00
 **/
object DeviceUtil {
    //设备物理型号
    fun getDeviceModel(): String {
        return Build.BRAND + "-" + Build.MODEL
    }

    //亮钻
    private var isLz: Boolean? = null

    fun isTablet(): Boolean {
        if (isLz == null) {
            isLz = "Android-rk3399-all" == getDeviceModel()
        }
        return isLz!!
    }

    fun isAndroid7() = Build.VERSION.SDK_INT == 25
    fun isAndroid11() = Build.VERSION.SDK_INT == 30
}