package io.github.rodrigo4sousa.pokedex.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.rodrigo4sousa.pokedex.data.local.CaughtPokemonEntity
import io.github.rodrigo4sousa.pokedex.testutil.FakeCaughtPokemonDao
import io.github.rodrigo4sousa.pokedex.testutil.FakePokemonApi
import io.github.rodrigo4sousa.pokedex.testutil.getOrAwaitValue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class PokemonRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val api = FakePokemonApi()
    private val dao = FakeCaughtPokemonDao()
    private val repository = PokemonRepository(api, dao)

    // --- Network ---

    @Test
    fun `the full list comes from a single request and keeps PokeAPI's order`() = runTest {
        val all = repository.getAllPokemons()

        assertEquals(listOf("pokemon?limit=100000&offset=0"), api.requests)
        assertEquals(listOf(1, 2, 3, 128, 133, 134, 135, 136), all.map { it.id })
        assertEquals("bulbasaur", all.first().name)
    }

    @Test
    fun `a Pokemon is returned as a domain model`() = runTest {
        val tauros = repository.getPokemon("tauros")

        assertEquals(128, tauros.id)
        assertEquals(listOf("normal"), tauros.types)
    }

    @Test
    fun `the evolution chain is found through the species`() = runTest {
        val chain = repository.getEvolutionChain("bulbasaur")

        assertEquals(listOf("pokemon-species/bulbasaur", "evolution-chain/1"), api.requests)
        assertNotNull(chain)
        assertEquals("ivysaur", chain!!.evolvesTo.single().pokemon.name)
    }

    @Test
    fun `type matchups combine every type of the Pokemon`() = runTest {
        val multipliers = repository.getTypeEffectiveness(listOf("grass", "poison"))

        assertEquals(setOf("type/grass", "type/poison"), api.requests.toSet())
        assertEquals(0.25, multipliers.getValue("grass"), 0.0)
        assertEquals(2.0, multipliers.getValue("fire"), 0.0)
    }

    @Test
    fun `the members of a type are fetched once and then cached`() = runTest {
        val first = repository.getPokemonNamesOfType("grass")
        val second = repository.getPokemonNamesOfType("grass")

        assertEquals(listOf("type/grass"), api.requests)
        assertEquals(first, second)
        assertTrue("bulbasaur" in first)
    }

    @Test
    fun `the species list has one entry per species, named after it`() = runTest {
        val species = repository.getAllSpecies()

        assertEquals(listOf("pokemon-species?limit=100000&offset=0"), api.requests)
        assertEquals(listOf(1, 2, 3, 128, 133, 134, 135, 136, 487), species.map { it.id })
        assertEquals("giratina", species.last().name)
    }

    @Test
    fun `a species opens as its default Pokemon, whose name can carry a form suffix`() = runTest {
        val giratina = repository.getDefaultPokemon("giratina")

        assertEquals(listOf("pokemon-species/giratina", "pokemon/giratina-altered"), api.requests)
        assertEquals("giratina-altered", giratina.name)
        assertEquals("giratina", giratina.speciesName)
        assertEquals(487, giratina.id)
    }

    @Test
    fun `the species total comes from the count endpoint`() = runTest {
        assertEquals(1025, repository.getSpeciesCount())
    }

    @Test
    fun `API errors reach the caller unchanged`() = runTest {
        api.fail("pokemon/", IOException("offline"))

        val error = runCatching { repository.getPokemon("bulbasaur") }.exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals("offline", error!!.message)
    }

    @Test
    fun `an unknown Pokemon fails like a 404`() = runTest {
        val error = runCatching { repository.getPokemon("missingno") }.exceptionOrNull()

        assertTrue(error is IOException)
    }

    // --- Caught Pokémon (Room) ---

    @Test
    fun `catching writes through the DAO and shows up as caught`() = runTest {
        repository.catchPokemon(name = "bulbasaur", id = 1)

        val caught = repository.getCaught().getOrAwaitValue()
        assertEquals(listOf("bulbasaur"), caught.map { it.pokemon.name })
        assertEquals(1, caught.single().pokemon.id)
        assertTrue(repository.isCaught("bulbasaur").getOrAwaitValue())
        assertFalse(repository.isCaught("ivysaur").getOrAwaitValue())
    }

    @Test
    fun `releasing removes it`() = runTest {
        repository.catchPokemon(name = "bulbasaur", id = 1)

        repository.releasePokemon("bulbasaur")

        assertEquals(emptyList<Any>(), repository.getCaught().getOrAwaitValue())
        assertFalse(repository.isCaught("bulbasaur").getOrAwaitValue())
    }

    @Test
    fun `caught Pokemon come ordered by number with the time they were caught`() = runTest {
        dao.insert(CaughtPokemonEntity(name = "tauros", pokemonId = 128, caughtAt = 200))
        dao.insert(CaughtPokemonEntity(name = "bulbasaur", pokemonId = 1, caughtAt = 300))

        val caught = repository.getCaught().getOrAwaitValue()

        assertEquals(listOf(1, 128), caught.map { it.pokemon.id })
        assertEquals(listOf(300L, 200L), caught.map { it.caughtAt })
    }

    @Test
    fun `catching the same Pokemon twice keeps the first catch`() = runTest {
        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 100))

        repository.catchPokemon(name = "eevee", id = 133)

        assertEquals(listOf(100L), repository.getCaught().getOrAwaitValue().map { it.caughtAt })
    }
}
