package io.github.rodrigo4sousa.pokedex.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.rodrigo4sousa.pokedex.testutil.awaitValue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migrations against the exported schemas (app/schemas, packaged as androidTest assets): a database
 * built from an old schema is migrated, validated against the new one, then opened with Room, which
 * also checks the schema's identity hash against the current entities.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PokedexDatabase::class.java
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    @After
    fun deleteTestDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migration1To2KeepsCaughtPokemonAndAddsTheGameTables() = runTest {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL("INSERT INTO caught_pokemon (name, pokemonId, caughtAt) VALUES ('eevee', 133, 100)")
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2).close()

        val database = Room.databaseBuilder(context, PokedexDatabase::class.java, TEST_DB)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            val rows = database.caughtPokemonDao().getAll().awaitValue { it.isNotEmpty() }
            assertEquals(listOf(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 100)), rows)

            val gameDao = database.guessGameDao()
            assertNull(gameDao.getStreak())
            val round = DailyRoundEntity(20_382, "giratina", 487, "", 3, "Playing")
            gameDao.saveRound(round)
            assertEquals(round, gameDao.getRound(20_382))
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
