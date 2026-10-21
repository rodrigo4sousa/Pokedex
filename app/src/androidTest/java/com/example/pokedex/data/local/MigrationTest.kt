package com.example.pokedex.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pokedex.testutil.awaitValue
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration harness. There is only schema v1 today, so this proves the setup works: the exported schema
 * (app/schemas, packaged as androidTest assets) builds a v1 database that Room opens with the current
 * entities. When v2 comes: add the Migration, a test that runs it with runMigrationsAndValidate, and
 * commit 2.json (see docs/testing.md).
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
    fun version1FromTheExportedSchemaOpensWithTheCurrentEntities() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL("INSERT INTO caught_pokemon (name, pokemonId, caughtAt) VALUES ('eevee', 133, 100)")
        }
        helper.runMigrationsAndValidate(TEST_DB, 1, true).close()

        // Room checks the schema's identity hash against the entities when it opens the file.
        val database = Room.databaseBuilder(context, PokedexDatabase::class.java, TEST_DB).build()
        try {
            val rows = database.caughtPokemonDao().getAll().awaitValue { it.isNotEmpty() }
            assertEquals(listOf(CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 100)), rows)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
