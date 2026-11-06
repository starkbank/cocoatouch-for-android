package com.starkbank.cocoatouch.uikit

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

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
        val file = captureFile(context)
        try {
            assertEquals(fileProviderAuthority(context), tmpFileUri(context, file).authority)
        } finally {
            discardCapture(file)
        }
    }

    @Test
    fun aNewCaptureDeletesEveryEarlierCapture() {
        val earlier = listOf(
            File.createTempFile("tmp_image_file", ".jpeg", context.cacheDir),
            File.createTempFile("tmp_image_file", ".jpeg", context.cacheDir)
        )
        val unrelated = File.createTempFile("other_file", ".jpeg", context.cacheDir)
        val created = captureFile(context)
        try {
            assertTrue(earlier.none { it.exists() })
            assertEquals(listOf(created), captures())
            assertTrue(unrelated.exists())
        } finally {
            discardCapture(created)
            discardCapture(unrelated)
        }
    }

    @Test
    fun aCancelledCaptureIsDiscarded() {
        val file = captureFile(context)
        discardCapture(file)
        assertFalse(file.exists())
    }

    @Test
    fun discardingNothingIsHarmless() {
        discardCapture(null)
    }

    @Test
    fun aCancelledResultDeletesTheFileAndSendsNothing() {
        val delegate = RecordingPickerDelegate()
        val file = captureFile(context)
        var picker: UIImagePickerController? = null
        activityRule.scenario.onActivity { picker = UIImagePickerController.shared }
        onMain {
            picker!!.delegate = delegate
            picker!!.latestTmpFile = file
            picker!!.latestTmpUri = tmpFileUri(context, file)
            picker!!.onCaptureResult(false)
        }
        assertFalse(file.exists())
        assertNull(picker!!.latestTmpUri)
        assertEquals(0, delegate.uris.size)
    }

    @Test
    fun aSuccessfulResultKeepsTheFileAndSendsTheUri() {
        val delegate = RecordingPickerDelegate()
        val file = captureFile(context)
        val uri = tmpFileUri(context, file)
        var picker: UIImagePickerController? = null
        activityRule.scenario.onActivity { picker = UIImagePickerController.shared }
        try {
            onMain {
                picker!!.delegate = delegate
                picker!!.latestTmpFile = file
                picker!!.latestTmpUri = uri
                picker!!.onCaptureResult(true)
            }
            assertTrue(file.exists())
            assertEquals(listOf(uri), delegate.uris)
        } finally {
            discardCapture(file)
        }
    }

    private fun captures(): List<File> {
        return context.cacheDir.listFiles()!!.filter { it.name.startsWith("tmp_image_file") }
    }
}

private class RecordingPickerDelegate : UIImagePickerControllerDelegate {

    val uris = mutableListOf<Uri?>()

    override fun imagePickerControllerDidFinish(
        imagePickerController: UIImagePickerController,
        info: Map<UIImagePickerController.InfoKey, UIImage>
    ) {
        uris.add(info[UIImagePickerController.InfoKey.originalImage]?.uri)
    }
}
