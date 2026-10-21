package com.example.pokedex.data.mapper

import com.example.pokedex.data.remote.dto.ChainLinkDto
import com.example.pokedex.data.remote.dto.DamageRelationsDto
import com.example.pokedex.data.remote.dto.EvolutionDetailDto
import com.example.pokedex.data.remote.dto.NamedResourceDto
import com.example.pokedex.data.remote.dto.PokemonDto
import com.example.pokedex.data.remote.dto.SpritesDto
import com.example.pokedex.data.remote.dto.TypeDto
import com.example.pokedex.domain.model.Ability
import com.example.pokedex.domain.model.EvolutionCondition
import com.example.pokedex.domain.model.EvolutionStage
import com.example.pokedex.domain.model.Gender
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.model.Sprites
import com.example.pokedex.domain.model.Stat

private const val SPRITE_BASE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"

/** The number at the end of a PokeAPI resource URL: ".../pokemon/25/" -> 25. */
fun idFromUrl(url: String): Int = url.trimEnd('/').substringAfterLast('/').toInt()

/** Default front sprite for a Pokédex number, on the PokeAPI/sprites repository. */
fun spriteUrlFor(id: Int): String = "$SPRITE_BASE_URL$id.png"

/** A list entry or a species link ({ name, url }) as a card. */
fun NamedResourceDto.toPokemonSummary(): PokemonSummary {
    val id = idFromUrl(url)
    return PokemonSummary(id = id, name = name, spriteUrl = spriteUrlFor(id))
}

fun PokemonDto.toDomain(): Pokemon = Pokemon(
    id = id,
    name = name,
    speciesName = species.name,
    height = height,
    weight = weight,
    types = types.map { it.type.name },
    abilities = abilities.map { Ability(name = it.ability.name, isHidden = it.is_hidden) },
    stats = stats.map { Stat(name = it.stat.name, baseValue = it.base_stat) },
    sprites = sprites.toDomain(),
    cryUrl = cries?.latest ?: cries?.legacy
)

fun SpritesDto.toDomain(): Sprites = Sprites(
    frontDefault = front_default,
    frontShiny = front_shiny,
    frontFemale = front_female,
    frontShinyFemale = front_shiny_female,
    backDefault = back_default,
    backShiny = back_shiny,
    backFemale = back_female,
    backShinyFemale = back_shiny_female
)

/** The chain as a tree of stages; each stage carries the condition to reach it. */
fun ChainLinkDto.toDomain(): EvolutionStage = EvolutionStage(
    pokemon = species.toPokemonSummary(),
    condition = evolution_details.toCondition(),
    evolvesTo = evolves_to.map { it.toDomain() }
)

/**
 * Evolution methods can differ between games; PokeAPI flags the current one as default.
 * Without a default, the last one is used. No details (the first stage) gives null.
 */
fun List<EvolutionDetailDto>.toCondition(): EvolutionCondition? {
    val detail = firstOrNull { it.is_default == true } ?: lastOrNull() ?: return null
    return EvolutionCondition(
        trigger = detail.trigger?.name,
        minLevel = detail.min_level,
        item = detail.item?.name,
        heldItem = detail.held_item?.name,
        knownMove = detail.known_move?.name,
        knownMoveType = detail.known_move_type?.name,
        minHappiness = detail.min_happiness,
        minAffection = detail.min_affection,
        location = detail.location?.name,
        // PokeAPI sends "" when the time of day doesn't matter.
        timeOfDay = detail.time_of_day?.takeIf { it.isNotBlank() },
        gender = when (detail.gender) {
            1 -> Gender.Female
            2 -> Gender.Male
            else -> null
        },
        tradeSpecies = detail.trade_species?.name
    )
}

/** Names of every Pokémon (forms included) that has this type. */
fun TypeDto.memberNames(): Set<String> = pokemon.map { it.pokemon.name }.toSet()

/**
 * Damage multiplier each attacking type deals to a Pokémon whose types have these relations,
 * e.g. Grass/Poison takes 2× from Fire and ¼× from Grass. Neutral (1×) types are left out.
 */
fun List<DamageRelationsDto>.toDamageMultipliers(): Map<String, Double> =
    flatMap { it.attackingTypes() }.toSet()
        .associateWith { attacking -> fold(1.0) { multiplier, relations -> multiplier * relations.multiplierFrom(attacking) } }
        .filterValues { it != 1.0 }

private fun DamageRelationsDto.attackingTypes(): Set<String> =
    (double_damage_from + half_damage_from + no_damage_from).map { it.name }.toSet()

private fun DamageRelationsDto.multiplierFrom(attackingType: String): Double = when {
    no_damage_from.any { it.name == attackingType } -> 0.0
    double_damage_from.any { it.name == attackingType } -> 2.0
    half_damage_from.any { it.name == attackingType } -> 0.5
    else -> 1.0
}
