package com.starkbank.cocoatouch.uikit

import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.RecordingViewController
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.rootController
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UINavigationControllerKnownGapTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var storyboard: UIStoryboard
    private lateinit var navigation: UINavigationController
    private lateinit var fragmentManager: FragmentManager
    private lateinit var root: RecordingViewController

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        scenario.onActivity { activity ->
            storyboard = activity
            navigation = navigation(activity)
            fragmentManager = activity.supportFragmentManager
            root = rootController(activity)
        }
    }

    @After
    fun close() {
        scenario.close()
    }

    // Pins audit CT-06: a pop back to the root runs its viewDidLoad again. Not fixed in this version.
    @Test
    fun ct06PopCallsViewDidLoadAgainOnRoot() {
        onMain {
            navigation.pushViewController(
                storyboard.instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed),
                false
            )
            navigation.popViewController(false)
            fragmentManager.executePendingTransactions()
            assertEquals(2, root.events.count { it == "viewDidLoad" })
        }
    }
}
