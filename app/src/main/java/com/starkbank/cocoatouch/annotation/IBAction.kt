package com.starkbank.cocoatouch.annotation
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention
import androidx.annotation.IdRes


@Retention(RetentionPolicy.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class IBAction(@IdRes final val value: Int)
