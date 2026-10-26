package com.ordra.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ordra.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_showsBrandAndEmptyState() {
        val tagline = composeTestRule.activity.getString(R.string.ordra_tagline)
        val emptyTitle = composeTestRule.activity.getString(R.string.home_empty_title)
        composeTestRule.onNodeWithText(tagline).assertIsDisplayed()
        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }
}
