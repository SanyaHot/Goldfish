package com.sfdex.goldfish.window

import android.app.Activity
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.sfdex.goldfish.R
import com.sfdex.goldfish.utils.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors

private const val TAG = "FloatingWindow"

class FloatingWindow(val context: Context, val block: (Int) -> Unit) {
    //协程作用域
    private var scope: CoroutineScope? = null

    //悬浮窗坐标
    private var lastCoordinate = arrayOf(300, 300)

    //进入位置选择
    private var selectClick = false

    //时间显示
    private var showTime = false

    //选取位置坐标
    private var selectCoordinate = arrayOf(0F, 0F)


    private lateinit var mWindowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var view: View

    //悬浮窗显示状态
    private var floatingShowing = false

    //最小化
    private var isMinimum = false

    fun show() {
        if (floatingShowing) {
            return
        }
        floatingShowing = true
        mWindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        view = LayoutInflater.from(context).inflate(R.layout.layout_floating_window, null)

        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.gravity = Gravity.LEFT or Gravity.TOP

        layoutParams.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL

        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.x = lastCoordinate[0]
        layoutParams.y = lastCoordinate[1]
        mWindowManager.addView(view, layoutParams)

        initListener()
    }

    private fun initListener() {
        val controlPanel = view.findViewById<ConstraintLayout>(R.id.cy_control)
        val screenshot = view.findViewById<Button>(R.id.screenshot)
        val click = view.findViewById<Button>(R.id.click)
        val lock = view.findViewById<Button>(R.id.lock)
        val shutdown = view.findViewById<Button>(R.id.shutdown)
        val time = view.findViewById<Button>(R.id.time)
        val close = view.findViewById<ImageView>(R.id.close)
        val mini = view.findViewById<ImageView>(R.id.mini)
        val icon = view.findViewById<ConstraintLayout>(R.id.icon)

        val realTime = view.findViewById<TextView>(R.id.real_time)

        val finish = view.findViewById<Button>(R.id.finish)

        view.setOnTouchListener(MyTouchListener())

        screenshot.setOnClickListener {
            controlPanel.isVisible = false
            RootUtils.screenshot {
                (context as Activity).runOnUiThread {
                    controlPanel.isVisible = true
                }
            }
        }

        click.setOnClickListener {
            selectClick = true
            updateLayout()
        }

        time.setOnClickListener {
            showTime = true
            updateLayout()
            showTime(realTime)
        }

        realTime.setOnClickListener {
            showTime = false
            updateLayout()
            scope?.cancel()
            scope = null
        }

        realTime.setOnTouchListener(MyTouchListener())

        lock.setOnClickListener {
            Utils.lock(context)
        }

        shutdown.setOnClickListener {
            "shutdown".toast()
            RootUtils.shutdown(context)
        }

        close.setOnClickListener {
            dismiss()
        }

        mini.setOnClickListener {
            isMinimum = true
            updateLayout()
        }

        icon.setOnClickListener {
            isMinimum = false
            updateLayout()
        }

        icon.setOnTouchListener(MyTouchListener())

        finish.setOnClickListener {
            selectClick = false
            updateLayout()
            it.postDelayed({
                performClick(selectCoordinate)
            }, 2000)
        }
    }

//    private val executor = Executors.newSingleThreadExecutor()

    //显示时间，500ms更新一次
    private fun showTime(realTime: TextView) {
        scope = MainScope()
        scope?.launch {
            while (showTime) {
                realTime.text =
                    "${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())}"
                delay(100)
            }
        }

//        executor.execute {
//            while (showTime) {
//                rouiT { realTime.text = "time" }
//                Thread.sleep(100)
//            }
//        }

    }

    //自定义触摸事件
    inner class MyTouchListener : View.OnTouchListener {
        var x = 0F
        var y = 0F
        var startX = 0F
        var startY = 0F
        var movedX = 0
        var movedY = 0
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX
                    y = event.rawY
                    startX = x
                    startY = y
                }

                MotionEvent.ACTION_MOVE -> {
                    movedX = (event.rawX - x).toInt()
                    movedY = (event.rawY - y).toInt()
                    x = event.rawX
                    y = event.rawY

                    //拦截选择位置时的滑动事件
                    if (selectClick) return false

                    layoutParams.x = layoutParams.x + movedX
                    layoutParams.y = layoutParams.y + movedY

                    lastCoordinate = arrayOf(layoutParams.x, layoutParams.y)
                    mWindowManager.updateViewLayout(view, layoutParams)
                }

                MotionEvent.ACTION_UP -> {
                    if (x - startX == 0F && y - startY == 0F) {
                        if (selectClick) {
                            selectCoordinate = arrayOf(event.rawX, event.rawY)
                            "x=${selectCoordinate[0]},y=${selectCoordinate[1]}".toast()
                        }
                        //响应点击事件
                        if (showTime || isMinimum) {
                            v.performClick()
                        }
                    }
                }
            }
            /**
             * return   是否拦截事件
             * true     拦截:限制事件的进一步分发。如果不手动调用view.performClick()，那设置了View.OnClickListener也没用
             * false    不拦截:不用调view.performClick()
             */
            return true
        }

    }

    //更新悬浮窗布局
    private fun updateLayout() {
        layoutParams.width =
            if (selectClick) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT

        layoutParams.height =
            if (selectClick) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.x = if (selectClick) 0 else lastCoordinate[0]
        layoutParams.y = if (selectClick) 0 else lastCoordinate[1]

        view.findViewById<View>(R.id.cy_control).apply {
            isVisible = !(selectClick || showTime || isMinimum)
        }
        view.findViewById<View>(R.id.finish).isVisible = selectClick
        view.findViewById<View>(R.id.real_time).isVisible = showTime
        view.findViewById<View>(R.id.icon).isVisible = isMinimum

        view.apply {
            setBackgroundColor(if (selectClick) Color.parseColor("#33000000") else Color.TRANSPARENT)
            if (showTime) setBackgroundResource(R.drawable.shape_real_time)
        }

        mWindowManager.updateViewLayout(view, layoutParams)
    }

    //关闭悬浮窗
    private fun dismiss() {
        mWindowManager.removeView(view)
        block.invoke(0)
    }
}