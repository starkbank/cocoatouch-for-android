package com.starkbank.cocoatouch.uikit

import android.text.InputFilter
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import java.lang.ref.WeakReference
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITextFieldContractTest {

    private class CountingDelegate : UITextFieldDelegate {

        var before: Int = 0
        var shouldChange: Int = 0
        var didChange: Int = 0

        override fun textFieldBeforeChange(textField: UITextField, s: CharSequence, start: Int, count: Int, after: Int) {
            before++
        }

        override fun textFieldShouldChange(textField: UITextField, s: CharSequence, start: Int, before: Int, count: Int) {
            shouldChange++
        }

        override fun textFieldDidChange(textField: UITextField) {
            didChange++
        }

        fun reset() {
            before = 0
            shouldChange = 0
            didChange = 0
        }
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var field: UITextField
    private lateinit var editText: EditText
    private val a = CountingDelegate()
    private val b = CountingDelegate()

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        onMain {
            field = UITextField().apply { widget = BackEditText(UIApplication.context(), null) }
            editText = field.widget as EditText
        }
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun settingDelegateTwiceDeliversOneDidChange() {
        onMain {
            field.setDelegate(a)
            field.setDelegate(a)
            editText.append("x")
            assertEquals(1, a.didChange)
        }
    }

    @Test
    fun replacingDelegateSilencesPreviousDelegate() {
        onMain {
            field.setDelegate(a)
            field.setDelegate(b)
            editText.append("x")
            assertEquals(0, a.didChange)
            assertEquals(1, b.didChange)
        }
    }

    @Test
    fun nilDelegateRemovesWatcher() {
        onMain {
            field.setDelegate(a)
            field.setDelegate(null)
            editText.append("x")
            assertEquals(0, a.didChange)
        }
    }

    @Test
    fun initThenSetDelegateDeliversOneDidChange() {
        onMain {
            field.init()
            field.setDelegate(a)
            editText.append("x")
            assertEquals(1, a.didChange)
        }
    }

    @Test
    fun programmaticSetTextFiresNoCallbacks() {
        onMain {
            field.setDelegate(a)
            field.setText("abc")
            assertEquals(0, a.before)
            assertEquals(0, a.shouldChange)
            assertEquals(0, a.didChange)
            assertEquals("abc", field.text())
            assertEquals(3, editText.selectionEnd)
        }
    }

    @Test
    fun userEditAfterSetTextNotifiesOnce() {
        onMain {
            field.setDelegate(a)
            field.setText("ab")
            a.reset()
            editText.append("c")
            assertEquals(1, a.didChange)
        }
    }

    @Test
    fun setTextReleasesTheLockWhenAFilterThrows() {
        onMain {
            field.setDelegate(a)
            editText.filters = arrayOf(InputFilter { _, _, _, _, _, _ -> throw IllegalStateException("filter") })
            assertThrows(IllegalStateException::class.java) { field.setText("x") }
            editText.filters = arrayOf()
            editText.append("y")
            assertEquals(1, a.didChange)
        }
    }

    @Test
    fun reentrantSetTextFromAnAppWatcherFiresNoCallbacks() {
        onMain {
            editText.addTextChangedListener(afterTextChanged = { s ->
                if (!s.toString().endsWith("!"))
                    field.setText("$s!")
            })
            field.setDelegate(a)
            field.setText("x")
            assertEquals("x!", field.text())
            assertEquals(0, a.didChange)
        }
    }

    @Test
    fun replacedDelegateIsNotRetained() {
        var weak: WeakReference<CountingDelegate>? = null
        onMain {
            val dropped = CountingDelegate()
            weak = WeakReference(dropped)
            field.setDelegate(dropped)
            field.setDelegate(b)
        }
        repeat(10) {
            if (weak!!.get() != null) {
                Runtime.getRuntime().gc()
                Thread.sleep(50)
            }
        }
        assertNull(weak!!.get())
    }

    // An assertion that fails on the main thread would crash the process instead of failing the test
    private fun onMain(block: () -> Unit) {
        var failure: Throwable? = null
        instrumentation.runOnMainSync {
            try {
                block()
            } catch (error: Throwable) {
                failure = error
            }
        }
        failure?.let { throw it }
    }
}
