package io.github.rodrigo4sousa.pokedex.presentation.navigation

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object PokedexScreen : Screen("pokedex_screen")
    object PokemonDetailScreen : Screen("pokemon_detail_screen")
    object GuessScreen : Screen("guess_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}