package com.starkbank.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.uikit.UIFragment

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackPressedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainStoryboard::class.java)

    @Test
    fun backPopsPushedViewController() {
        onView(withId(R.id.mainTableview)).perform(click())
        onView(withId(R.id.tableview)).check(matches(isDisplayed()))

        pressBack()

        onView(withId(R.id.tableview)).check(doesNotExist())
        onView(withId(R.id.mainTableview)).check(matches(isDisplayed()))
        activityRule.scenario.onActivity { activity ->
            val rootFragment = activity.supportFragmentManager.fragments.first() as UIFragment
            val navigationController = rootFragment.viewController!!.navigationController!!
            assertEquals(1, navigationController.viewControllers.size)
        }
    }
}
