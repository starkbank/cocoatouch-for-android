package com.starkbank.cocoatouch.uikit

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.foundation.UserDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

// Never launches an activity: the content provider must have run at process start
@RunWith(AndroidJUnit4::class)
class UIApplicationInitializationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun applicationContextExistsBeforeAnyActivity() {
        assertNotNull(UIApplication.applicationContext)
    }

    @Test
    fun theInitializerProviderIsDeclared() {
        val authority = "${context.packageName}.cocoatouch-initializer"
        assertNotNull(context.packageManager.resolveContentProvider(authority, 0))
    }

    @Test
    fun userDefaultsWorkBeforeAnyActivity() {
        UserDefaults.standard.set("x", "beforeAnyActivity")
        assertEquals("x", UserDefaults.standard.string("beforeAnyActivity"))
    }
}
