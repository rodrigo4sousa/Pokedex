package com.example.pokedex.data.remote.dto

// GET pokemon-species/{name}. Only the evolution chain link is used.
data class PokemonSpeciesDto(
    val evolution_chain: ApiResourceDto?
)
