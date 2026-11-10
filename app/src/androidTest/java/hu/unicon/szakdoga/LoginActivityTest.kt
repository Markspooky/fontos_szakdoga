package hu.unicon.szakdoga

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.unicon.szakdoga.api.UserSession
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            UserSession.logout(activity)
        }
    }

    @Test
    fun loginUI_elementsAreDisplayed() {
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.btnQuickAdmin)).check(matches(isDisplayed()))
    }

    @Test
    fun quickLoginAdmin_clicksButton() {
        onView(withId(R.id.btnQuickAdmin)).perform(click())
    }

    @Test
    fun manualLogin_typeUsernameAndClick() {
        onView(withId(R.id.usernameEditText))
            .perform(typeText("admin"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())
    }
}
