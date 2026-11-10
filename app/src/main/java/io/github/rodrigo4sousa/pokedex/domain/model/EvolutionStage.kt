package io.github.rodrigo4sousa.pokedex.domain.model

/**
 * One stage of an evolution chain and the stages it evolves into. A chain is a tree: Bulbasaur has one
 * stage after another, Eevee has eight stages after the first, Tauros has a single stage.
 */
data class EvolutionStage(
    val pokemon: PokemonSummary,
    /** What it takes to reach this stage from the previous one; null for the first stage. */
    val condition: EvolutionCondition?,
    val evolvesTo: List<EvolutionStage>
)

/**
 * The conditions of one way to evolve. Names are PokeAPI names ("water-stone", "level-up").
 * Only the fields the app shows; all of them are optional.
 */
data class EvolutionCondition(
    val trigger: String?,
    val minLevel: Int? = null,
    val item: String? = null,
    val heldItem: String? = null,
    val knownMove: String? = null,
    val knownMoveType: String? = null,
    val minHappiness: Int? = null,
    val minAffection: Int? = null,
    val location: String? = null,
    val timeOfDay: String? = null,
    val gender: Gender? = null,
    val tradeSpecies: String? = null
)

enum class Gender { Female, Male }
