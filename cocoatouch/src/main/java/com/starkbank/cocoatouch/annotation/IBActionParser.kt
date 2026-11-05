package com.starkbank.cocoatouch.annotation
import android.view.View
import android.widget.Button
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import java.lang.reflect.Method


object IBActionParser {

    fun parse(controller: UIResponder) {
        val methods: Array<Method> = controller.javaClass.declaredMethods
        for (method in methods) {
            parse(method, controller)
        }
    }

    private fun parse(method: Method, controller: UIResponder) {
        if (method.isAnnotationPresent(IBAction::class.java)) {
            val action = method.getAnnotation(IBAction::class.java)!!

            if (action.value == DefaultActions.onBackPressed)
                return

            val button: Any = controller.viewWithTag(action.value)
            if (Button::class.java.isAssignableFrom(button.javaClass)) {
                return parse(
                    button as Button,
                    method,
                    controller
                )
            }
            if (View::class.java.isAssignableFrom(button.javaClass)){
                return parse(
                    button as View,
                    method,
                    controller
                )
            }
        }
    }

    private fun parse(view: View, method: Method, controller: UIResponder) {
        view.setOnClickListener {
            try {
                val uiview = UIView()
                method.invoke(controller, uiview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun parse(button: Button, method: Method, controller: UIResponder) {
        button.setOnClickListener {
            try {
                val uibutton = UIButton()
                uibutton.widget = button
                method.invoke(controller, uibutton)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}