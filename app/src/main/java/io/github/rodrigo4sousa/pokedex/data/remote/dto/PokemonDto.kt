package io.github.rodrigo4sousa.pokedex.data.remote.dto

import com.google.gson.annotations.SerializedName

// GET pokemon/{name}. PokeAPI sends more fields (moves, game_indices, ...); these are the ones the app reads.
data class PokemonDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val abilities: List<AbilitySlotDto>,
    val types: List<TypeSlotDto>,
    val sprites: SpritesDto,
    val stats: List<StatDto>,
    val cries: CriesDto?,
    val species: NamedResourceDto
)

// { "ability": { "name": "overgrow", "url": "..." }, "is_hidden": false, "slot": 1 }
data class AbilitySlotDto(
    val ability: NamedResourceDto,
    val is_hidden: Boolean,
    val slot: Int
)

// { "slot": 1, "type": { "name": "grass", "url": "..." } }
data class TypeSlotDto(
    val slot: Int,
    val type: NamedResourceDto
)

// { "base_stat": 80, "effort": 0, "stat": { "name": "hp", "url": "..." } }
data class StatDto(
    val base_stat: Int,
    val effort: Int,
    val stat: NamedResourceDto
)

data class SpritesDto(
    val back_default: String?,
    val back_female: String?,
    val back_shiny: String?,
    val back_shiny_female: String?,
    val front_default: String?,
    val front_female: String?,
    val front_shiny: String?,
    val front_shiny_female: String?,
    val other: OtherSpritesDto?
)

// sprites.other; only the official artwork is read.
data class OtherSpritesDto(
    @SerializedName("official-artwork") val official_artwork: ArtworkDto?
)

data class ArtworkDto(
    val front_default: String?
)

// URLs of .ogg sound files; older Pokémon also have the original game's cry as "legacy".
data class CriesDto(
    val latest: String?,
    val legacy: String?
)
