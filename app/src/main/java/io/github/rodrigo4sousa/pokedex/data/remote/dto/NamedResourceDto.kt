package io.github.rodrigo4sousa.pokedex.data.remote.dto

// PokeAPI's link to another resource: { "name": "grass", "url": "https://pokeapi.co/api/v2/type/12/" }
data class NamedResourceDto(
    val name: String,
    val url: String
)

// A link without a name, e.g. the species' evolution_chain.
data class ApiResourceDto(
    val url: String
)
