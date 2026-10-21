package com.example.pokedex.presentation.navigation

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.HomeScreen.route) {
                HomeScreen(
                    onPokemonClick = { name ->
                        navController.navigate(Screen.PokemonDetailScreen.withArgs(name))
                    }
                )
            }

            composable(Screen.PokedexScreen.route) {
                PokedexScreen(
                    onPokemonClick = { name ->
                        navController.navigate(Screen.PokemonDetailScreen.withArgs(name))
                    }
                )
            }

            composable(Screen.GuessScreen.route) {
                GuessScreen(
                    onOpenDetail = { name ->
                        navController.navigate(Screen.PokemonDetailScreen.withArgs(name))
                    }
                )
            }

            composable(
                route = Screen.PokemonDetailScreen.route + "/{name}",
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                    }
                )
            ) {
                PokemonDetailScreen(
                    onBack = { navController.popBackStack() },
                    onPokemonClick = { name ->
                        navController.navigate(Screen.PokemonDetailScreen.withArgs(name))
                    }
                )
            }
        }
    }
}
