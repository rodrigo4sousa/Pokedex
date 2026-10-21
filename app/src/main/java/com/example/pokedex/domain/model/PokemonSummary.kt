package com.example.pokedex.domain.model

/** A Pokémon as shown in a list or grid: enough to draw its card and open its detail. */
data class PokemonSummary(
    val id: Int,
    val name: String,
    val spriteUrl: String
)
