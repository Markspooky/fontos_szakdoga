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
class MaterialSearchUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MaterialSearch::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            DataSourcePreferences.setMode(activity, DataSourceMode.LOCAL_SQLITE)
        }
    }

    @Test
    fun materialSearchUI_elementsAreDisplayed() {
        onView(withId(R.id.searchEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.materialsRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun searchMaterial_typeQuery() {
        onView(withId(R.id.searchEditText))
            .perform(typeText("Cérna"), closeSoftKeyboard())
    }
}
