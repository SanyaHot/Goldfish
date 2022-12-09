package com.sfdex.goldfish.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.sfdex.goldfish.MyApplication
import com.sfdex.goldfish.accessibility.apn.APN_SETTING
import com.sfdex.goldfish.accessibility.apn.KeyFinished
import com.sfdex.goldfish.accessibility.apn.NET_MORE
import com.sfdex.goldfish.accessibility.apn.currentStep
import com.sfdex.goldfish.sp.Sp
import com.sfdex.goldfish.utils.DeviceUtil
import com.sfdex.goldfish.utils.ShellUtils
import com.sfdex.goldfish.utils.log
import com.sfdex.goldfish.utils.toast
import kotlin.concurrent.thread

//无障碍服务
class MyAccessibilityService : AccessibilityService() {

    private var delegate: EventDispatcher? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        delegate = EventDispatcher(this)
        "AutoApn已启动".toast()

        //if (Sp.getBoolean(KeyFinished)) return

        currentStep = APN_SETTING

        SystemClock.sleep(100)
        if (DeviceUtil.isTablet()) {
            ShellUtils.execCommand("killall com.android.settings", true)
            SystemClock.sleep(100)
            ShellUtils.execCommand("am start -n com.android.phone/.MobileNetworkSettings", true)
        } else if (DeviceUtil.isAndroid7()) {
            val intent = Intent().apply {
                component =
                    ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else if (DeviceUtil.isAndroid11()) {
            currentStep = NET_MORE
            val intent = Intent().apply {
                component =
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.network.telephony.MobileNetworkActivity"
                    )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //if (Sp.getBoolean(KeyFinished)) return

        delegate?.onAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        "Fuck Stub".toast()
    }
}