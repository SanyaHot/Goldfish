package com.sfdex.goldfish.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.sfdex.goldfish.MyApplication
import com.sfdex.goldfish.utils.log
import com.sfdex.goldfish.utils.toast

//无障碍服务
class MyAccessibilityService : AccessibilityService() {

    private var delegate: EventDispatcher? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        delegate = EventDispatcher(this)
        "华强已启动".toast()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        delegate?.onAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        "Fuck Stub".toast()
    }
}