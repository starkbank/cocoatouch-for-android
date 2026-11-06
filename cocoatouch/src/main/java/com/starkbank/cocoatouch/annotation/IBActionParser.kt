package com.starkbank.cocoatouch.annotation
import android.view.View
import com.starkbank.cocoatouch.compatibility.DefaultActions
import com.starkbank.cocoatouch.uikit.UIButton
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
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

    // Walks up to UIResponder so a base controller's actions are bound. One view id has one handler: the subclass
    // entry wins, otherwise the base class's listener would be set last and replace it.
    // The result is constant per class, so it is built once
    private fun walk(type: Class<*>): List<Method> {
        val methodsByViewId = LinkedHashMap<Int, Method>()
        var current: Class<*>? = type
        while (current != null && current != UIResponder::class.java) {
            for (method in current.declaredMethods) {
                if (!method.isAnnotationPresent(IBAction::class.java)) continue
                method.isAccessible = true
                methodsByViewId.putIfAbsent(method.getAnnotation(IBAction::class.java)!!.value, method)
            }
            current = current.superclass
        }
        return methodsByViewId.values.toList()
    }

    private fun parse(method: Method, controller: UIResponder) {
        val action = method.getAnnotation(IBAction::class.java)!!

        val type = senderType(method, controller)
        if (action.value == DefaultActions.onBackPressed) {
            checkBackSender(type, method, controller)
            return
        }
        checkClickSender(type, method, controller)
        val view = viewOf(action.value, method, controller)
        view.setOnClickListener {
            try {
                method.invoke(controller, sender(view, method))
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
    }

    private fun senderType(method: Method, controller: UIResponder): Class<*> {
        if (method.parameterTypes.size == 1) return method.parameterTypes[0]
        throw IllegalStateException(
            "@IBAction ${method.name} in ${controller.javaClass.simpleName} must declare exactly one sender parameter"
        )
    }

    // dispatchBackPressed always sends a UIButton, so the parameter only has to receive one
    private fun checkBackSender(type: Class<*>, method: Method, controller: UIResponder) {
        if (type.isAssignableFrom(UIButton::class.java)) return
        throw IllegalStateException(
            "@IBAction ${method.name} in ${controller.javaClass.simpleName}: " +
                "back handler sender type ${type.simpleName} cannot receive a UIButton"
        )
    }

    private fun checkClickSender(type: Class<*>, method: Method, controller: UIResponder) {
        if (!UIResponder::class.java.isAssignableFrom(type)) {
            if (type.isAssignableFrom(UIView::class.java)) return
            throw IllegalStateException(
                "@IBAction ${method.name} in ${controller.javaClass.simpleName}: " +
                    "sender type ${type.simpleName} cannot receive a UIView"
            )
        }
        if (!Modifier.isAbstract(type.modifiers) && type.constructors.any { it.parameterTypes.isEmpty() }) return
        throw IllegalStateException(
            "@IBAction ${method.name} in ${controller.javaClass.simpleName}: " +
                "sender type ${type.simpleName} has no public no-arg constructor"
        )
    }

    private fun viewOf(id: Int, method: Method, controller: UIResponder): View {
        val root = IBOutletParser.rootView(controller)
            ?: throw IllegalStateException(
                "@IBAction ${method.name} bound before ${controller.javaClass.simpleName} has a view"
            )
        return root.findViewById<View?>(id)
            ?: throw IllegalStateException(
                "@IBAction ${method.name}: view $id not found in ${controller.javaClass.simpleName}"
            )
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
