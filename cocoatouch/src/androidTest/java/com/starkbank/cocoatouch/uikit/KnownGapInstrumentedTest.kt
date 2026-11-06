package com.starkbank.cocoatouch.uikit

import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.foundation.NSIndexPath
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.storyboard
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class KnownGapInstrumentedTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var scenario: ActivityScenario<TestStoryboard>

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
    }

    @After
    fun close() {
        scenario.close()
    }

    // Pins audit CT-02: the Disabled title overwrites the Normal one. Not fixed in this version.
    @Test
    fun ct02SetTitleForDisabledOverwritesNormal() {
        onMain {
            val button = UIButton().apply { widget = Button(UIApplication.context()) }
            button.setTitle("a", UIControlState.Normal)
            button.setTitle("b", UIControlState.Disabled)
            assertEquals("b", button.titleForState(UIControlState.Normal))
        }
    }

    // Pins audit CT-03: a presented controller lands on the push stack. Not fixed in this version.
    @Test
    fun ct03PresentedControllerJoinsThePushStack() {
        scenario.onActivity { activity ->
            val navigation = navigation(activity)
            navigation.pushViewController(pushed(activity), false)
            navigation.presentViewController(pushed(activity), false)
            activity.supportFragmentManager.executePendingTransactions()
            assertEquals(3, navigation.pushViewControllers.count())
            assertEquals(0, navigation.modalViewControllers.count())
        }
    }

    // Pins audit CT-07: willDisplay only fires from onScrolled for the last visible row. Not fixed in this version.
    @Test
    fun ct07WillDisplaySkipsRowsBeforeTheLastVisible() {
        val displayedRows = CopyOnWriteArrayList<Int>()
        lateinit var list: RecyclerView
        scenario.onActivity { activity ->
            val context = UIApplication.context()
            val tableView = UITableView().apply { widget = RecyclerView(context) }
            val container = FrameLayout(context)
            list = tableView.recyclerView()
            container.addView(list)
            activity.addContentView(
                container,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            tableView.setDataSource(object : UITableViewDataSource {
                override fun tableViewNumberOfRowsInSection(tableView: UITableView, section: Int): Int = 3

                override fun tableViewCellForRowAtIndexPath(
                    tableView: UITableView,
                    indexPath: NSIndexPath
                ): UITableViewCell = UITableViewCell().apply { widget = TextView(context).apply { text = "row" } }
            })
            tableView.setDelegate(object : UITableViewDelegate {
                override fun tableViewWillDisplayForRowAt(
                    tableView: UITableView,
                    willDisplay: UITableViewCell,
                    forRowAt: NSIndexPath
                ) {
                    displayedRows.add(forRowAt.row)
                }
            })
        }
        // The main thread must stay free to lay the list out, so the wait polls from the test thread
        var waitedMs = 0
        while (displayedRows.isEmpty() && waitedMs < 5000) {
            Thread.sleep(50)
            waitedMs += 50
        }
        onMain {
            assertTrue("table never reported a row, childCount=${list.childCount}", displayedRows.isNotEmpty())
            val lastVisible = (list.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            assertTrue("lastVisible=$lastVisible", lastVisible >= 1)
            assertEquals(listOf(lastVisible), displayedRows.toList())
            assertTrue(displayedRows.none { it < lastVisible })
        }
    }

    // Pins audit CT-16: a recreated activity gets a new navigation controller holding only the initial controller.
    @Test
    fun ct16RecreateKeepsOnlyTheInitialController() {
        scenario.onActivity { navigation(it).pushViewController(pushed(it), false) }
        scenario.recreate()
        scenario.onActivity {
            assertEquals(1, navigation(it).viewControllers.size)
            assertEquals(1, it.supportFragmentManager.fragments.size)
            assertEquals(0, it.supportFragmentManager.backStackEntryCount)
        }
    }

    // The modal presentation reads the controller's storyboard, which only storyboard-made controllers have
    private fun pushed(activity: UIActivity): UIViewController {
        return storyboard(activity).instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed)
    }
}
