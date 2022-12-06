package com.sfdex.goldfish.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 无障碍服务View工具
 */
interface IViewHelper {
    /**
     * 通过id去查找view
     * @param id 控件的id
     */
    fun findById(id: String, event: AccessibilityEvent, last:Boolean = false): AccessibilityNodeInfo?

    /**
     * 通过文字内容去查找view
     * @param txt 查找的内容
     * @param regex 是否完全匹配
     */
    fun findByTxt(
        txt: String,
        event: AccessibilityEvent,
        regex: Boolean = false
    ): AccessibilityNodeInfo?

    //点击
    fun click(node: AccessibilityNodeInfo?): Boolean

    //点击坐标
    fun click(x: Float, y: Float): Boolean

    //使用手势实现点击
    fun gesture(node: AccessibilityNodeInfo): Boolean
    fun gestureCoordinate(x: Float, y: Float): Boolean

    //遍历View树
    fun traversal(event: AccessibilityEvent)
    fun getNodeFromTree(event: AccessibilityEvent,txt:String):AccessibilityNodeInfo?
}