package com.example.pokedex.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CaughtPokemonEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PokedexDatabase : RoomDatabase() {

    abstract fun caughtPokemonDao(): CaughtPokemonDao
}
