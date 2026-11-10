package io.github.rodrigo4sousa.pokedex.data.remote.dto

// GET pokemon?limit&offset: one page of the list; results are name + URL only.
data class PokemonPageDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NamedResourceDto>
)
