package com.starkbank.cocoatouch.annotation
import android.view.View
import com.starkbank.cocoatouch.uikit.UIResponder
import com.starkbank.cocoatouch.uikit.UIView
import com.starkbank.cocoatouch.uikit.UIViewController
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap


internal class OutletField(val field: Field, val isShadowed: Boolean)


object IBOutletParser {

    private val fieldsByType = ConcurrentHashMap<Class<*>, List<OutletField>>()

    @InternalCocoaTouchApi
    fun parse(controller: UIResponder) {
        for (outletField in outletFields(controller.javaClass)) {
            parse(outletField, controller)
        }
    }

    internal fun outletFields(type: Class<*>): List<OutletField> {
        return fieldsByType.computeIfAbsent(type) { walk(it) }
    }

    // Walks up to UIResponder so a base controller's outlets are bound, subclass first. A field whose name a subclass
    // outlet already declared is shadowed and is bound only when its id exists in the layout: field names survive R8
    // through the consumer rules but getter names do not, so overriding cannot be detected from the accessors, and a
    // Kotlin override reads through a virtual accessor, which makes the subclass field the one in use, or a private
    // base field sits next to a subclass field of the same name. The result is constant per class, so it is built
    // once and made accessible once
    private fun walk(type: Class<*>): List<OutletField> {
        val outletFields = ArrayList<OutletField>()
        val namesSeen = HashSet<String>()
        var current: Class<*>? = type
        while (current != null && current != UIResponder::class.java) {
            val declaredHere = ArrayList<String>()
            for (field in current.declaredFields) {
                if (!field.isAnnotationPresent(IBOutlet::class.java)) continue
                field.isAccessible = true
                outletFields.add(OutletField(field, field.name in namesSeen))
                declaredHere.add(field.name)
            }
            namesSeen.addAll(declaredHere)
            current = current.superclass
        }
        return outletFields
    }

    private fun parse(outletField: OutletField, controller: UIResponder) {
        val field = outletField.field
        val outlet = field.getAnnotation(IBOutlet::class.java)!!
        val root = rootView(controller)
            ?: throw IllegalStateException(
                "@IBOutlet ${field.name} (${outlet.value}) bound before ${controller.javaClass.simpleName} has a view"
            )
        val view: View? = root.findViewById(outlet.value)
        if (view == null && outletField.isShadowed) return
        if (view == null) {
            throw IllegalStateException(
                "@IBOutlet ${field.name} (${outlet.value}) not found in ${controller.javaClass.simpleName}"
            )
        }
        val responder = field.type.newInstance() as UIResponder
        responder.widget = view
        field[controller] = responder
    }

    // Same preconditions as the viewWithTag overrides: a controller looks in its view, a fragment-backed view in its
    // fragment's container, anything else in its own widget
    internal fun rootView(host: UIResponder): View? {
        if (host is UIViewController) return host.view?.let { rootView(it) }
        if (host !is UIView) return host.widget
        val fragment = host.fragment() ?: return host.widget
        return fragment.container
    }
}
