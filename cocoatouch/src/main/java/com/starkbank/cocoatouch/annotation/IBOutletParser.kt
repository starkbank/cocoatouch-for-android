package com.starkbank.cocoatouch.annotation
import com.starkbank.cocoatouch.uikit.UIResponder
import java.lang.reflect.Field


object IBOutletParser {

    fun parse(controller: UIResponder) {
        for (field in outletFields(controller.javaClass)) {
            parse(field, controller)
        }
    }

    // Walks up to UIResponder so a base controller's outlets are bound; a subclass field hides the parent's by name
    internal fun outletFields(type: Class<*>): List<Field> {
        val fieldsByName = LinkedHashMap<String, Field>()
        var current: Class<*>? = type
        while (current != null && current != UIResponder::class.java) {
            for (field in current.declaredFields) {
                if (!field.isAnnotationPresent(IBOutlet::class.java)) continue
                fieldsByName.putIfAbsent(field.name, field)
            }
            current = current.superclass
        }
        return fieldsByName.values.toList()
    }

    private fun parse(field: Field, controller: UIResponder) {
        field.isAccessible = true
        val outlet = field.getAnnotation(IBOutlet::class.java)!!
        val responder = field.type.newInstance() as UIResponder
        responder.widget = try {
            controller.viewWithTag(outlet.value)
        } catch (e: NullPointerException) {
            throw IllegalStateException(
                "@IBOutlet ${field.name} (${outlet.value}) not found in ${controller.javaClass.simpleName}",
                e
            )
        }
        field[controller] = responder
    }
}
