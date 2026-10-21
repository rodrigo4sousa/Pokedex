package com.example.pokedex.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePokedexDatabase(@ApplicationContext context: Context): PokedexDatabase =
        Room.databaseBuilder(context, PokedexDatabase::class.java, "pokedex.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCaughtPokemonDao(database: PokedexDatabase): CaughtPokemonDao =
        database.caughtPokemonDao()

    @Provides
    fun provideGuessGameDao(database: PokedexDatabase): GuessGameDao =
        database.guessGameDao()
}
