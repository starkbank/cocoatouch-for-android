package com.starkbank.cocoatouch.annotation
import com.starkbank.cocoatouch.uikit.UIResponder
import java.lang.reflect.Field


object IBOutletParser {

    fun parse(controller: UIResponder) {
        val fields: Array<Field> = controller.javaClass.declaredFields
        for (field in fields) {
            parse(field, controller)
        }
    }

    private fun parse(field: Field, controller: UIResponder) {
        field.isAccessible = true
        if (field.isAnnotationPresent(IBOutlet::class.java)) {
            val outlet = field.getAnnotation(IBOutlet::class.java)
            try {
                val responder = field.type.newInstance() as UIResponder
                responder.widget = controller.viewWithTag(outlet.value)
                field[controller] = responder
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }
}