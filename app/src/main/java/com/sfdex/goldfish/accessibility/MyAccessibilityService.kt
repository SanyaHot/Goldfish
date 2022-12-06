package com.sfdex.goldfish.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.sfdex.goldfish.MyApplication
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
        "华强已启动".toast()

        SystemClock.sleep(100)
        ShellUtils.execCommand("killall com.android.settings", true)
        SystemClock.sleep(100)
        ShellUtils.execCommand("am start -n com.android.phone/.MobileNetworkSettings", true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        delegate?.onAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        "Fuck Stub".toast()
    }
}