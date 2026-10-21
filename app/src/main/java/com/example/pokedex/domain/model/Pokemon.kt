package com.example.pokedex.domain.model

/** Everything the detail page shows about one Pokémon. */
data class Pokemon(
    val id: Int,
    val name: String,
    /** The species this Pokémon (or form) belongs to; its evolution chain is looked up by species. */
    val speciesName: String,
    /** In decimetres, as PokeAPI sends it. */
    val height: Int,
    /** In hectograms, as PokeAPI sends it. */
    val weight: Int,
    /** Type names in slot order, e.g. ["grass", "poison"]. */
    val types: List<String>,
    val abilities: List<Ability>,
    val stats: List<Stat>,
    val sprites: Sprites,
    /** The newest cry, or the original game's one when there is no newer recording. */
    val cryUrl: String?
)

data class Ability(
    val name: String,
    val isHidden: Boolean
)

data class Stat(
    /** PokeAPI stat name, e.g. "hp", "special-attack". */
    val name: String,
    val baseValue: Int
)

/** Sprite URLs; any of them can be missing. */
data class Sprites(
    val frontDefault: String?,
    val frontShiny: String?,
    val frontFemale: String?,
    val frontShinyFemale: String?,
    val backDefault: String?,
    val backShiny: String?,
    val backFemale: String?,
    val backShinyFemale: String?
)
