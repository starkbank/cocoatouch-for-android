package com.starkbank.cocoatouch.uikit

import androidx.fragment.app.FragmentManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.TestStoryboard
import com.starkbank.cocoatouch.navigation
import com.starkbank.cocoatouch.onMain
import com.starkbank.cocoatouch.test.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UINavigationControllerPopTest {

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
    fun popRemovesOnlyTheTopWhenTheSameControllerIsPushedTwice() {
        var backStackBeforePop = 0
        onMain {
            val repeated = storyboard.instantiateViewControllerWithIdentifier(R.layout.cocoatouch_test_pushed)
            repeat(2) {
                navigation.pushViewController(repeated, false)
                fragmentManager.executePendingTransactions()
            }
            backStackBeforePop = fragmentManager.backStackEntryCount
            navigation.popViewController(false)
            fragmentManager.executePendingTransactions()
            assertEquals(2, navigation.viewControllers.size)
        }
        assertEquals(backStackBeforePop - 1, fragmentManager.backStackEntryCount)
    }
}
