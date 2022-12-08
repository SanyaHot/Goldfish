package com.sfdex.goldfish.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sfdex.goldfish.accessibility.MyAccessibilityService
import com.sfdex.goldfish.accessibility.apn.APN_SETTING
import com.sfdex.goldfish.accessibility.apn.NET_MORE
import com.sfdex.goldfish.accessibility.apn.currentStep
import com.sfdex.goldfish.utils.AccessibilityServiceUtils
import com.sfdex.goldfish.utils.toast
import com.sfdex.goldfish.viewmodel.MainViewModel
import com.sfdex.goldfish.window.FloatingWindow
import com.sfdex.goldfish.ui.theme.GoldfishTheme
import com.sfdex.goldfish.utils.DeviceUtil
import com.sfdex.goldfish.utils.ShellUtils

//主界面
class MainActivity : ComponentActivity() {

    private var floatingWindow: FloatingWindow? = null

    private lateinit var mViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val model: MainViewModel by viewModels()
        mViewModel = model

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                GoldfishTheme { Layout() }
            }
        }
        checkService()
    }

    @Composable
    fun Layout(viewModel: MainViewModel = viewModel()) {
//        val isEnable: Boolean by mViewModel.isEnable.observeAsState(false)
        val isEnable: Boolean by viewModel.isEnable.observeAsState(false)

        BoxWithConstraints {
            val constrains = if (minWidth < 600.dp) {
                decoupledConstrains(16.dp)  // Portrait constraints
            } else {
                decoupledConstrains(32.dp)  // Landscape constraints
            }

            ConstraintLayout(
                constrains, modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Button(onClick = { showFloatingWindow() }, modifier = Modifier.layoutId("button")) {
                    Text(text = "开启悬浮窗")
                }
                Button(onClick = { setApn() }, modifier = Modifier.layoutId("apn")) {
                    Text(text = "重设APN")
                }
                Text(
                    text = "无障碍服务${if (isEnable) "正常" else "未开启"}", /*modifier = Modifier.fillMaxWidth(),*/
                    textAlign = TextAlign.Center,
                    modifier = Modifier.layoutId("text")
                )
            }
        }
    }

    /**
     * Decoupled API
     * 在上面的 ConstraintLayout 示例中，约束条件是在应用它们的可组合项中使用修饰符以内嵌方式指定的。
     * 不过，在某些情况下，最好将约束条件与应用它们的布局分离开来。
     * 例如，您可能会希望根据屏幕配置来更改约束条件，或在两个约束条件集之间添加动画效果
     */
    private fun decoupledConstrains(margin: Dp): ConstraintSet {
        return ConstraintSet {
            val button = createRefFor("button")
            val text = createRefFor("text")

            constrain(button) {
                bottom.linkTo(text.top, margin)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            constrain(text) {
//                top.linkTo(parent.top)
//                //如果不设置父ConstraintLayout的modifier,下面的代码不会生效
//                bottom.linkTo(parent.bottom)
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)

                //以上代码等同于下
                centerTo(parent)
            }
        }
    }

    private fun showFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ), 100
            )
        } else {
//                startService(Intent(this, FloatingWindowService::class.java))
            getFloatingWindow().show()
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = AccessibilityServiceUtils.isAccessibilityServiceEnabled(
            this,
            MyAccessibilityService::class.java
        )
        mViewModel.setValue(enabled)
//        tips.text = "无障碍服务${if (enabled) "正常" else "未启动"}"
    }

    //检查无障碍权限
    private fun checkService() {
        if (!AccessibilityServiceUtils.isAccessibilityServiceEnabled(
                this,
                MyAccessibilityService::class.java
            )
        ) {
            if (DeviceUtil.isTablet()) {
                ShellUtils.execCommand(
                    "settings put secure enabled_accessibility_services com.sfdex.goldfish/.accessibility.MyAccessibilityService",
                    true
                )
            } else {
                AccessibilityServiceUtils.goToAccessibilitySetting(this)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Settings.canDrawOverlays(this)) {
//                startService(Intent(this, FloatingWindowService::class.java))
                floatingWindow?.show()
            } else {
                "未开启悬浮窗权限".toast()
            }
        }
    }

    private fun getFloatingWindow(): FloatingWindow {
        if (floatingWindow == null) {
            floatingWindow = FloatingWindow(this) {
                floatingWindow = null
            }
        }
        return floatingWindow!!
    }

    private fun setApn() {
        currentStep = APN_SETTING
        if (DeviceUtil.isTablet()) {
            ShellUtils.execCommand("killall com.android.settings", true)
            SystemClock.sleep(100)
            ShellUtils.execCommand("am start -n com.android.phone/.MobileNetworkSettings", true)
        } else if (DeviceUtil.isAndroid7()) {
            val intent = Intent().apply {
                component =
                    ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else if (DeviceUtil.isAndroid11()) {
            currentStep = NET_MORE
            val intent = Intent().apply {
                component =
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.network.telephony.MobileNetworkActivity"
                    )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
}