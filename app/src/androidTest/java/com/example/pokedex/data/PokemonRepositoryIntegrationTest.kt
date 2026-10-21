package com.example.pokedex.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pokedex.data.local.PokedexDatabase
import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.data.remote.pokeApiRetrofit
import com.example.pokedex.testing.PokeApiDispatcher
import com.example.pokedex.testutil.awaitValue
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException

/**
 * PokemonRepository with its real collaborators: Room (in memory) and Retrofit against MockWebServer
 * serving the recorded fixtures. No Hilt: the objects are built here, the way the modules build them.
 */
@RunWith(AndroidJUnit4::class)
class PokemonRepositoryIntegrationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = PokeApiDispatcher()
    private val server = MockWebServer()
    private lateinit var database: PokedexDatabase
    private lateinit var repository: PokemonRepository

    @Before
    fun setUp() {
        server.dispatcher = dispatcher
        server.start()
        val api = pokeApiRetrofit(server.url("/").toString(), OkHttpClient()).create(PokemonApi::class.java)
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PokedexDatabase::class.java
        ).build()
        repository = PokemonRepository(api, database.caughtPokemonDao())
    }

    @After
    fun tearDown() {
        database.close()
        server.shutdown()
    }

    @Test
    fun catchingAPokemonFromTheListMakesItAppearInMyPokedex() = runTest {
        val bulbasaur = repository.getAllPokemons().first { it.name == "bulbasaur" }

        repository.catchPokemon(bulbasaur.name, bulbasaur.id)

        val caught = repository.getCaught().awaitValue { it.isNotEmpty() }
        assertEquals(listOf(bulbasaur), caught.map { it.pokemon })
        assertTrue(repository.isCaught("bulbasaur").awaitValue { it })
    }

    @Test
    fun catchingFromTheDetailUsesTheNumberFromTheServer() = runTest {
        val tauros = repository.getPokemon("tauros")

        repository.catchPokemon(tauros.name, tauros.id)

        assertEquals(128, repository.getCaught().awaitValue { it.isNotEmpty() }.single().pokemon.id)
    }

    @Test
    fun myPokedexStillLoadsWhileTheServerFails() = runTest {
        repository.catchPokemon("eevee", 133)
        dispatcher.failPath("/")

        // The caught list only reads Room...
        assertEquals(listOf("eevee"), repository.getCaught().awaitValue { it.isNotEmpty() }.map { it.pokemon.name })
        // ...while anything that needs the server fails.
        val error = runCatching { repository.getSpeciesCount() }.exceptionOrNull()
        assertTrue("was $error", error is HttpException && error.code() == 500)
    }
}
