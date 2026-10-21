package com.example.pokedex.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pokedex.testutil.awaitValue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** CaughtPokemonDao against a real (in-memory) Room database. */
@RunWith(AndroidJUnit4::class)
class CaughtPokemonDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PokedexDatabase
    private lateinit var dao: CaughtPokemonDao

    @Before
    fun openDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PokedexDatabase::class.java
        ).build()
        dao = database.caughtPokemonDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertedRowsComeBackOrderedByNumber() = runTest {
        dao.insert(CaughtPokemonEntity(name = "tauros", pokemonId = 128, caughtAt = 200))
        dao.insert(CaughtPokemonEntity(name = "bulbasaur", pokemonId = 1, caughtAt = 300))

        val rows = dao.getAll().awaitValue { it.size == 2 }

        assertEquals(listOf("bulbasaur", "tauros"), rows.map { it.name })
        assertEquals(listOf(300L, 200L), rows.map { it.caughtAt })
    }

    @Test
    fun deleteRemovesOnlyThatPokemon() = runTest {
        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133))
        dao.insert(CaughtPokemonEntity(name = "tauros", pokemonId = 128))

        dao.delete("eevee")

        assertEquals(listOf("tauros"), dao.getAll().awaitValue { it.size == 1 }.map { it.name })
    }

    @Test
    fun catchingTheSameNameTwiceKeepsTheFirstRow() = runTest {
        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 100))

        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 999))

        val rows = dao.getAll().awaitValue { it.isNotEmpty() }
        assertEquals(1, rows.size)
        assertEquals(100L, rows.single().caughtAt)
    }

    @Test
    fun isCaughtFollowsInsertAndDelete() = runTest {
        val isCaught = dao.isCaught("eevee")
        assertFalse(isCaught.awaitValue())

        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133))
        assertTrue(isCaught.awaitValue { it })

        dao.delete("eevee")
        assertFalse(isCaught.awaitValue { !it })
    }

    @Test
    fun observersAreNotifiedOnEveryChange() = runTest {
        val all = dao.getAll()
        assertEquals(emptyList<CaughtPokemonEntity>(), all.awaitValue())

        dao.insert(CaughtPokemonEntity(name = "eevee", pokemonId = 133))
        assertEquals(listOf("eevee"), all.awaitValue { it.size == 1 }.map { it.name })

        dao.insert(CaughtPokemonEntity(name = "bulbasaur", pokemonId = 1))
        assertEquals(listOf("bulbasaur", "eevee"), all.awaitValue { it.size == 2 }.map { it.name })

        dao.delete("bulbasaur")
        assertEquals(listOf("eevee"), all.awaitValue { it.size == 1 }.map { it.name })
    }
}
