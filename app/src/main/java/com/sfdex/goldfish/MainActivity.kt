package com.sfdex.goldfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.sfdex.goldfish.service.MyAccessibilityService
import com.sfdex.goldfish.utils.AccessibilityServiceUtils

class MainActivity : AppCompatActivity() {
    lateinit var tips: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tips = findViewById(R.id.tips)
        checkService()
    }

    override fun onResume() {
        super.onResume()
        val enabled = AccessibilityServiceUtils.isAccessibilityServiceEnabled(
            this,
            MyAccessibilityService::class.java
        )
        tips.text = "无障碍服务${if (enabled) "正常" else "未启动"}"
    }

    //检查无障碍权限
    private fun checkService() {
        if (!AccessibilityServiceUtils.isAccessibilityServiceEnabled(
                this,
                MyAccessibilityService::class.java
            )
        ) {
            AccessibilityServiceUtils.goToAccessibilitySetting(this)
        }
    }
}