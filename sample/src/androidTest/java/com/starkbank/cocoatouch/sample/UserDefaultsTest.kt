package com.starkbank.cocoatouch.sample

import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.starkbank.cocoatouch.foundation.UserDefaults
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDefaultsTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainStoryboard::class.java)

    @Test
    fun preferencesFileIsNamedAfterPackageNameAndKey() {
        activityRule.scenario.onActivity { activity ->
            UserDefaults.standard.set(true, "cocoatouchFlag")
            val preferences = activity.getSharedPreferences("com.starkbank.cocoatouch.samplecocoatouchFlag", Context.MODE_PRIVATE)
            assertTrue(preferences.getBoolean("cocoatouchFlag", false))
        }
    }
}
