package com.starkbank.cocoatouch.foundation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationCenterSurfaceTest {

    @Test
    fun theTokenlessRemoveObserverIsHiddenFromJava() {
        val tokenless = NotificationCenter.Companion::class.java.declaredMethods
            .filter { it.name == "removeObserver" && it.parameterCount == 0 }
        assertEquals(1, tokenless.size)
        assertTrue(tokenless.single().isSynthetic)
    }
}
