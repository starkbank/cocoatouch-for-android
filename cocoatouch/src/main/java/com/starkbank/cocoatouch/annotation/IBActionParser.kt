package com.starkbank.cocoatouch.annotation
import android.view.View
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap


object IBActionParser {

    private val methodsByType = ConcurrentHashMap<Class<*>, List<Method>>()

    fun parse(controller: UIResponder) {
        for (method in actionMethods(controller.javaClass)) {
            parse(method, controller)
        }
    }

    internal fun actionMethods(type: Class<*>): List<Method> {
        return methodsByType.computeIfAbsent(type) { walk(it) }
    }

    // Walks up to UIResponder so a base controller's actions are bound; a subclass method hides the parent's
    // one with the same name and parameters. The result is constant per class, so it is built once
    private fun walk(type: Class<*>): List<Method> {
        val methodsBySignature = LinkedHashMap<Pair<String, List<Class<*>>>, Method>()
        var current: Class<*>? = type
        while (current != null && current != UIResponder::class.java) {
            for (method in current.declaredMethods) {
                if (!method.isAnnotationPresent(IBAction::class.java)) continue
                method.isAccessible = true
                methodsBySignature.putIfAbsent(Pair(method.name, method.parameterTypes.toList()), method)
            }
            current = current.superclass
        }
        return methodsBySignature.values.toList()
    }

    private fun parse(method: Method, controller: UIResponder) {
        val action = method.getAnnotation(IBAction::class.java)!!

        if (action.value == DefaultActions.onBackPressed)
            return

        val view: View = controller.viewWithTag(action.value)
        view.setOnClickListener {
            try {
                method.invoke(controller, sender(view, method))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sender(view: View, method: Method): UIResponder {
        val sender = senderOf(method.parameterTypes[0])
        sender.widget = view
        return sender
    }

    private fun senderOf(type: Class<*>): UIResponder {
        if (UIResponder::class.java.isAssignableFrom(type))
            return type.newInstance() as UIResponder
        return UIView()
    }
}
