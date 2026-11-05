package com.starkbank.cocoatouch.uikit

import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIImagePickerControllerTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(TestStoryboard::class.java)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun fileProviderAuthorityUsesPackageName() {
        assertEquals("com.starkbank.cocoatouch.test.provider", fileProviderAuthority(context))
    }

    @Test
    fun tmpFileUriIsServedByTheConsumerProvider() {
        assertEquals(fileProviderAuthority(context), tmpFileUri(context).authority)
    }
}
