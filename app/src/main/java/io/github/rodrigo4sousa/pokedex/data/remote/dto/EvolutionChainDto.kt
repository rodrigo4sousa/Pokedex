package io.github.rodrigo4sousa.pokedex.data.remote.dto

// GET evolution-chain/{id} (full URL taken from the species response).
data class EvolutionChainDto(
    val chain: ChainLinkDto
)

// One species in an evolution chain. evolves_to can branch (Eevee has eight).
data class ChainLinkDto(
    val species: NamedResourceDto,
    val evolution_details: List<EvolutionDetailDto>,
    val evolves_to: List<ChainLinkDto>
)

// What it takes to evolve into a species. PokeAPI sends more fields; these are the ones shown.
data class EvolutionDetailDto(
    val is_default: Boolean?,
    val trigger: NamedResourceDto?,
    val min_level: Int?,
    val item: NamedResourceDto?,
    val held_item: NamedResourceDto?,
    val known_move: NamedResourceDto?,
    val known_move_type: NamedResourceDto?,
    val min_happiness: Int?,
    val min_affection: Int?,
    val location: NamedResourceDto?,
    val time_of_day: String?,
    val gender: Int?,
    val trade_species: NamedResourceDto?
)
