package com.example.pokedex.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.presentation.navigation.sharedSprite
import com.example.pokedex.presentation.navigation.sharedSpriteBackground
import com.example.pokedex.presentation.navigation.spriteKey

@Composable
fun PokemonGrid(
    pokemons: List<PokemonSummary>,
    caughtNames: Set<String>,
    gridState: LazyGridState,
    onPokemonClick: (PokemonSummary) -> Unit,
    onToggleCaught: (PokemonSummary) -> Unit,
    modifier: Modifier = Modifier,
    /** Which screen the grid is on; it keys the sprite that flies to the detail page. */
    origin: String? = null
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pokemons, key = { it.name }) { pokemon ->
            PokemonCard(
                pokemon = pokemon,
                isCaught = pokemon.name in caughtNames,
                onClick = { onPokemonClick(pokemon) },
                onToggleCaught = { onToggleCaught(pokemon) },
                spriteKey = spriteKey(origin, pokemon.id)
            )
        }
    }
}

@Composable
private fun PokemonCard(
    pokemon: PokemonSummary,
    isCaught: Boolean,
    onClick: () -> Unit,
    onToggleCaught: () -> Unit,
    spriteKey: String?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .sharedSpriteBackground(spriteKey)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pokemon.spriteUrl,
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .sharedSprite(spriteKey)
                )
                CatchStarButton(
                    isCaught = isCaught,
                    onClick = onToggleCaught,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }

            Text(
                text = pokemon.name.replaceFirstChar { it.titlecase(LocalLocale.current.platformLocale) },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
            )
        }
    }
}
