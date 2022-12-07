package com.sfdex.goldfish.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.sfdex.goldfish.MyApplication
import com.sfdex.goldfish.R
import com.sfdex.goldfish.utils.getString
import com.sfdex.goldfish.utils.log

private const val TAG = "ViewHelper"

class ViewHelper(private val accessibilityService: MyAccessibilityService) : IViewHelper {

    //屏幕宽度
    private var screenWidth = 0

    init {
        screenWidth = MyApplication.getContext().resources.displayMetrics.widthPixels
    }

    override fun findById(
        id: String,
        event: AccessibilityEvent,
        last: Boolean
    ): AccessibilityNodeInfo? {
        Log.d(TAG, "findById: $id")
        val source = event.source ?: accessibilityService.rootInActiveWindow
        source ?: return null
        var foundView: AccessibilityNodeInfo? = null
        val node = source.findAccessibilityNodeInfosByViewId(id)
        if (node != null && node.size > 0) {
            foundView = if (last) {
                node[node.size - 1]
            } else {
                node[0]
            }
            TAG log "findById success"
        } else {
            TAG log "findById failure"
        }
        return foundView
    }

    override fun findByTxt(
        txt: String,
        event: AccessibilityEvent,
        regex: Boolean
    ): AccessibilityNodeInfo? {
        Log.d(TAG, "findByTxt: $txt")
        var foundView: AccessibilityNodeInfo? = getNodeFromTree(event, txt)
//        var foundView: AccessibilityNodeInfo? = null
        if (foundView == null) {
            val source = accessibilityService.rootInActiveWindow
            val source1 = event.source

            if (source == null) {
                TAG log "SOURCE NULL"
                return null
            }
            var list = source.findAccessibilityNodeInfosByText(txt)
            if (list.isEmpty() && source1 != null) {
                list = source1.findAccessibilityNodeInfosByText(txt)
            }
            if (list.isEmpty()) {
                TAG log "FIND FAILURE"
                return null
            }
            list.map { node ->
                TAG log "Map NodeInfo: $node"
                val content = node.text ?: node.contentDescription
                if (content?.isNotBlank() == true) {
                    TAG log "content: $content"
                }
                content?.toString()?.let {
                    if (it.contains(txt)) {
                        val rect = Rect()
                        node.getBoundsInScreen(rect)
                        foundView = node
                        TAG log "FoundView $foundView"
                        return foundView
                    }
                }
            }
        }

        return foundView
    }

    override fun click(node: AccessibilityNodeInfo?): Boolean {
        node ?: return false
        val result = node.perFormClick()
//        Log.d(TAG, "click: $node")
//        Log.d(TAG, "clickResult: $result")
        return result
    }

    override fun click(x: Float, y: Float): Boolean {
        return gestureCoordinate(x, y)
    }

    //递归点击次数
    var recursiveClick = 0

    //点击
    private fun AccessibilityNodeInfo.perFormClick(): Boolean {
        val performResult: Boolean
        if (!isClickable) {
            performResult = gesture(this)
            TAG log "perFormGesture: gesture $performResult"
            return performResult
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
        return performResult
    }

    override fun gesture(node: AccessibilityNodeInfo): Boolean {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val x = (rect.left + rect.right) / 2F
        val y = (rect.top + rect.bottom) / 2F
        return gestureCoordinate(x, y)
    }

    var retryCount = 0
    var lastX = 0F
    var lastY = 0F
    override fun gestureCoordinate(x: Float, y: Float): Boolean {
        val builder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x, y)
        builder.addStroke(GestureDescription.StrokeDescription(path, 0L, 100L))
        val gesture = builder.build()
        return accessibilityService.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    TAG log "dispatchGesture onCompleted: "
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    TAG log "dispatchGesture onCancelled: "
                    if (lastX == 0F && lastY == 0F) {
                        lastX = x
                        lastY = y
                    }
                    if (lastX == x && lastY == y) {
                        retryCount++
                        if (retryCount < 3) {
                            SystemClock.sleep(30)
                            gestureCoordinate(x, y)
                        } else {
                            retryCount = 0
                        }
                    }else{
                        retryCount = 0
                    }
                    lastX = x
                    lastY = y
                }
            },
            null
        )
    }

    //遍历View树
    override fun traversal(event: AccessibilityEvent) {
        val nodeInfo: AccessibilityNodeInfo? =
            event.source ?: accessibilityService.rootInActiveWindow
        nodeInfo?.let {
            doTraversal(0, it)
        }
    }

    //递归遍历
    private fun doTraversal(depth: Int, nodeInfo: AccessibilityNodeInfo) {
        printNode(depth, nodeInfo)
        val childCount = nodeInfo.childCount
        for (i in 0 until childCount) {
            val child = nodeInfo.getChild(i)
            if (child != null) {
                TAG log "traversal: index $i"
                val newDepth = depth + 1
                doTraversal(newDepth, child)
            }
        }
    }

    //遍历View树
    override fun getNodeFromTree(event: AccessibilityEvent, txt: String): AccessibilityNodeInfo? {
        val nodeInfo: AccessibilityNodeInfo? =
            event.source ?: accessibilityService.rootInActiveWindow
        nodeInfo?.let {
            return getNodeFromTreeInternal(0, it, txt)
        }
        return null
    }

    //递归遍历
    private fun getNodeFromTreeInternal(
        depth: Int,
        nodeInfo: AccessibilityNodeInfo,
        txt: String
    ): AccessibilityNodeInfo? {
        val childCount = nodeInfo.childCount
        for (i in 0 until childCount) {
            val child = nodeInfo.getChild(i)
            if (child != null) {
                TAG log "traversal: index $i"
                val newDepth = depth + 1
                return getNodeFromTreeByTraversal(newDepth, child, txt)
            }
        }
        return null
    }

    //递归遍历
    private fun getNodeFromTreeByTraversal(
        depth: Int,
        nodeInfo: AccessibilityNodeInfo,
        txt: String
    ): AccessibilityNodeInfo? {
        var content = nodeInfo.text
        if (content.isNullOrBlank()) {
            content = nodeInfo.contentDescription
        }
        if (content?.contains(txt) == true) {
            printNode(depth, nodeInfo)
            return nodeInfo
        }
        val childCount = nodeInfo.childCount
        for (i in 0 until childCount) {
            val child = nodeInfo.getChild(i)
            if (child != null) {
                TAG log "traversal: index $i"
                val newDepth = depth + 1
                return getNodeFromTreeByTraversal(newDepth, child, txt)
            }
        }
        return null
    }

    //打印结点信息
    private fun printNode(depth: Int, node: AccessibilityNodeInfo) {
        TAG.run {
            log("------------------------------")
//            log("traversal: depth $depth")
            log("traversal: text ${node.text}")
            log("traversal: desc ${node.contentDescription}")
//            log("traversal: class ${node.className}")
//            log("traversal: clickable ${node.isClickable}")
//            log("traversal: viewId ${node.viewIdResourceName}")
            val rect = Rect()
            node.getBoundsInScreen(rect)
            log("traversal: bounds $rect")
        }
    }
}