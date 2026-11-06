package com.starkbank.cocoatouch.uikit

import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.starkbank.cocoatouch.foundation.NSIndexPath
import com.starkbank.cocoatouch.test.R
import androidx.appcompat.widget.SwitchCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UIAppearanceTest {

    private class TestAppearance(
        val action: String = "#FFFFFF",
        val headerFont: UIFont? = null,
        val border: String = "#CCCCCC",
        val secondary: String = "#EFEFF0",
        val darkSecondary: String = secondary
    ) : UIAppearance by UIDefaultAppearance {

        override fun onActionColor(traitCollection: UITraitCollection): UIColor {
            return UIColor(action)
        }

        override fun sectionHeaderFont(traitCollection: UITraitCollection): UIFont? {
            return headerFont
        }

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
            val onActionColor = UIApplication.appearance.onActionColor(UITraitCollection.current).colorInt
            assertEquals(onActionColor, (uiSwitch.widget as SwitchCompat).thumbTintList!!.defaultColor)
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

    @Test
    fun activityStartsWithTheConfigurationTrait() {
        scenario.onActivity {
            assertEquals(
                UITraitCollection.from(it.resources.configuration.uiMode).userInterfaceStyle,
                UITraitCollection.current.userInterfaceStyle
            )
        }
    }

    // AppCompat resets the resources to the system uiMode inside super.onConfigurationChanged, so a night
    // configuration cannot be injected; seed the opposite style and expect it to be refreshed from the resources
    @Test
    fun configurationChangeUpdatesTheTraitCollection() {
        scenario.onActivity { activity ->
            val expected = UITraitCollection.from(activity.resources.configuration.uiMode).userInterfaceStyle
            val stale = if (expected == UIUserInterfaceStyle.dark) UIUserInterfaceStyle.light else UIUserInterfaceStyle.dark
            UITraitCollection.current = UITraitCollection(stale)
            activity.onConfigurationChanged(Configuration(activity.resources.configuration))
            assertEquals(expected, UITraitCollection.current.userInterfaceStyle)
        }
    }

    @Test
    fun buttonTintUsesAppearanceActionColor() {
        UIApplication.appearance = TestAppearance(action = "#ABCDEF")
        var tint: UIColor? = null
        instrumentation.runOnMainSync {
            val button = UIButton().apply { widget = Button(UIApplication.context()) }
            val field = UIButton::class.java.getDeclaredField("_tintColor")
            field.isAccessible = true
            tint = field.get(button) as UIColor
        }
        assertEquals(Color.parseColor("#ABCDEF"), tint!!.colorInt)
    }

    @Test
    fun sectionHeaderFontReachesTheTitleCell() {
        UIApplication.appearance = TestAppearance(headerFont = UIFont(R.font.cocoatouch_test_font, 18f))
        var titleView: TextView? = null
        instrumentation.runOnMainSync {
            val context = UIApplication.context()
            val tableView = UITableView().apply { widget = RecyclerView(context) }
            tableView.setDataSource(object : UITableViewDataSource {
                override fun tableViewNumberOfRowsInSection(tableView: UITableView, section: Int): Int = 0

                override fun tableViewCellForRowAtIndexPath(
                    tableView: UITableView,
                    indexPath: NSIndexPath
                ): UITableViewCell = UITableViewCell()

                override fun tableViewTitleForHeaderInSection(tableView: UITableView, section: Int): String? = "Header"
            })
            val adapter = tableView.recyclerView().adapter!!
            val holder = adapter.createViewHolder(FrameLayout(context), adapter.getItemViewType(0))
            adapter.bindViewHolder(holder, 0)
            titleView = (holder as UIViewHolder).container.getChildAt(0) as TextView
        }
        val metrics = UIApplication.context().resources.displayMetrics
        assertEquals(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, metrics), titleView!!.textSize, 0.01f)
        assertSame(ResourcesCompat.getFont(UIApplication.context(), R.font.cocoatouch_test_font), titleView!!.typeface)
    }
}
