package com.starkbank.cocoatouch.uikit

import org.junit.Assert.assertTrue
import org.junit.Test


class UIApplicationDelegateDefaultsTest {

    private val delegate = object : UIApplicationDelegate {}

    @Test
    fun launchingSucceedsWithoutAnOverride() {
        assertTrue(delegate.application(UIApplication, null))
    }

    @Test
    fun everyCallbackIsOptional() {
        delegate.applicationWillResignActive(UIApplication)
        delegate.applicationDidEnterBackground(UIApplication)
        delegate.applicationWillEnterForeground(UIApplication)
        delegate.applicationDidBecomeActive(UIApplication)
        delegate.applicationWillTerminate(UIApplication)
    }
}
