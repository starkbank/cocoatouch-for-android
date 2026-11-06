package com.starkbank.cocoatouch.compatibility
import com.starkbank.cocoatouch.annotation.IBAction
import com.starkbank.cocoatouch.annotation.IBActionParser
import com.starkbank.cocoatouch.uikit.UIResponder
import java.lang.reflect.Method
import com.starkbank.cocoatouch.annotation.InternalCocoaTouchApi


@InternalCocoaTouchApi
object BackPressedParser {

    fun getMethod(controller: UIResponder?): Method? {
        if (controller == null)
            return null

        return IBActionParser.actionMethods(controller.javaClass).firstOrNull { isTheRightMethod(it) }
    }

    private fun isTheRightMethod(method: Method): Boolean {
        val action = method.getAnnotation(IBAction::class.java)!!
        return action.value == DefaultActions.onBackPressed
    }
}
