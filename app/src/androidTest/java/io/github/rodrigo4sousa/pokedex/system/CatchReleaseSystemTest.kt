package io.github.rodrigo4sousa.pokedex.system

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import io.github.rodrigo4sousa.pokedex.MainActivity
import io.github.rodrigo4sousa.pokedex.testutil.MockWebServerRule
import io.github.rodrigo4sousa.pokedex.testutil.UI_TIMEOUT_MS
import io.github.rodrigo4sousa.pokedex.testutil.catchFromGrid
import io.github.rodrigo4sousa.pokedex.testutil.openTab
import io.github.rodrigo4sousa.pokedex.testutil.scrollGridTo
import io.github.rodrigo4sousa.pokedex.testutil.starOnCard
import io.github.rodrigo4sousa.pokedex.testutil.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * US01 - add a Pokémon to my Pokédex; US02 - remove a Pokémon from my Pokédex.
 * Scenario: docs/architecture/4plus1/scenarios-catch-release.puml.
 * Each test starts with an empty in-memory database (TestDatabaseModule).
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class CatchReleaseSystemTest {

    @get:Rule(order = 0)
    val server = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val compose = createEmptyComposeRule()

    private lateinit var app: ActivityScenario<MainActivity>

    @Before
    fun launchApp() {
        app = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun closeApp() {
        app.close()
    }

    @Test
    fun catchingFromHomeAddsItToMyPokedex() {
        compose.catchFromGrid("Bulbasaur")

        compose.openTab("Pokédex")

        compose.waitForText("Caught 1 / 1025")
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
    }

    @Test
    fun releasingFromMyPokedexRemovesIt() {
        compose.catchFromGrid("Bulbasaur")
        compose.openTab("Pokédex")
        compose.waitForText("Caught 1 / 1025")

        compose.onNode(starOnCard("Bulbasaur", caught = true)).performClick()

        compose.waitForText("No Pokémon caught yet.", substring = true)
        compose.onNodeWithText("Caught 0 / 1025").assertIsDisplayed()
    }

    @Test
    fun catchingFromTheDetailPageAddsItToMyPokedex() {
        compose.waitForText("Bulbasaur")
        compose.scrollGridTo("Tauros")
        compose.onNodeWithText("Tauros").performClick()
        compose.waitForText("#128")

        compose.onNode(hasText("Catch") and hasClickAction()).performClick()
        compose.waitUntilExactlyOneExists(hasText("Release") and hasClickAction(), UI_TIMEOUT_MS)

        compose.onNodeWithContentDescription("Back").performClick()
        compose.openTab("Pokédex")
        compose.waitForText("Caught 1 / 1025")
        compose.onNodeWithText("Tauros").assertIsDisplayed()
    }

    @Test
    fun caughtPokemonIsStillShownAfterTheActivityIsRecreated() {
        compose.catchFromGrid("Bulbasaur")

        app.recreate()

        compose.waitUntilExactlyOneExists(starOnCard("Bulbasaur", caught = true), UI_TIMEOUT_MS)
        compose.openTab("Pokédex")
        compose.waitForText("Caught 1 / 1025")
    }
}
