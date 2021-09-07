package com.sfdex.goldfish.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.sfdex.goldfish.MyApplication
import com.sfdex.goldfish.utils.log
import com.sfdex.goldfish.utils.toast

private const val TAG = "MyAccessibilityService"

//无障碍服务
class MyAccessibilityService : AccessibilityService() {

    private var delegate: AccessibilityDelegate? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        delegate = AccessibilityDelegate(this)
        "华强已启动".toast()

        delegate?.screenWidth = MyApplication.getContext().resources.displayMetrics.widthPixels
        TAG log "onServiceConnected: width ${delegate?.screenWidth}"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        delegate?.onAccessibilityEvent(event)
    }

    override fun onInterrupt() {
        "Fuck Stub".toast()
    }

    //Dynamic update AccessibilityServiceInfo
    private fun initAccessibility() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            packageNames =
                arrayOf("com.ruanmei.ithome", "com.tencent.mm", "com.sfdex.glodfish")
            notificationTimeout = 100
            canRetrieveWindowContent
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    //声明这个属性可以获得读取viewId的能力
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }
}