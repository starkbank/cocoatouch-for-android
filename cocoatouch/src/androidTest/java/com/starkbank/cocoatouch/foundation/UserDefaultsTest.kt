package com.starkbank.cocoatouch.foundation

import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDefaultsTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestStoryboard::class.java)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun stringIsStoredInPackagePrefixedFile() {
        UserDefaults.standard.set("value", "token")
        assertEquals("value", context.getSharedPreferences(context.packageName + "token", Context.MODE_PRIVATE).getString("token", null))
    }

    @Test
    fun removeObjectDeletesValue() {
        UserDefaults.standard.set("value", "token")
        UserDefaults.standard.removeObject("token")
        assertNull(UserDefaults.standard.string("token"))
    }
}
