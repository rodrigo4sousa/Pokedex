package com.example.pokedex.data.remote

import com.example.pokedex.testing.PokeApiDispatcher
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * PokemonApi with the app's real Retrofit setup (pokeApiRetrofit: Gson converter) against MockWebServer
 * serving the recorded fixtures. Checks the requests the app sends and that every fixture parses.
 */
class PokemonApiIntegrationTest {

    private val dispatcher = PokeApiDispatcher()
    private val server = MockWebServer()
    private lateinit var api: PokemonApi

    @Before
    fun startServer() {
        server.dispatcher = dispatcher
        server.start()
        api = pokeApiRetrofit(server.url("/").toString(), OkHttpClient()).create(PokemonApi::class.java)
    }

    @After
    fun stopServer() {
        server.shutdown()
    }

    private fun takePath(): String = requireNotNull(server.takeRequest(1, TimeUnit.SECONDS)).path!!

    // --- Requests ---

    @Test
    fun `getPokemon requests pokemon by name`() = runTest {
        val bulbasaur = api.getPokemon("bulbasaur")

        assertEquals("/pokemon/bulbasaur", takePath())
        assertEquals(1, bulbasaur.id)
    }

    @Test
    fun `getPokemonPage sends limit and offset`() = runTest {
        val page = api.getPokemonPage(limit = 100000, offset = 0)

        val request = requireNotNull(server.takeRequest(1, TimeUnit.SECONDS))
        assertEquals("/pokemon", request.requestUrl!!.encodedPath)
        assertEquals("100000", request.requestUrl!!.queryParameter("limit"))
        assertEquals("0", request.requestUrl!!.queryParameter("offset"))
        assertEquals(8, page.results.size)
        assertEquals(null, page.next)
    }

    @Test
    fun `getPokemonSpecies requests the species and reads the chain link`() = runTest {
        val species = api.getPokemonSpecies("eevee")

        assertEquals("/pokemon-species/eevee", takePath())
        assertTrue(species.evolution_chain!!.url.endsWith("/evolution-chain/67/"))
    }

    @Test
    fun `getEvolutionChain calls the absolute url it is given`() = runTest {
        val chain = api.getEvolutionChain(server.url("/evolution-chain/67/").toString())

        assertEquals("/evolution-chain/67/", takePath())
        assertEquals("eevee", chain.chain.species.name)
    }

    @Test
    fun `getType requests the type by name`() = runTest {
        val grass = api.getType("grass")

        assertEquals("/type/grass", takePath())
        assertTrue(grass.damage_relations.double_damage_from.any { it.name == "fire" })
        assertTrue(grass.pokemon.any { it.pokemon.name == "bulbasaur" })
    }

    @Test
    fun `getPokemonSpeciesPage sends limit and offset and reads species names`() = runTest {
        val page = api.getPokemonSpeciesPage(limit = 100000, offset = 0)

        val request = requireNotNull(server.takeRequest(1, TimeUnit.SECONDS))
        assertEquals("/pokemon-species", request.requestUrl!!.encodedPath)
        assertEquals("100000", request.requestUrl!!.queryParameter("limit"))
        assertEquals("0", request.requestUrl!!.queryParameter("offset"))
        assertTrue(page.results.any { it.name == "giratina" })
    }

    @Test
    fun `getPokemonSpecies reads the default variety`() = runTest {
        val species = api.getPokemonSpecies("giratina")

        assertEquals("giratina-altered", species.varieties.single { it.is_default }.pokemon.name)
    }

    @Test
    fun `getPokemonSpeciesCount asks for a single result`() = runTest {
        val count = api.getPokemonSpeciesCount()

        assertEquals("/pokemon-species?limit=1", takePath())
        assertEquals(1025, count.count)
    }

    // --- Every fixture parses ---

    @Test
    fun `every recorded fixture parses into the DTOs`() = runTest {
        for (name in listOf("bulbasaur", "eevee", "tauros")) {
            val pokemon = api.getPokemon(name)
            assertEquals(name, pokemon.name)
            assertTrue(pokemon.types.isNotEmpty() && pokemon.stats.size == 6 && pokemon.abilities.isNotEmpty())
            assertNotNull(pokemon.sprites.front_default)
            assertNotNull(pokemon.cries?.latest)

            val species = api.getPokemonSpecies(name)
            val chain = api.getEvolutionChain(server.url(species.evolution_chain!!.url.substringAfter("/api/v2")).toString())
            assertNotNull(chain.chain.species.name)
        }
        for (type in listOf("grass", "poison", "normal")) {
            val dto = api.getType(type)
            assertTrue(dto.pokemon.isNotEmpty())
            assertNotNull(dto.damage_relations.double_damage_from)
        }
        assertEquals(8, api.getPokemonPage(100000, 0).results.size)
        assertEquals(1025, api.getPokemonSpeciesCount().count)
    }

    // --- Errors ---

    @Test
    fun `a 404 throws HttpException with the code`() = runTest {
        val error = runCatching { api.getPokemon("missingno") }.exceptionOrNull()

        assertTrue("was $error", error is HttpException)
        assertEquals(404, (error as HttpException).code())
    }

    @Test
    fun `a 500 throws HttpException with the code`() = runTest {
        dispatcher.failPath("/pokemon/")

        val error = runCatching { api.getPokemon("bulbasaur") }.exceptionOrNull()

        assertTrue("was $error", error is HttpException)
        assertEquals(500, (error as HttpException).code())
    }

    @Test
    fun `malformed JSON throws while parsing`() = runTest {
        server.dispatcher = QueueDispatcher().apply {
            enqueueResponse(MockResponse().setBody("<html>Service unavailable</html>"))
        }

        val error = runCatching { api.getPokemon("bulbasaur") }.exceptionOrNull()

        // Gson's strict reader rejects it; it is an IOException, so callers handle it like a network error.
        assertTrue("was $error", error is MalformedJsonException)
        assertTrue(error is IOException)
    }
}
