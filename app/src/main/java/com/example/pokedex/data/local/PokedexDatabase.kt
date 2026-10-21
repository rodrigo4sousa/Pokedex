package com.example.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CaughtPokemonEntity::class, DailyRoundEntity::class, StreakEntity::class],
    version = 2,
    exportSchema = true
)
abstract class PokedexDatabase : RoomDatabase() {

    abstract fun caughtPokemonDao(): CaughtPokemonDao

    abstract fun guessGameDao(): GuessGameDao
}
