package com.starkbank.cocoatouch.annotation
import androidx.annotation.IdRes
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IBOutlet(@IdRes val value: Int)
