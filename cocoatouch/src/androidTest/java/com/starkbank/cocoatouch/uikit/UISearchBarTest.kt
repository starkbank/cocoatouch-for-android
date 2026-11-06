package com.starkbank.cocoatouch.uikit

import android.graphics.Color
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.collectGarbageOnce
import com.starkbank.cocoatouch.onMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UISearchBarTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var searchBar: UISearchBar
    private lateinit var container: FrameLayout
    private lateinit var delegate: RecordingSearchBarDelegate

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        instrumentation.runOnMainSync {
            val activity = UIApplication.context() as android.app.Activity
            val searchView = SearchView(activity)
            container = FrameLayout(activity).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                addView(searchView)
            }
            activity.findViewById<ViewGroup>(android.R.id.content).addView(container)
            searchBar = UISearchBar().apply { widget = searchView }
        }
    }

    @After
    fun close() {
        instrumentation.setInTouchMode(true)
        scenario.close()
    }

    @Test
    fun answeringFalseClearsFocus() {
        instrumentation.runOnMainSync {
            searchBar.delegate = RecordingSearchBarDelegate(answer = false).also { delegate = it }
            searchBar.getEditText().requestFocus()
            assertEquals(1, delegate.calls)
            assertFalse(searchBar.getEditText().hasFocus())
        }
    }

    @Test
    fun answeringFalseWithNoOtherFocusableViewEndsUnfocused() {
        instrumentation.runOnMainSync {
            container.isFocusable = false
            container.isFocusableInTouchMode = false
        }
        instrumentation.setInTouchMode(false)
        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB)
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync {
            searchBar.delegate = RecordingSearchBarDelegate(answer = false).also { delegate = it }
            searchBar.getEditText().requestFocus()
            assertEquals(1, delegate.calls)
            assertFalse(searchBar.getEditText().hasFocus())
        }
    }

    @Test
    fun answeringTrueKeepsFocus() {
        instrumentation.runOnMainSync {
            searchBar.delegate = RecordingSearchBarDelegate(answer = true).also { delegate = it }
            searchBar.getEditText().requestFocus()
            assertEquals(1, delegate.calls)
            assertTrue(searchBar.getEditText().hasFocus())
        }
    }

    @Test
    fun anInlineDelegateObjectIsKept() {
        onMain { assignInlineDelegate() }
        repeat(20) { collectGarbageOnce() }
        onMain { assertNotNull(searchBar.delegate) }
    }

    private fun assignInlineDelegate() {
        searchBar.delegate = object : UISearchBarDelegate {
            override fun searchBarShouldBeginEditing(searchBar: UISearchBar): Boolean = false
        }
    }

    @Test
    fun settingTintColorDoesNotCrashOnAnyApiLevel() {
        instrumentation.runOnMainSync {
            searchBar.tintColor = UIColor("#FF0000")
            assertEquals(Color.parseColor("#FF0000"), searchBar.tintColor!!.colorInt)
        }
    }
}

private class RecordingSearchBarDelegate(private val answer: Boolean) : UISearchBarDelegate {

    var calls: Int = 0

    override fun searchBarShouldBeginEditing(searchBar: UISearchBar): Boolean {
        calls += 1
        return answer
    }
}
