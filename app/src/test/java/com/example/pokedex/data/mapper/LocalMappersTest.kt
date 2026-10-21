package com.example.pokedex.data.mapper

import com.example.pokedex.data.local.CaughtPokemonEntity
import com.example.pokedex.domain.model.CaughtPokemon
import com.example.pokedex.domain.model.PokemonSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMappersTest {

    @Test
    fun `a caught row becomes a caught Pokemon with its card`() {
        val row = CaughtPokemonEntity(name = "eevee", pokemonId = 133, caughtAt = 1_760_000_000_000)

        assertEquals(
            CaughtPokemon(
                pokemon = PokemonSummary(
                    id = 133,
                    name = "eevee",
                    spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/133.png"
                ),
                caughtAt = 1_760_000_000_000
            ),
            row.toDomain()
        )
    }

    @Test
    fun `a new row is stamped with the current time`() {
        val before = System.currentTimeMillis()

        val row = caughtPokemonEntity(name = "tauros", id = 128)

        assertEquals("tauros", row.name)
        assertEquals(128, row.pokemonId)
        assertTrue(row.caughtAt in before..System.currentTimeMillis())
    }
}
