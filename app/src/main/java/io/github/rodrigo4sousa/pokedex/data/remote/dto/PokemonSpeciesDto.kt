package io.github.rodrigo4sousa.pokedex.data.remote.dto

// GET pokemon-species/{name}. Only the evolution chain link and the varieties are used.
data class PokemonSpeciesDto(
    val evolution_chain: ApiResourceDto?,
    val varieties: List<PokemonVarietyDto>
)

// One Pokémon of the species; the default one is what pokemon/{name} returns for the species itself.
data class PokemonVarietyDto(
    val is_default: Boolean,
    val pokemon: NamedResourceDto
)
