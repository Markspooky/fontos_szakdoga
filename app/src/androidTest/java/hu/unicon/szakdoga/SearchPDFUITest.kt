package hu.unicon.szakdoga

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.unicon.szakdoga.repository.DataSourceMode
import hu.unicon.szakdoga.repository.DataSourcePreferences
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchPDFUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(SearchPDF::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            DataSourcePreferences.setMode(activity, DataSourceMode.LOCAL_SQLITE)
        }
    }

    @Test
    fun pdfSearchUI_elementsAreDisplayed() {
        onView(withId(R.id.searchEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.explorerRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.breadcrumbChipGroup)).check(matches(isDisplayed()))
    }

    @Test
    fun searchPdf_typeQuery() {
        onView(withId(R.id.searchEditText))
            .perform(typeText("Gyártási"), closeSoftKeyboard())
    }
}
