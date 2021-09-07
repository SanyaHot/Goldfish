package com.sfdex.goldfish.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.sfdex.goldfish.R
import com.sfdex.goldfish.utils.getString
import com.sfdex.goldfish.utils.log


private const val TAG = "AccessibilityDelegate"

class AccessibilityDelegate(private val accessibilityService: AccessibilityService) {
    //记录Activity
    private var lastActivity: String = ""

    //WindowId
    private var lastWindowId: Int = -1

    var screenWidth = 0

    //相同Activity执行点击次数
    private var execTime = 0

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            //窗口内容变化
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> { //2048
                if (event.windowId == lastWindowId) {
                    if (execTime < 5) {
                        execTime++
                        findClick(event)
                    }
                }
            }

            //窗口状态变化
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {   //32
                TAG log "TYPE_WINDOW_STATE_CHANGED: ${event.className} windowId:${event.windowId}"
                event?.className ?: return
                val window = accessibilityService.getWindow(event.windowId)
                if (window?.isFocused != false) {
                    val currentActivity = event.className.toString()
                    if (lastActivity != currentActivity) {
                        this.lastActivity = event.className.toString()
                        this.lastWindowId = event.windowId
                        findClick(event)
                        execTime = 0
                    }
                    TAG log "currentActivity: $currentActivity "
                }

                //微信登录
                if ("com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI" == event.className) {
                    byText(getString(R.string.login), event)
                }
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                TAG log "TYPE_VIEW_CLICKED: $event"
            }

            else -> {
                TAG log "onAccessibilityEvent: event -> ${event.eventType} -- ${event.className}"
            }

        }
    }

    private fun findClick(event: AccessibilityEvent) {
        //静态广告(掌上生活)
        if (lastActivity == "com.cmbchina.ccd.pluto.cmbActivity.SplashActivity") {
            byId(
                "com.cmbchina.ccd.pluto.cmbActivity:id/img_cancel",
                event
            )
        } else {
            byText(getString(R.string.skip), event, true)
        }
    }

    //byId (an id like "com.cmbchina.ccd.pluto.cmbActivity:id/img_cancel"
    private fun byId(id: String, event: AccessibilityEvent) {
        val source = event.source ?: accessibilityService.rootInActiveWindow
        TAG log "findViewByViewId: $id"
        if (source == null) {
            TAG log "findViewByViewId: failure1"
            return
        }
        var foundView: AccessibilityNodeInfo? = null
        val node = source.findAccessibilityNodeInfosByViewId(id)
        if (node != null && node.size > 0) {
            TAG log "findViewByViewId: success"
        }
        foundView = node[0]
        foundView?.perFormClick()
    }

    /**
     * @param str   文字
     * @param event 事件
     * @param match
     */
    private fun byText(str: String, event: AccessibilityEvent, match: Boolean = false) {
        val source = accessibilityService.rootInActiveWindow
        val source1 = event.source
        if (source1 == null) {
            TAG log "404 source1: "
        }
        if (source == null) {
            TAG log "404 NOT FOUND"
            return
        }
        val list = source.findAccessibilityNodeInfosByText(str)
        var foundView: AccessibilityNodeInfo? = null
        if (list.isNotEmpty()) {
            TAG log "findViewByText success: "
            list.map { node ->
                TAG log "NodeInfo: $node"
                val content = node.text ?: node.contentDescription
                content?.toString()?.toLowerCase()?.let {
                    if (it == str || (match && it.length < 10 && it.startsWith(str))) {
                        if (str == getString(R.string.skip)) {
                            val rect = Rect()
                            node.getBoundsInScreen(rect)
                            //屏幕右边1/4
                            if (rect.left >= screenWidth * 0.75) {
                                foundView = node
                            }
                        } else foundView = node
                    }
                }

                //京东
                if (foundView == null && node.contentDescription == str) {
                    foundView = node
                }
            }
        } else {
            TAG log "find failure: "
        }
//        foundView?.perFormClick()
        foundView?.let {
            it.perFormClick()
        }
    }

    //多次调用
    var recursiveClick = 0

    //点击
    private fun AccessibilityNodeInfo.perFormClick() {
        var performResult = false
        if (!isClickable) {
            performResult = performGesture()
            TAG log "perFormGesture: gesture $performResult"
            return
        }
        performResult = performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.id)
        TAG log "perFormClick: $recursiveClick node:$this"
        TAG log "perFormClick: $performResult"
        if (!performResult) {
            if (recursiveClick < 3) {
                recursiveClick++
                parent?.perFormClick()
            } else {
                recursiveClick = 0
            }
        } else {
            recursiveClick = 0
        }
    }


    //可以通过手势解决isClickable为false时不能点击的情况(微博)
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun AccessibilityNodeInfo.performGesture(): Boolean {
        val rect = Rect()
        getBoundsInScreen(rect)
        val x = (rect.left + rect.right) / 2
        val y = (rect.top + rect.bottom) / 2
        val point = Point(x, y)
        TAG log "clickByNode: $point"
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(point.x.toFloat(), point.y.toFloat())
        builder.addStroke(StrokeDescription(path, 0L, 100L))
        val gesture = builder.build()
        return accessibilityService.dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                TAG log "dispatchGesture onCompleted: "
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                TAG log "dispatchGesture onCancelled: "
            }
        }, null)
    }

    //获取Window
    private fun AccessibilityService.getWindow(windowId: Int): AccessibilityWindowInfo? {
        windows?.forEach {
            TAG log "getWindow: ${it.javaClass.simpleName}"
            if (it.id == windowId) {
                return it
            }
        }
        return null
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