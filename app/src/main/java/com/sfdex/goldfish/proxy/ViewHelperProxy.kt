package com.sfdex.goldfish.proxy

import android.util.Log
import com.sfdex.goldfish.accessibility.IViewHelper

object ViewHelperProxy {
    fun newInstance(viewHelper: IViewHelper): IViewHelper {
        val classLoader = viewHelper.javaClass.classLoader
        val interfaces = viewHelper.javaClass.interfaces
        val invocationHandler = ViewHelperInvocationHandler(viewHelper)
        return DynamicProxy.newInstance<Any>(
            classLoader,
            interfaces,
            invocationHandler
        ) as IViewHelper
    }
}