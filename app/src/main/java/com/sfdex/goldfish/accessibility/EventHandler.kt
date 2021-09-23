package com.sfdex.goldfish.accessibility

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast
import com.sfdex.goldfish.R
import com.sfdex.goldfish.proxy.ViewHelperProxy
import com.sfdex.goldfish.utils.gContext
import com.sfdex.goldfish.utils.getString
import com.sfdex.goldfish.utils.log

private const val TAG = "SkipHandler"

class SkipHandler(
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
        val configured = PropertiesManager.isContain(event.packageName.toString())
        val node: AccessibilityNodeInfo? =
            if (configured) {
                proxy.findById(PropertiesManager.getId(event.packageName.toString()), event)
            } else {
                proxy.findByTxt(gContext.getString(R.string.skip), event, true)
            }

        node?.let {
            val result = proxy.click(it)
            Log.d(TAG, "skipResult: $result node:$it")
            return result
        }
//        if (node == null && configured) {
//            findIdFailed = true
//        }
        return false
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

                //微信登录
                if ("com.tencent.mm.plugin.webwx.ui.ExtDeviceWXLoginUI" == event.className) {
                    proxy.findByTxt(getString(R.string.login), event, false)?.let {
                        proxy.click(it)
                    }
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
                //TAG log "TYPE_VIEW_CLICKED: $event"
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