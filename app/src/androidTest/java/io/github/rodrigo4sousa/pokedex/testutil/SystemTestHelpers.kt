@file:OptIn(ExperimentalTestApi::class)

package io.github.rodrigo4sousa.pokedex.testutil

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import io.github.rodrigo4sousa.pokedex.testing.PokeApiDispatcher

/** How long system tests wait for something to appear: covers the mock server round trip. */
const val UI_TIMEOUT_MS = 10_000L

fun ComposeTestRule.waitForText(text: String, substring: Boolean = false) =
    waitUntilAtLeastOneExists(hasText(text, substring = substring), UI_TIMEOUT_MS)

fun ComposeTestRule.waitForNoText(text: String) =
    waitUntilDoesNotExist(hasText(text), UI_TIMEOUT_MS)

/** Waits until the mock server has received a request for exactly [path] (e.g. "/type/grass"). */
fun ComposeTestRule.waitForRequest(dispatcher: PokeApiDispatcher, path: String) =
    waitUntil(UI_TIMEOUT_MS) { path in dispatcher.requests }

/**
 * The catch star on a Pokémon's card in a grid. Its description is "Catch" when the Pokémon is not
 * caught and "Release" when it is; its parent in the semantics tree is the card, which has the name.
 */
fun starOnCard(pokemon: String, caught: Boolean): SemanticsMatcher =
    hasContentDescription(if (caught) "Release" else "Catch") and hasParent(hasText(pokemon))

/** Bottom bar tab ("Home" or "Pokédex"); the screen titles have the same text but no click action. */
fun ComposeTestRule.openTab(label: String) =
    onNode(hasText(label) and hasClickAction()).performClick()

/** Scrolls the visible grid until the card with [pokemon] is composed. */
fun ComposeTestRule.scrollGridTo(pokemon: String) =
    onNode(hasScrollToNodeAction()).performScrollToNode(hasText(pokemon))

fun ComposeTestRule.typeInSearch(text: String) =
    onNode(hasSetTextAction()).performTextInput(text)

/** Waits for [pokemon]'s card on the current grid and taps its star to catch it. */
fun ComposeTestRule.catchFromGrid(pokemon: String) {
    waitForText(pokemon)
    onNode(starOnCard(pokemon, caught = false)).performClick()
    waitUntilExactlyOneExists(starOnCard(pokemon, caught = true), UI_TIMEOUT_MS)
}
