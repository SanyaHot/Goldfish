package com.sfdex.goldfish.accessibility

import android.view.accessibility.AccessibilityEvent
import com.sfdex.goldfish.accessibility.apn.ApnHandler

import com.sfdex.goldfish.utils.gDispatcher


private const val TAG = "EventDispatcher"

class EventDispatcher(private val accessibilityService: MyAccessibilityService) {

    var skipHandler: ApnHandler

    init {
        gDispatcher = this
        skipHandler = ApnHandler(accessibilityService)
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        skipHandler.handleEvent(event)
    }

    fun performGesture(x: Float, y: Float) {
        skipHandler.performGesture(x, y)
    }

}