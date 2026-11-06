package com.starkbank.cocoatouch

// The android.jar stubs ignore their Context, and Kotlin forbids a literal null for a Java non-null parameter
@Suppress("UNCHECKED_CAST")
internal fun <T> nullOf(): T = null as T
