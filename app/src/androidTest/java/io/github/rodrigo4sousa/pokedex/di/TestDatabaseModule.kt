package io.github.rodrigo4sousa.pokedex.di

import android.content.Context
import androidx.room.Room
import io.github.rodrigo4sousa.pokedex.data.local.CaughtPokemonDao
import io.github.rodrigo4sousa.pokedex.data.local.DatabaseModule
import io.github.rodrigo4sousa.pokedex.data.local.GuessGameDao
import io.github.rodrigo4sousa.pokedex.data.local.PokedexDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import jakarta.inject.Singleton

/**
 * Replaces DatabaseModule in instrumented tests with an in-memory Room database. Hilt builds a new
 * component for every test, so each test starts with no caught Pokémon and never touches pokedex.db.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun providePokedexDatabase(@ApplicationContext context: Context): PokedexDatabase =
        Room.inMemoryDatabaseBuilder(context, PokedexDatabase::class.java).build()

    @Provides
    fun provideCaughtPokemonDao(database: PokedexDatabase): CaughtPokemonDao =
        database.caughtPokemonDao()

    @Provides
    fun provideGuessGameDao(database: PokedexDatabase): GuessGameDao =
        database.guessGameDao()
}
