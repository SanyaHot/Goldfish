package com.sfdex.goldfish.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast
import com.sfdex.goldfish.R
import com.sfdex.goldfish.broadcast.MyAdminManageReceiver


object Utils {
    //锁定屏幕
    fun lock(context: Context) {
        val adminName = ComponentName(gContext, MyAdminManageReceiver::class.java)
        //获取设备管理器
        val mDPM = gContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        //检查设备管理器权限
        if (!mDPM.isAdminActive(adminName)) {
            setAdmin(context, adminName)
        } else {
//            mDPM.setNetworkLoggingEnabled(adminName, true)
//            setProfileOwner(context, mDPM)

            mDPM.lockNow()
        }
    }

    //设置设备管理器
    private fun setAdmin(context: Context, adminName: ComponentName) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.app_name);
        (context as Activity).startActivityForResult(intent, 889);
    }

    private fun setProfileOwner(context: Context, mDPM: DevicePolicyManager?) {
        val mAdminCN = ComponentName(context, MyAdminManageReceiver::class.java)
        if (mDPM != null) {
            try {
                if (mDPM.isAdminActive(mAdminCN)) {
                    if (mDPM.isProfileOwnerApp(context.packageName)) {
                        Toast.makeText(context, "配置管理已经激活", Toast.LENGTH_SHORT).show()
                    } else {
                        mDPM.setProfileEnabled(mAdminCN)
                        //mDPM.setProfileOwner(mAdminCN, DEVICE_POLICY_TEST, UserHandle.myUserId())
                    }
                } else {
                    Toast.makeText(context, "请先激活设备管理器", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

//    fun clearProfileOwner() {
//        if (mDPM != null) {
//            try {
//                if (mDPM.isProfileOwnerApp(getPackageName())) {
//                    mDPM.clearProfileOwner(mAdminCN)
//                } else {
//                    Toast.makeText(this, "配置管理已经清除", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: SecurityException) {
//                e.printStackTrace()
//            } catch (e: IllegalArgumentException) {
//                e.printStackTrace()
//            }
//        }
//    }


}