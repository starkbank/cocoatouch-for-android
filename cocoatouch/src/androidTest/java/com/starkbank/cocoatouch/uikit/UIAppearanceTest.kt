package com.starkbank.cocoatouch.uikit

import android.graphics.Color
import androidx.appcompat.widget.SwitchCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIAppearanceTest {

    private class TestAppearance(
        val border: String = "#CCCCCC",
        val secondary: String = "#EFEFF0",
        val darkSecondary: String = secondary
    ) : UIAppearance by UIDefaultAppearance {

        override fun borderColor(traitCollection: UITraitCollection): UIColor {
            return UIColor(border)
        }

        override fun secondaryBackgroundColor(traitCollection: UITraitCollection): UIColor {
            if (traitCollection.userInterfaceStyle == UIUserInterfaceStyle.dark) return UIColor(darkSecondary)
            return UIColor(secondary)
        }
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var savedTrait: UITraitCollection

    @Before
    fun save() {
        savedTrait = UITraitCollection.current
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun restore() {
        UIApplication.appearance = UIDefaultAppearance
        UITraitCollection.current = savedTrait
        scenario.close()
    }

    @Test
    fun defaultAppearanceKeepsPreviousLiterals() {
        val trait = UITraitCollection.current
        assertEquals(Color.WHITE, UIDefaultAppearance.onActionColor(trait).colorInt)
        assertEquals(Color.LTGRAY, UIDefaultAppearance.borderColor(trait).colorInt)
        assertEquals(Color.parseColor("#EFEFF0"), UIDefaultAppearance.secondaryBackgroundColor(trait).colorInt)
        assertNull(UIDefaultAppearance.sectionHeaderFont(trait))
    }

    @Test
    fun switchTrackUsesAppearanceBorderColor() {
        UIApplication.appearance = TestAppearance(border = "#123456")
        instrumentation.runOnMainSync {
            val uiSwitch = UISwitch().apply { widget = SwitchCompat(UIApplication.context()) }
            uiSwitch.onTintColor = UIColor("#000000")
            val track = (uiSwitch.widget as SwitchCompat).trackTintList!!
            assertEquals(Color.parseColor("#123456"), track.getColorForState(intArrayOf(), 0))
        }
    }

    @Test
    fun appearanceChangeAppliesToNextSearchBar() {
        instrumentation.runOnMainSync {
            UIApplication.appearance = TestAppearance(secondary = "#111111")
            assertEquals(Color.parseColor("#111111"), UISearchBar().barTintColor!!.colorInt)
            UIApplication.appearance = TestAppearance(secondary = "#222222")
            assertEquals(Color.parseColor("#222222"), UISearchBar().barTintColor!!.colorInt)
        }
    }

    @Test
    fun darkTraitReachesTheAppearance() {
        UIApplication.appearance = TestAppearance(secondary = "#EEEEEE", darkSecondary = "#111111")
        UITraitCollection.current = UITraitCollection(UIUserInterfaceStyle.dark)
        instrumentation.runOnMainSync {
            assertEquals(Color.parseColor("#111111"), UISearchBar().barTintColor!!.colorInt)
        }
    }
}
