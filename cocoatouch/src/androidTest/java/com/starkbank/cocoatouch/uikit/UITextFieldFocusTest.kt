package com.starkbank.cocoatouch.uikit

import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITextFieldFocusTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var a: BackEditText
    private lateinit var b: BackEditText
    private lateinit var fieldA: UITextField
    private lateinit var fieldB: UITextField

    @Before
    fun launch() {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        scenario.onActivity { activity ->
            a = BackEditText(activity, null).apply { imeOptions = EditorInfo.IME_ACTION_NEXT }
            b = BackEditText(activity, null)
            val column = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
            column.addView(a, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            column.addView(b, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            activity.addContentView(column, ViewGroup.LayoutParams(600, 600))
            fieldA = UITextField().apply { widget = a }
            fieldB = UITextField().apply { widget = b }
            fieldA.init()
            fieldB.init()
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        onMain { assertTrue(a.isInTouchMode) }
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun imeNextReachesAFieldThatResigned() {
        onMain {
            fieldB.resignFirstResponder()
            fieldA.becomeFirstResponder()
            a.onEditorAction(EditorInfo.IME_ACTION_NEXT)
            assertTrue(b.isFocused)
        }
    }

    @Test
    fun accessibilityClickFocusesAFieldThatResigned() {
        onMain {
            fieldB.resignFirstResponder()
            b.performAccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, null)
            assertTrue(b.isFocused)
        }
    }

    @Test
    fun resignLeavesTheFieldUnfocused() {
        onMain {
            fieldA.becomeFirstResponder()
            fieldA.resignFirstResponder()
            assertFalse(a.isFocused)
        }
    }
}
