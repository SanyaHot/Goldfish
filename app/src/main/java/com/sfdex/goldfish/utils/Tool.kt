package com.sfdex.goldfish.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import com.sfdex.goldfish.MyApplication

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

infix fun String.log(msg: String) {
    Log.d(this, "$msg")
}