package io.github.rodrigo4sousa.pokedex.presentation.pokedex

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.rodrigo4sousa.pokedex.data.PokemonRepository
import io.github.rodrigo4sousa.pokedex.data.local.CaughtPokemonEntity
import io.github.rodrigo4sousa.pokedex.presentation.components.PokemonSort
import io.github.rodrigo4sousa.pokedex.testutil.FakeCaughtPokemonDao
import io.github.rodrigo4sousa.pokedex.testutil.FakePokemonApi
import io.github.rodrigo4sousa.pokedex.testutil.MainDispatcherRule
import io.github.rodrigo4sousa.pokedex.testutil.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PokedexViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakePokemonApi()
    private val dao = FakeCaughtPokemonDao()

    private fun viewModel() = PokedexViewModel(PokemonRepository(api, dao))

    private fun PokedexViewModel.names() = caughtPokemons.getOrAwaitValue().map { it.name }

    /** Bulbasaur caught last, Eevee first, Tauros in between. */
    private suspend fun catchThree() {
        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 100))
        dao.insert(CaughtPokemonEntity(name = "tauros", pokemonId = 128, caughtAt = 200))
        dao.insert(CaughtPokemonEntity(name = "bulbasaur", pokemonId = 1, caughtAt = 300))
    }

    @Test
    fun `shows only caught Pokemon, in number order, with the count and total`() = runTest {
        catchThree()

        val viewModel = viewModel()

        assertEquals(listOf("bulbasaur", "tauros", "eevee"), viewModel.names())
        assertEquals(3, viewModel.caughtCount.getOrAwaitValue())
        assertEquals(1025, viewModel.total.getOrAwaitValue())
    }

    @Test
    fun `nothing caught gives an empty list and a zero count`() {
        val viewModel = viewModel()

        assertEquals(emptyList<String>(), viewModel.names())
        assertEquals(0, viewModel.caughtCount.getOrAwaitValue())
    }

    @Test
    fun `the total stays empty when the count request fails, and the list still works`() = runTest {
        catchThree()
        api.fail("pokemon-species?")

        val viewModel = viewModel()

        assertNull(viewModel.total.value)
        assertEquals(3, viewModel.names().size)
    }

    @Test
    fun `sorts by date caught (newest first) and by name`() = runTest {
        catchThree()
        val viewModel = viewModel()

        viewModel.onSortChange(PokemonSort.DateCaught)
        assertEquals(listOf("bulbasaur", "tauros", "eevee"), viewModel.names())

        viewModel.onSortChange(PokemonSort.Name)
        assertEquals(listOf("bulbasaur", "eevee", "tauros"), viewModel.names())
    }

    @Test
    fun `search and type filter only look at caught Pokemon`() = runTest {
        catchThree()
        val viewModel = viewModel()

        viewModel.onQueryChange("A")
        assertEquals(listOf("bulbasaur", "tauros"), viewModel.names())

        viewModel.onQueryChange("")
        viewModel.toggleType("normal")
        assertEquals(listOf("tauros", "eevee"), viewModel.names())
    }

    @Test
    fun `catching elsewhere updates the list`() = runTest {
        val viewModel = viewModel()
        assertEquals(emptyList<String>(), viewModel.names())

        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133))

        assertEquals(listOf("eevee"), viewModel.names())
        assertEquals(1, viewModel.caughtCount.getOrAwaitValue())
    }

    @Test
    fun `releasing removes it from the list`() = runTest {
        catchThree()
        val viewModel = viewModel()
        val tauros = viewModel.caughtPokemons.getOrAwaitValue().single { it.name == "tauros" }

        viewModel.release(tauros)

        assertEquals(listOf("bulbasaur", "eevee"), viewModel.names())
        assertEquals(2, viewModel.caughtCount.getOrAwaitValue())
    }
}
