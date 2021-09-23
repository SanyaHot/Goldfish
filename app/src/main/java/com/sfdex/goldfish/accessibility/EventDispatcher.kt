package com.sfdex.goldfish.accessibility

import android.view.accessibility.AccessibilityEvent

import com.sfdex.goldfish.utils.gDispatcher


private const val TAG = "EventDispatcher"

class EventDispatcher(private val accessibilityService: MyAccessibilityService) {

    var skipHandler: SkipHandler

    init {
        gDispatcher = this
        skipHandler = SkipHandler(accessibilityService)
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        skipHandler.handleEvent(event)
    }

    fun performGesture(x: Float, y: Float) {
        skipHandler.performGesture(x, y)
    }

}