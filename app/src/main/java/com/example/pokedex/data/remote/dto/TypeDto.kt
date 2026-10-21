package com.example.pokedex.data.remote.dto

// GET type/{name}.
data class TypeDto(
    val damage_relations: DamageRelationsDto,
    val pokemon: List<TypePokemonDto>
)

// Every Pokémon (forms included) that has this type, in either slot.
data class TypePokemonDto(
    val pokemon: NamedResourceDto
)

// Defensive side of a type: which attacking types do double, half or no damage to it.
data class DamageRelationsDto(
    val double_damage_from: List<NamedResourceDto>,
    val half_damage_from: List<NamedResourceDto>,
    val no_damage_from: List<NamedResourceDto>
)
