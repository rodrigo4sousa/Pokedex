package io.github.rodrigo4sousa.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.rodrigo4sousa.pokedex.presentation.navigation.Navigation
import io.github.rodrigo4sousa.pokedex.presentation.theme.PokedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PokedexTheme {
                Navigation()
            }
        }
    }
}