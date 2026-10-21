package com.example.pokedex.presentation.pokemonDetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.testutil.FakeCaughtPokemonDao
import com.example.pokedex.testutil.FakePokemonApi
import com.example.pokedex.testutil.MainDispatcherRule
import com.example.pokedex.testutil.getOrAwaitValue
import com.example.pokedex.testutil.observeForTesting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PokemonDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakePokemonApi()
    private val dao = FakeCaughtPokemonDao()

    /** The route argument is the Pokémon's name, as Navigation passes it. */
    private fun viewModel(name: String) =
        PokemonDetailViewModel(SavedStateHandle(mapOf("name" to name)), PokemonRepository(api, dao))

    @Test
    fun `loads the Pokemon, its evolution chain and its type matchups`() {
        val viewModel = viewModel("bulbasaur")

        assertEquals("bulbasaur", viewModel.pokemon.getOrAwaitValue().name)
        val chain = viewModel.evolutionChain.getOrAwaitValue()
        assertEquals(listOf("ivysaur"), chain?.evolvesTo?.map { it.pokemon.name })
        assertEquals(2.0, viewModel.typeEffectiveness.getOrAwaitValue().getValue("fire"), 0.0)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `a Pokemon that does not evolve gets a single-stage chain`() {
        val viewModel = viewModel("tauros")

        val chain = viewModel.evolutionChain.getOrAwaitValue()
        assertEquals("tauros", chain?.pokemon?.name)
        assertTrue(chain!!.evolvesTo.isEmpty())
    }

    @Test
    fun `an unknown Pokemon sets the error and nothing else loads`() {
        val viewModel = viewModel("missingno")

        assertEquals("No Pokemon found!", viewModel.error.getOrAwaitValue())
        assertNull(viewModel.pokemon.value)
        assertEquals(listOf("pokemon/missingno"), api.requests)
    }

    @Test
    fun `a failing extra section stays empty while the rest loads`() {
        api.fail("type/")

        val viewModel = viewModel("eevee")

        assertEquals("eevee", viewModel.pokemon.getOrAwaitValue().name)
        assertEquals(8, viewModel.evolutionChain.getOrAwaitValue()?.evolvesTo?.size)
        assertNull(viewModel.typeEffectiveness.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `toggleCaught catches and then releases`() {
        val viewModel = viewModel("eevee")

        viewModel.isCaught.observeForTesting {
            assertFalse(viewModel.isCaught.getOrAwaitValue())

            viewModel.toggleCaught()
            assertTrue(viewModel.isCaught.getOrAwaitValue())

            viewModel.toggleCaught()
            assertFalse(viewModel.isCaught.getOrAwaitValue())
        }
    }
}
