package com.example.pokedex.presentation.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.presentation.components.PokemonSort
import com.example.pokedex.testutil.FakeCaughtPokemonDao
import com.example.pokedex.testutil.FakePokemonApi
import com.example.pokedex.testutil.MainDispatcherRule
import com.example.pokedex.testutil.getOrAwaitValue
import com.example.pokedex.testutil.observeForTesting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakePokemonApi()
    private val dao = FakeCaughtPokemonDao()

    private fun viewModel() = HomeViewModel(PokemonRepository(api, dao))

    private fun HomeViewModel.names() = pokemons.getOrAwaitValue().map { it.name }

    @Test
    fun `the initial load shows every Pokemon from the server in number order`() {
        val viewModel = viewModel()

        assertEquals(
            listOf("bulbasaur", "ivysaur", "venusaur", "tauros", "eevee", "vaporeon", "jolteon", "flareon"),
            viewModel.names()
        )
        assertNull(viewModel.error.value)
    }

    @Test
    fun `a failed load leaves the list empty and sets the error`() {
        api.fail("pokemon?")

        val viewModel = viewModel()

        assertEquals(emptyList<String>(), viewModel.names())
        assertEquals("Failed to load Pokémon", viewModel.error.getOrAwaitValue())
    }

    @Test
    fun `search filters by name, ignoring case and spaces`() {
        val viewModel = viewModel()

        viewModel.onQueryChange("  SAUR ")

        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur"), viewModel.names())
    }

    @Test
    fun `sorting by name orders alphabetically, and back to number`() {
        val viewModel = viewModel()

        viewModel.onSortChange(PokemonSort.Name)
        assertEquals(
            listOf("bulbasaur", "eevee", "flareon", "ivysaur", "jolteon", "tauros", "vaporeon", "venusaur"),
            viewModel.names()
        )

        viewModel.onSortChange(PokemonSort.Number)
        assertEquals("bulbasaur", viewModel.names().first())
        assertEquals("flareon", viewModel.names().last())
    }

    @Test
    fun `the type filter keeps only Pokemon of that type, and clearing brings all back`() {
        val viewModel = viewModel()

        viewModel.toggleType("grass")
        assertEquals(setOf("grass"), viewModel.selectedTypes.getOrAwaitValue())
        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur"), viewModel.names())

        viewModel.clearTypes()
        assertEquals(8, viewModel.names().size)
    }

    @Test
    fun `selected types combine with OR`() {
        val viewModel = viewModel()

        viewModel.toggleType("poison")
        viewModel.toggleType("normal")

        assertEquals(listOf("bulbasaur", "ivysaur", "venusaur", "tauros", "eevee"), viewModel.names())
    }

    @Test
    fun `search and type filter apply together`() {
        val viewModel = viewModel()

        viewModel.toggleType("normal")
        viewModel.onQueryChange("tau")

        assertEquals(listOf("tauros"), viewModel.names())
    }

    @Test
    fun `a failed type request unselects the type and sets the error`() {
        api.fail("type/")
        val viewModel = viewModel()

        viewModel.toggleType("grass")

        assertEquals(emptySet<String>(), viewModel.selectedTypes.getOrAwaitValue())
        assertEquals(8, viewModel.names().size)
        assertEquals("Failed to load grass Pokémon", viewModel.error.getOrAwaitValue())
    }

    @Test
    fun `toggling the star catches and then releases`() {
        val viewModel = viewModel()
        val bulbasaur = viewModel.pokemons.getOrAwaitValue().first()

        viewModel.caughtNames.observeForTesting {
            viewModel.toggleCaught(bulbasaur)
            assertEquals(setOf("bulbasaur"), viewModel.caughtNames.getOrAwaitValue())

            viewModel.toggleCaught(bulbasaur)
            assertTrue(viewModel.caughtNames.getOrAwaitValue().isEmpty())
        }
    }
}
