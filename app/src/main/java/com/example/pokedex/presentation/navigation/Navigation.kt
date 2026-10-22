package com.example.pokedex.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.R
import com.example.pokedex.presentation.guess.GuessScreen
import com.example.pokedex.presentation.home.HomeScreen
import com.example.pokedex.presentation.pokedex.PokedexScreen
import com.example.pokedex.presentation.pokemonDetail.PokemonDetailScreen

// The icon is read in composition, because a drawable resource can only be loaded there.
private enum class BottomTab(val screen: Screen, val label: String, val icon: @Composable () -> ImageVector) {
    Home(Screen.HomeScreen, "Home", { Icons.Filled.Home }),
    Pokedex(Screen.PokedexScreen, "Pokédex", { Icons.Filled.Star }),
    Guess(Screen.GuessScreen, "Guess", { ImageVector.vectorResource(R.drawable.ic_question_mark) })
}

private const val TRANSITION_MILLIS = 350 // keep in step with SHARED_MILLIS in SharedTransitions.kt

// The detail page rises a little while fading in, and sinks back when it is closed.
private val detailEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(TRANSITION_MILLIS)) + slideInVertically(tween(TRANSITION_MILLIS)) { it / 8 }
}
private val detailPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(TRANSITION_MILLIS)) + slideOutVertically(tween(TRANSITION_MILLIS)) { it / 8 }
}

/**
 * Opens a detail page. With [id] the page can show the sprite before the Pokémon has loaded, and with
 * [origin] as well that sprite flies in from the card that was tapped.
 */
private fun NavController.openDetail(name: String, id: Int? = null, origin: String? = null) {
    val query = listOfNotNull(id?.let { "id=$it" }, origin?.let { "from=$it" }).joinToString("&")
    navigate(Screen.PokemonDetailScreen.withArgs(name) + if (query.isEmpty()) "" else "?$query")
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = BottomTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
    }

    // Screens handle their own system-bar insets; the scaffold only adds room for the bottom bar,
    // which is shown on the tabs and hidden on the detail page.
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentTab != null) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == currentTab,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon(), contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.HomeScreen.route,
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    composable(Screen.HomeScreen.route) {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            HomeScreen(
                                onPokemonClick = { pokemon ->
                                    navController.openDetail(pokemon.name, pokemon.id, SpriteOrigin.HOME)
                                }
                            )
                        }
                    }

                    composable(Screen.PokedexScreen.route) {
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            PokedexScreen(
                                onPokemonClick = { pokemon ->
                                    navController.openDetail(pokemon.name, pokemon.id, SpriteOrigin.POKEDEX)
                                }
                            )
                        }
                    }

                    composable(Screen.GuessScreen.route) {
                        GuessScreen(
                            onOpenDetail = { name -> navController.openDetail(name) }
                        )
                    }

                    composable(
                        route = Screen.PokemonDetailScreen.route + "/{name}?id={id}&from={from}",
                        arguments = listOf(
                            navArgument("name") {
                                type = NavType.StringType
                            },
                            navArgument("id") {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                            navArgument("from") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        ),
                        enterTransition = detailEnter,
                        popExitTransition = detailPopExit
                    ) { entry ->
                        val arguments = entry.arguments
                        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            PokemonDetailScreen(
                                spriteKey = spriteKey(arguments?.getString("from"), arguments?.getInt("id")),
                                onBack = { navController.popBackStack() },
                                onPokemonClick = { pokemon ->
                                    navController.openDetail(pokemon.id.toString(), pokemon.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
