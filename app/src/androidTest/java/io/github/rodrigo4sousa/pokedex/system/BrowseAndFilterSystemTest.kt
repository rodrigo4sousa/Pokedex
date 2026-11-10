package io.github.rodrigo4sousa.pokedex.system

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import io.github.rodrigo4sousa.pokedex.MainActivity
import io.github.rodrigo4sousa.pokedex.testutil.MockWebServerRule
import io.github.rodrigo4sousa.pokedex.testutil.scrollGridTo
import io.github.rodrigo4sousa.pokedex.testutil.typeInSearch
import io.github.rodrigo4sousa.pokedex.testutil.waitForNoText
import io.github.rodrigo4sousa.pokedex.testutil.waitForRequest
import io.github.rodrigo4sousa.pokedex.testutil.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Rule
import org.junit.Test

/**
 * US03 - list all the Pokémon, to find the one I want.
 * Scenario: docs/architecture/4plus1/scenarios-browse-and-filter.puml.
 * The fixture list has Bulbasaur, Ivysaur, Venusaur, Tauros, Eevee, Vaporeon, Jolteon and Flareon.
 */
@HiltAndroidTest
class BrowseAndFilterSystemTest {

    @get:Rule(order = 0)
    val server = MockWebServerRule()

    @get:Rule(order = 1)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 2)
    val compose = createEmptyComposeRule()

    // Launched inside each test, so a test can make the server fail before the first request.
    private var app: ActivityScenario<MainActivity>? = null

    @After
    fun closeApp() {
        app?.close()
    }

    private fun launchApp() {
        app = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun listShowsEveryPokemonFromTheServer() {
        launchApp()

        compose.waitForText("Bulbasaur")
        compose.onNodeWithText("Tauros").assertIsDisplayed()
        compose.scrollGridTo("Flareon")
        compose.onNodeWithText("Flareon").assertIsDisplayed()
    }

    @Test
    fun searchByNameShowsOnlyMatchingPokemon() {
        launchApp()
        compose.waitForText("Bulbasaur")

        compose.typeInSearch("saur")

        compose.waitForNoText("Tauros")
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("Ivysaur").assertIsDisplayed()
        compose.onNodeWithText("Venusaur").assertIsDisplayed()
        compose.onNodeWithText("Eevee").assertDoesNotExist()
    }

    @Test
    fun filterByTypeShowsOnlyThatType() {
        launchApp()
        compose.waitForText("Bulbasaur")

        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithText("Grass").performClick()
        compose.waitForRequest(server.dispatcher, "/type/grass")
        compose.onNodeWithContentDescription("Close filters").performClick()

        compose.waitForNoText("Tauros")
        compose.onNodeWithText("Bulbasaur").assertIsDisplayed()
        compose.onNodeWithText("Venusaur").assertIsDisplayed()
        compose.onNodeWithText("Eevee").assertDoesNotExist()
    }

    /**
     * Current behaviour: Home has no error screen. When the list request fails it shows the same
     * "No Pokémon to show" message as an empty list. The test waits for the failed request first,
     * because that message is also visible while the list is loading.
     */
    @Test
    fun serverErrorShowsNoPokemonMessage() {
        server.dispatcher.failPath("/pokemon?")
        launchApp()

        compose.waitForRequest(server.dispatcher, "/pokemon?limit=100000&offset=0")
        compose.waitForIdle()

        compose.onNodeWithText("No Pokémon to show").assertIsDisplayed()
        compose.onNodeWithText("Bulbasaur").assertDoesNotExist()
    }
}
