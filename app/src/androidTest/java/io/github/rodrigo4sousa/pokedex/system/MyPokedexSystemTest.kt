package io.github.rodrigo4sousa.pokedex.system

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import io.github.rodrigo4sousa.pokedex.MainActivity
import io.github.rodrigo4sousa.pokedex.testutil.MockWebServerRule
import io.github.rodrigo4sousa.pokedex.testutil.catchFromGrid
import io.github.rodrigo4sousa.pokedex.testutil.openTab
import io.github.rodrigo4sousa.pokedex.testutil.typeInSearch
import io.github.rodrigo4sousa.pokedex.testutil.waitForNoText
import io.github.rodrigo4sousa.pokedex.testutil.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * US05 - review my Pokédex, to see which Pokémon I have already caught.
 * Scenario: docs/architecture/4plus1/scenarios-my-pokedex.puml.
 * The total (1025) comes from the pokemon-species-count fixture.
 */
@HiltAndroidTest
class MyPokedexSystemTest {

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
    fun showsAnEmptyMessageWhenNothingIsCaught() {
        compose.waitForText("Bulbasaur")

        compose.openTab("Pokédex")

        compose.waitForText("Caught 0 / 1025")
        compose.onNodeWithText("No Pokémon caught yet.", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsOnlyCaughtPokemonWithTheCount() {
        compose.catchFromGrid("Bulbasaur")
        compose.catchFromGrid("Tauros")

        compose.openTab("Pokédex")

        compose.waitForText("Caught 2 / 1025")
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("Tauros").assertIsDisplayed()
        compose.onNodeWithText("Ivysaur").assertDoesNotExist()
        compose.onNodeWithText("Eevee").assertDoesNotExist()
    }

    @Test
    fun searchFindsCaughtPokemonByName() {
        compose.catchFromGrid("Bulbasaur")
        compose.catchFromGrid("Tauros")
        compose.openTab("Pokédex")
        compose.waitForText("Caught 2 / 1025")

        compose.typeInSearch("tau")

        compose.waitForNoText("Bulbasaur")
        compose.onNodeWithText("Tauros").assertIsDisplayed()
    }
}
