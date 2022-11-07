package com.starkbank.cocoatouch.annotation
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIButton
import android.widget.Button
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
            val button: Any = controller.viewWithTag(action.value)

            if (Button::class.java.isAssignableFrom(button.javaClass)) {
                return parse(button as Button, method, controller)
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