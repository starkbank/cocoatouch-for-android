package com.starkbank.cocoatouch.uikit

import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        val file = captureFile(context, null)
        try {
            assertEquals(fileProviderAuthority(context), tmpFileUri(context, file).authority)
        } finally {
            discardCapture(file)
        }
    }

    @Test
    fun aNewCaptureDeletesThePreviousOne() {
        val first = captureFile(context, null)
        val second = captureFile(context, first)
        try {
            assertFalse(first.exists())
            assertTrue(second.exists())
        } finally {
            discardCapture(second)
        }
    }

    @Test
    fun aCancelledCaptureIsDiscarded() {
        val file = captureFile(context, null)
        discardCapture(file)
        assertFalse(file.exists())
    }

    @Test
    fun discardingNothingIsHarmless() {
        discardCapture(null)
    }
}
