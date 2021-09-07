package com.sfdex.goldfish

import android.app.Application
import android.content.Context

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }

    companion object{
        private var mContext: Context?=null

        fun getContext():Context {
            return mContext!!
        }
    }
}