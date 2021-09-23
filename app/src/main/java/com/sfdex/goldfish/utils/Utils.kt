package com.sfdex.goldfish.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.sfdex.goldfish.broadcast.MyAdminManageReceiver
import com.sfdex.goldfish.R


object Utils {
    //锁定屏幕
    fun lock(context: Context) {
        val adminName = ComponentName(gContext, MyAdminManageReceiver::class.java)
        //获取设备管理器
        val mDPM = gContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        //检查设备管理器权限
        if (!mDPM.isAdminActive(adminName)) {
            setAdmin(context,adminName)
        } else {
            mDPM.lockNow()
        }
    }

    //设置设备管理器
    private fun setAdmin(context: Context,adminName: ComponentName) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
        (context as Activity).startActivityForResult(intent,889);
    }

}