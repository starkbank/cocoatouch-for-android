package com.starkbank.cocoatouch.annotation

@RequiresOptIn(message = "Android implementation detail of CocoaTouch; prefer the UIKit API.", level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class InternalCocoaTouchApi
