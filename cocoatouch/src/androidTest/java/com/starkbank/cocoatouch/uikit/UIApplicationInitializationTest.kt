package com.starkbank.cocoatouch.uikit

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ProviderInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.CocoaTouchInitializer
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIApplicationInitializationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun theInitializerProviderIsDeclared() {
        val authority = "${context.packageName}.cocoatouch-initializer"
        assertNotNull(context.packageManager.resolveContentProvider(authority, 0))
    }

    @Test
    fun theProviderStoresTheApplicationContextAndInitializeKeepsTheFirstOne() {
        val saved = UIApplication.applicationContext
        try {
            UIApplication.applicationContext = null
            CocoaTouchInitializer().attachInfo(context, ProviderInfo())
            assertSame(context.applicationContext, UIApplication.applicationContext)

            UIApplication.initialize(
                object : ContextWrapper(context) {
                    override fun getApplicationContext(): Context = this
                }
            )
            assertSame(context.applicationContext, UIApplication.applicationContext)
        } finally {
            UIApplication.applicationContext = saved
        }
    }
}
