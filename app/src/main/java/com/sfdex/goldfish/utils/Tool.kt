package com.sfdex.goldfish.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.sfdex.goldfish.MyApplication
import com.sfdex.goldfish.accessibility.EventDispatcher

val gContext: Context
    get() = MyApplication.getContext()

fun getString(@StringRes id: Int) = gContext.getString(id)

fun String.toast() {
    Toast.makeText(MyApplication.getContext(), this, Toast.LENGTH_SHORT).show()
}

fun String.logd(msg: String) {
    //this = TAG
    Log.d(this, "$msg")
}

infix fun String.log(msg: Any) {
    Log.d(this, "$msg")
}

var gDispatcher: EventDispatcher? = null
fun performClick(coordinate: Array<Float>) {
    "点击坐标(${coordinate[0]},${coordinate[1]})".toast()
    if (coordinate.size < 2) {
        "坐标异常".toast()
    }
    gDispatcher ?: "服务异常".toast()
    if (gDispatcher != null) {
        gDispatcher!!.performGesture(coordinate[0], coordinate[1])
    }
}