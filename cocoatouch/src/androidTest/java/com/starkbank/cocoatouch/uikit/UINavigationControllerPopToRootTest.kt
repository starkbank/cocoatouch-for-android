package com.starkbank.cocoatouch.uikit

import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UINavigationControllerPopToRootTest {

    private lateinit var scenario: ActivityScenario<TestStoryboard>
    private lateinit var storyboard: UIStoryboard
    private lateinit var navigation: UINavigationController
    private lateinit var fragmentManager: FragmentManager

    @Before
    fun launch() {
        scenario = ActivityScenario.launch(TestStoryboard::class.java)
        scenario.onActivity { activity ->
            storyboard = activity
            navigation = navigation(activity)
            fragmentManager = activity.supportFragmentManager
        }
    }

    @After
    fun close() {
        scenario.close()
    }

    @Test
    fun popToRootRemovesEveryPushedController() {
        onMain {
            repeat(2) {
                navigation.pushViewController(
                    storyboard.instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed),
                    false
                )
                fragmentManager.executePendingTransactions()
            }
            navigation.popToRootViewController(false)
            fragmentManager.executePendingTransactions()
            assertEquals(0, fragmentManager.backStackEntryCount)
            assertEquals(1, navigation.viewControllers.size)
            assertEquals(1, fragmentManager.fragments.size)
        }
    }

    @Test
    fun popToRootWithControllersOfRootClass() {
        onMain {
            repeat(2) {
                navigation.pushViewController(
                    storyboard.instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_root),
                    false
                )
                fragmentManager.executePendingTransactions()
            }
            navigation.popToRootViewController(false)
            fragmentManager.executePendingTransactions()
            assertEquals(0, fragmentManager.backStackEntryCount)
            assertEquals(1, navigation.viewControllers.size)
            assertEquals(1, fragmentManager.fragments.size)
        }
    }
}
