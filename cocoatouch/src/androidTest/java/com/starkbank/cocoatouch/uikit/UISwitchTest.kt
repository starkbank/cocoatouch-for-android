package com.starkbank.cocoatouch.uikit

import androidx.appcompat.widget.SwitchCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UISwitchTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun settingOnTintColorToNullClearsTheTint() {
        onMain {
            val widget = SwitchCompat(UIApplication.context())
            val uiSwitch = UISwitch().apply { this.widget = widget }
            uiSwitch.onTintColor = UIColor("#123456")
            assertNotNull(widget.trackTintList)
            uiSwitch.onTintColor = null
            assertNull(widget.trackTintList)
            assertNull(widget.thumbTintList)
            assertNull(uiSwitch.onTintColor)
        }
    }
}
