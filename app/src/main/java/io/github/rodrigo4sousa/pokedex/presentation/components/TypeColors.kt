package io.github.rodrigo4sousa.pokedex.presentation.components

import androidx.compose.ui.graphics.Color

/** The 18 battle types, in the order the games list them. */
val PokemonTypes = listOf(
    "normal", "fire", "water", "electric", "grass", "ice",
    "fighting", "poison", "ground", "flying", "psychic", "bug",
    "rock", "ghost", "dragon", "dark", "steel", "fairy"
)

fun typeColor(name: String): Color = when (name.lowercase()) {
    "normal" -> Color(0xFFA8A77A)
    "fire" -> Color(0xFFEE8130)
    "water" -> Color(0xFF6390F0)
    "electric" -> Color(0xFFF7D02C)
    "grass" -> Color(0xFF7AC74C)
    "ice" -> Color(0xFF96D9D6)
    "fighting" -> Color(0xFFC22E28)
    "poison" -> Color(0xFFA33EA1)
    "ground" -> Color(0xFFE2BF65)
    "flying" -> Color(0xFFA98FF3)
    "psychic" -> Color(0xFFF95587)
    "bug" -> Color(0xFFA6B91A)
    "rock" -> Color(0xFFB6A136)
    "ghost" -> Color(0xFF735797)
    "dragon" -> Color(0xFF6F35FC)
    "dark" -> Color(0xFF705746)
    "steel" -> Color(0xFFB7B7CE)
    "fairy" -> Color(0xFFD685AD)
    else -> Color(0xFF9E9E9E)
}
