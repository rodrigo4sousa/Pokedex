package io.github.rodrigo4sousa.pokedex.domain.model

/** A Pokémon the user caught, and when (epoch millis). */
data class CaughtPokemon(
    val pokemon: PokemonSummary,
    val caughtAt: Long
)
