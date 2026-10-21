package com.example.pokedex.data.mapper

import com.example.pokedex.data.local.CaughtPokemonEntity
import com.example.pokedex.domain.model.CaughtPokemon
import com.example.pokedex.domain.model.PokemonSummary

fun CaughtPokemonEntity.toDomain(): CaughtPokemon = CaughtPokemon(
    pokemon = PokemonSummary(id = pokemonId, name = name, spriteUrl = spriteUrlFor(pokemonId)),
    caughtAt = caughtAt
)

/** The row stored when a Pokémon is caught; caughtAt defaults to now. */
fun caughtPokemonEntity(name: String, id: Int): CaughtPokemonEntity =
    CaughtPokemonEntity(name = name, pokemonId = id)
