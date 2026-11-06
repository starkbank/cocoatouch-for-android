package com.starkbank.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShrinkerSmokeTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainStoryboard::class.java)

    @Test
    fun outletIsBoundInMinifiedBuild() {
        onView(withId(R.id.mainTextfield)).perform(click())
        onView(withId(R.id.textField1)).check(matches(withHint("Random TextField1")))
    }

    @Test
    fun actionIsInvokedInMinifiedBuild() {
        onView(withId(R.id.mainTextfield)).perform(click())
        onView(withId(R.id.textfield_bfr)).perform(click())
        onView(withId(R.id.textField1)).check(matches(hasFocus()))
    }

    @Test
    fun inheritedOutletIsBoundInMinifiedBuild() {
        onView(withId(R.id.mainTextfield)).perform(click())
        onView(withId(R.id.textField1)).check(matches(withHint("Random TextField1")))
    }

    @Test
    fun appSubclassOutletIsInstantiatedInMinifiedBuild() {
        onView(withId(R.id.mainTextfield)).perform(click())
        onView(withId(R.id.textField2)).check(matches(withHint("Random TextField2")))
    }

    @Test
    fun cellOutletIsBoundInMinifiedBuild() {
        onView(withId(R.id.mainTableview)).perform(click())
        onView(withText("Jon Snow")).check(matches(isDisplayed()))
    }

    @Test
    fun minifiedAppLaunches() {
        onView(withId(R.id.mainTextfield)).check(matches(isDisplayed()))
    }
}
