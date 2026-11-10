package io.github.rodrigo4sousa.pokedex.system

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import io.github.rodrigo4sousa.pokedex.MainActivity
import io.github.rodrigo4sousa.pokedex.testutil.MockWebServerRule
import io.github.rodrigo4sousa.pokedex.testutil.scrollGridTo
import io.github.rodrigo4sousa.pokedex.testutil.waitForRequest
import io.github.rodrigo4sousa.pokedex.testutil.waitForText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * US04 - inspect a Pokémon, to better understand its attributes.
 * Scenario: docs/architecture/4plus1/scenarios-view-detail.puml.
 * Bulbasaur has a linear chain, Eevee a branching one, Tauros none.
 */
@HiltAndroidTest
class ViewDetailSystemTest {

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
        compose.waitForText("Bulbasaur")
    }

    @After
    fun closeApp() {
        app.close()
    }

    private fun openDetail(pokemon: String) {
        compose.scrollGridTo(pokemon)
        compose.onNodeWithText(pokemon).performClick()
    }

    @Test
    fun bulbasaurShowsStatsTypeMatchupsAndEvolutionChain() {
        openDetail("Bulbasaur")

        compose.waitForText("#001")
        compose.onNodeWithText("Grass").assertExists()
        compose.onNodeWithText("Poison").assertExists()
        compose.onNodeWithText("Base stats").assertExists()
        compose.onNodeWithText("HP").assertExists()

        compose.waitForText("Fire 2×")
        compose.onNodeWithText("Psychic 2×").assertExists()
        compose.onNodeWithText("Grass ¼×").assertExists()

        compose.waitForText("Lv. 16")
        compose.onNodeWithText("Lv. 32").assertExists()
        compose.onNodeWithText("Ivysaur").assertExists()
        compose.onNodeWithText("Venusaur").assertExists()
    }

    @Test
    fun eeveeShowsEveryBranchOfItsEvolution() {
        openDetail("Eevee")

        compose.waitForText("#133")
        listOf("Vaporeon", "Jolteon", "Flareon", "Espeon", "Umbreon", "Leafeon", "Glaceon", "Sylveon")
            .forEach { compose.waitForText(it) }
    }

    @Test
    fun taurosHasNoEvolutionSection() {
        openDetail("Tauros")

        compose.waitForText("#128")
        compose.waitForText("Fighting 2×")
        compose.onNodeWithText("Ghost 0×").assertExists()

        // The chain is requested even for Pokémon that don't evolve; the section stays hidden.
        compose.waitForRequest(server.dispatcher, "/evolution-chain/63/")
        compose.waitForIdle()
        compose.onNodeWithText("Evolution").assertDoesNotExist()
    }
}
