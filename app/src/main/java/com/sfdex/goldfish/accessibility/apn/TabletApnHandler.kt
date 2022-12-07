package com.sfdex.goldfish.accessibility.apn

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import com.sfdex.goldfish.accessibility.IViewHelper
import com.sfdex.goldfish.accessibility.MyAccessibilityService
import com.sfdex.goldfish.accessibility.ViewHelper
import com.sfdex.goldfish.proxy.ViewHelperProxy
import com.sfdex.goldfish.utils.log

private const val TAG = "TabletApnHandler"
var currentStep = TabletApnHandler.APN_SETTING

class TabletApnHandler(
    private val accessibilityService: MyAccessibilityService,
) {

    //记录Activity
    private var lastActivity: String = ""

    //WindowId
    private var lastWindowId: Int = -1

    //相同Activity执行点击次数
    private var execTime = 0

    //是否已经点击成功
    private var hasSkip = false

    //查找ViewId失败
    private var findIdFailed = false

    //ViewHelper代理
    private val proxy: IViewHelper =
        ViewHelperProxy.newInstance(ViewHelper(accessibilityService))

    private fun skip(event: AccessibilityEvent, count: Int = 0): Boolean {
        TAG log "packageName: ${event.packageName}"
        TAG log "className: ${event.className}"
        if (currentStep == FINISH) {
            return false
        }
        //proxy.traversal(event)
        val node: AccessibilityNodeInfo? =
            when (event.className) {
                in arrayOf(
                    "com.android.phone.MobileNetworkSettings",
                    "com.android.settings.Settings\$ApnSettingsActivity",
                    "com.android.settings.Settings\$ApnEditorActivity",
                    "android.app.AlertDialog",
                    "androidx.appcompat.app.AlertDialog",
                    "android.widget.FrameLayout"
                ) -> monitorEvent(event)

                /*"com.android.phone.MobileNetworkSettings" -> proxy.findByTxt("接入点名称", event)
                "com.android.settings.Settings\$ApnSettingsActivity" -> if(currentStep == APPLY) inputAndSave(event) else proxy.findByTxt("新建 APN", event)
                "com.android.settings.Settings\$ApnEditorActivity" -> inputAndSave(event)
                "android.app.AlertDialog" -> inputAndSave(event)
                "android.widget.FrameLayout" -> inputAndSave(event)*/

//                "com.android.providers.telephony" -> proxy.findByTxt("新建 APN", event, true)
//                "com.android.settings" -> proxy.findByTxt("接入点名称 (APN)", event, true)
//                else -> inputAndSave(event)
                else -> null
            }

        node?.let {
            SystemClock.sleep(100)
            val result = proxy.click(it)
            Log.d(TAG, "skipResult: $result node:$it")
            return result
        }
        return false
    }

    companion object {
        const val APN_SETTING = 1
        const val NEW_APN = 2
        const val CLICK_NAME = 3
        const val INPUT_NAME = 4
        const val SAVE_NAME = 5
        const val CLICK_APN = 6
        const val INPUT_APN = 7
        const val SAVE_APN = 8
        const val SAVE_ENTER = 9
        const val SAVE = 10
        const val APPLY = 11
        const val FINISH = 12
    }

//    var currentStep = APN_SETTING
    private fun monitorEvent(event: AccessibilityEvent): AccessibilityNodeInfo? {
        TAG log "before currentStep: $currentStep"
        var node: AccessibilityNodeInfo? =
            when (currentStep) {
                APN_SETTING -> proxy.findByTxt("接入点名称", event)
                NEW_APN -> proxy.findByTxt("新建 APN", event)
                CLICK_NAME -> proxy.findByTxt("名称", event, true)
                INPUT_NAME -> proxy.findById("android:id/edit", event)
                SAVE_NAME -> proxy.findById("android:id/button1", event)
                CLICK_APN -> proxy.findByTxt("APN", event, true)
                INPUT_APN -> proxy.findById("android:id/edit", event)
                SAVE_APN -> proxy.findById("android:id/button1", event)
                SAVE_ENTER -> proxy.findByTxt("更多选项", event)
                SAVE -> proxy.findByTxt("保存", event)
                APPLY -> proxy.findById("com.android.settings:id/apn_radiobutton", event, true)
                else -> null
            }
        if (node != null) {
            if (currentStep == INPUT_NAME) {
                val ret = input(node, "UCloud")
                Log.d(TAG, "inputName: $ret")
                if (!ret) {
                    return null
                }
                currentStep++
                node = proxy.findById("android:id/button1", event)
            }
            if (currentStep == INPUT_APN) {
                val ret = input(node!!, "shykd01s.shm2mapn")
                Log.d(TAG, "inputAPN: $ret")
                if (!ret) {
                    return null
                }
                currentStep++
                node = proxy.findById("android:id/button1", event)
            }
        }
        if (node != null) {
            TAG log "inputAndSave: ${event.className}"
            if (currentStep == APPLY) {
                //node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SELECT.id)
            }
            currentStep++
        }
        TAG log "after currentStep: $currentStep"
        return node
    }

    private fun input(node: AccessibilityNodeInfo, txt: String): Boolean {
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            txt
        )
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    //处理事件
    fun handleEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            //窗口状态变化
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {   //32
                TAG log "TYPE_WINDOW_STATE_CHANGED: ${event.className} windowId:${event.windowId}"
                event?.className ?: return
                val window = getWindow(event.windowId)
                if (window?.isFocused != false) {
                    val currentActivity = event.className.toString()
                    if (lastActivity != currentActivity) {
                        this.lastActivity = event.className.toString()
                        this.lastWindowId = event.windowId
                        //页面切换重置记录
                        findIdFailed = false
                        hasSkip = skip(event)
                        execTime = 0
                    }
                    TAG log "currentActivity: $currentActivity "
                }
            }

            //窗口内容变化
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> { //2048
                Log.d(TAG, "TYPE_WINDOW_CONTENT_CHANGED: ${event.className}")
                if (event.packageName.equals("com.ruanmei.ithome")) {
                    if (!hasSkip)
                        hasSkip = skip(event)
                    return
                }

                if (event.windowId == lastWindowId) {
                    while (execTime < 5 && !hasSkip) {
                        execTime++
                        hasSkip = skip(event)
                    }
                }
            }

            //1
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                TAG log "TYPE_VIEW_CLICKED: $event"
            }

            else -> {
                //TAG log "onAccessibilityEvent: event -> ${event.eventType} -- ${event.className}"
            }

        }
    }

    //获取Window
    private fun getWindow(windowId: Int): AccessibilityWindowInfo? {
        accessibilityService.windows?.forEach {
            TAG log "getWindow: ${it.javaClass.simpleName}"
            if (it.id == windowId) {
                return it
            }
        }
        return null
    }

    //使用手势实现点击
    fun performGesture(x: Float, y: Float) {
        proxy.gestureCoordinate(x, y)
    }

    /**
     * 获取ActivityInfo
     * @param componentName package+activity
     */
    private fun getActivityInfo(componentName: ComponentName): ActivityInfo? {
        return try {
            Log.i(TAG, "get appInfo:" + componentName.packageName)
            Toast.makeText(
                accessibilityService,
                componentName.packageName,
                Toast.LENGTH_SHORT
            ).show()
            accessibilityService.packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}