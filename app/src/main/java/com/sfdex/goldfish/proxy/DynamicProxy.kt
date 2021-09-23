package com.sfdex.goldfish.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

object DynamicProxy {
    fun <T> newInstance(
        classLoader: ClassLoader,
        interfaces: Array<Class<*>>,
        invocationHandler: InvocationHandler
    ):T {
        return Proxy.newProxyInstance(classLoader, interfaces, invocationHandler) as T
    }
}