package io.github.rodrigo4sousa.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caught_pokemon")
data class CaughtPokemonEntity(
    @PrimaryKey val name: String,
    val pokemonId: Int,
    val caughtAt: Long = System.currentTimeMillis()
)
