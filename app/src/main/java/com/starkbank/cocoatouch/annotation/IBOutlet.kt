package com.starkbank.cocoatouch.annotation
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention
import androidx.annotation.IdRes


@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IBOutlet(@IdRes val value: Int)
