package com.starkbank.cocoatouch.compability
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.uikit.UIResponder
import java.lang.reflect.Method


object BackPressedParser {

    fun getMethod(controller: UIResponder?): Method? {
        if (controller == null)
            return null

        val methods = controller.javaClass.declaredMethods
        for (method in methods) {
            if (isTheRightMethod(method)) {
                return method
            }
        }
        return null
    }

    private fun isTheRightMethod(method: Method): Boolean {
        if (method.isAnnotationPresent(IBAction::class.java)) {
            val action = method.getAnnotation(IBAction::class.java)!!
            return action.value == DefaultActions.onBackPressed
        }
        return false
    }
}