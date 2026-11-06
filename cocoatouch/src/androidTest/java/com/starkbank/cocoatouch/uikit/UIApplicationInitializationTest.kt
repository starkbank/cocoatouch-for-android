package com.starkbank.cocoatouch.uikit

import android.content.Context
import android.content.ContextWrapper
import androidx.startup.AppInitializer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.CocoaTouchInitializer
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIApplicationInitializationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun theInitializerIsRegisteredWithAndroidxStartup() {
        assertTrue(AppInitializer.getInstance(context).isEagerlyInitialized(CocoaTouchInitializer::class.java))
    }

    @Test
    fun theInitializerStoresTheApplicationContextAndInitializeKeepsTheFirstOne() {
        val saved = UIApplication.applicationContext
        try {
            UIApplication.applicationContext = null
            CocoaTouchInitializer().create(context)
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
