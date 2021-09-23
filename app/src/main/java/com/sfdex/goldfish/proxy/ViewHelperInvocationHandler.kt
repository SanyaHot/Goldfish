package com.sfdex.goldfish.proxy

import android.util.Log
import com.sfdex.goldfish.accessibility.IViewHelper
import com.sfdex.goldfish.accessibility.ViewHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

private const val TAG = "ViewHelperInvocationHandler"

class ViewHelperInvocationHandler(private val viewHelper: IViewHelper) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
//        Log.d(TAG, "invoke: ${method.name}")
//        args.mapIndexed { index, any ->
//            Log.d(TAG, "args[$index]: ${args[index]}")
//        }
        //java.lang.IllegalArgumentException: Wrong number of arguments; expected 3, got 1
        //Object ... args & Object[] args
        //kotlin的args: Array<out Any>与java的Object[] args不兼容
        //所以得传*args
        //return method.invoke(this.viewHelper, args)

        return method.invoke(this.viewHelper, *args)
    }
}