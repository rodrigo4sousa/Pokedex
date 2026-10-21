@file:OptIn(ExperimentalTestApi::class)

package com.example.pokedex.system

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import com.example.pokedex.MainActivity
import com.example.pokedex.testutil.MockWebServerRule
import com.example.pokedex.testutil.UI_TIMEOUT_MS
import com.example.pokedex.testutil.openTab
import com.example.pokedex.testutil.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * US06 - guess the daily Pokémon from its silhouette.
 * Scenario: docs/architecture/4plus1/scenarios-guess-pokemon.puml.
 * TestClockModule fixes the date, so today's Pokémon is Bulbasaur; every test starts with no round played.
 */
@HiltAndroidTest
class GuessPokemonSystemTest {

    @get:Rule(order = 0)
    val server = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val compose = createEmptyComposeRule()

    private lateinit var app: ActivityScenario<MainActivity>

    @Before
    fun openGuessTab() {
        app = ActivityScenario.launch(MainActivity::class.java)
        compose.waitForText("Bulbasaur")
        compose.openTab("Guess")
        compose.waitUntilExactlyOneExists(hasContentDescription("Hidden Pokémon"), UI_TIMEOUT_MS)
    }

    @After
    fun closeApp() {
        app.close()
    }

    /** Types [name] and taps the Guess button (not the Guess tab, which is selectable). */
    private fun guess(name: String) {
        compose.onNode(hasSetTextAction()).performTextInput(name)
        compose.onNode(hasText("Guess") and hasClickAction() and !isSelectable()).performScrollTo().performClick()
    }

    @Test
    fun guessingRightRevealsThePokemonAndStartsAStreak() {
        // Until the round ends, nothing on screen names the Pokémon, not even for TalkBack.
        compose.onAllNodes(hasContentDescription("bulbasaur", substring = true, ignoreCase = true)).assertCountEquals(0)
        compose.onNodeWithContentDescription("3 of 3 hearts left").assertExists()

        guess("BULBA-saur")

        compose.waitForText("You got it!")
        compose.onNodeWithText("Bulbasaur").assertExists()
        compose.onNodeWithContentDescription("Bulbasaur").assertExists()
        compose.onNodeWithText("Streak: 1").assertExists()
        compose.onNodeWithText("Come back tomorrow for a new Pokémon.").assertExists()

        compose.onNodeWithText("Open details").performScrollTo().performClick()
        compose.waitForText("#001")
    }

    @Test
    fun threeWrongGuessesRevealThePokemonAndResetTheStreak() {
        guess("Ivysaur")
        compose.waitUntilExactlyOneExists(hasContentDescription("2 of 3 hearts left"), UI_TIMEOUT_MS)
        compose.onNodeWithText("Ivysaur").assertExists()

        guess("Agumon")
        compose.waitForText("“Agumon” isn't a Pokémon.")
        compose.onNodeWithContentDescription("2 of 3 hearts left").assertExists()

        guess("Venusaur")
        guess("Tauros")

        compose.waitForText("Out of hearts!")
        compose.onNodeWithText("Bulbasaur").assertExists()
        compose.onNodeWithText("Streak: 0").assertExists()
        compose.onNodeWithText("Come back tomorrow for a new Pokémon.").assertExists()
    }
}
