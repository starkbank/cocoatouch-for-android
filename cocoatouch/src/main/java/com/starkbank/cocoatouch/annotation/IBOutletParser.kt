package com.starkbank.cocoatouch.annotation
import android.view.View
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap


object IBOutletParser {

    private val fieldsByType = ConcurrentHashMap<Class<*>, List<Field>>()

    fun parse(controller: UIResponder) {
        for (field in outletFields(controller.javaClass)) {
            parse(field, controller)
        }
    }

    internal fun outletFields(type: Class<*>): List<Field> {
        return fieldsByType.computeIfAbsent(type) { walk(it) }
    }

    // Walks up to UIResponder so a base controller's outlets are bound; a subclass field hides the parent's by name.
    // The result is constant per class, so it is built once and made accessible once
    private fun walk(type: Class<*>): List<Field> {
        val fieldsByName = LinkedHashMap<String, Field>()
        var current: Class<*>? = type
        while (current != null && current != UIResponder::class.java) {
            for (field in current.declaredFields) {
                if (!field.isAnnotationPresent(IBOutlet::class.java)) continue
                field.isAccessible = true
                fieldsByName.putIfAbsent(field.name, field)
            }
            current = current.superclass
        }
        return fieldsByName.values.toList()
    }

    private fun parse(field: Field, controller: UIResponder) {
        val outlet = field.getAnnotation(IBOutlet::class.java)!!
        val root = rootView(controller)
            ?: throw IllegalStateException(
                "@IBOutlet ${field.name} (${outlet.value}) bound before ${controller.javaClass.simpleName} has a view"
            )
        val view: View = root.findViewById(outlet.value)
            ?: throw IllegalStateException(
                "@IBOutlet ${field.name} (${outlet.value}) not found in ${controller.javaClass.simpleName}"
            )
        val responder = field.type.newInstance() as UIResponder
        responder.widget = view
        field[controller] = responder
    }

    // Same preconditions as the viewWithTag overrides: a controller looks in its view, a fragment-backed view in its
    // fragment's container, anything else in its own widget
    private fun rootView(host: UIResponder): View? {
        if (host is UIViewController) return host.view?.let { rootView(it) }
        if (host !is UIView) return host.widget
        val fragment = host.fragment() ?: return host.widget
        return fragment.container
    }
}
