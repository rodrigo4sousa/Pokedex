package com.example.pokedex.presentation.pokedex

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pokedex.presentation.components.FilterSidePanel
import com.example.pokedex.presentation.components.PokemonGrid
import com.example.pokedex.presentation.components.PokemonSearchBar
import com.example.pokedex.presentation.components.PokemonSort
import com.example.pokedex.presentation.components.SortChips

@Composable
fun PokedexScreen(
    onPokemonClick: (String) -> Unit,
    viewModel: PokedexViewModel = hiltViewModel()
) {
    val caughtPokemons by viewModel.caughtPokemons.observeAsState()
    val caughtCount by viewModel.caughtCount.observeAsState()
    val total by viewModel.total.observeAsState()
    val query by viewModel.query.observeAsState("")
    val sort by viewModel.sort.observeAsState(PokemonSort.Number)
    val selectedTypes by viewModel.selectedTypes.observeAsState(emptySet())
    val gridState = rememberLazyGridState()
    var showFilters by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(query, sort, selectedTypes) {
        gridState.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.systemBarsPadding()) {
                Text(
                    text = "My Pokédex",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp)
                )

                // Both are null for the moment before the database answers; show nothing rather than "0".
                val count = caughtCount
                val pokemons = caughtPokemons
                if (count != null && pokemons != null) {
                    Text(
                        text = if (total != null) "Caught $count / $total" else "Caught $count",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp)
                    )

                    if (count == 0) {
                        EmptyMessage("No Pokémon caught yet.\nTap the star on a Pokémon to catch it.")
                    } else {
                        PokemonSearchBar(
                            query = query,
                            onQueryChange = viewModel::onQueryChange,
                            placeholder = "Search caught Pokémon",
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                        )

                        SortChips(
                            options = PokemonSort.entries,
                            sort = sort,
                            onSortChange = viewModel::onSortChange,
                            activeFilterCount = selectedTypes.size,
                            onFilterClick = { showFilters = true },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        if (pokemons.isEmpty()) {
                            EmptyMessage(
                                if (selectedTypes.isEmpty()) "No caught Pokémon match \"${query.trim()}\"."
                                else "No caught Pokémon match your search and filters."
                            )
                        } else {
                            val caughtNames = remember(pokemons) { pokemons.map { it.name }.toSet() }
                            PokemonGrid(
                                pokemons = pokemons,
                                caughtNames = caughtNames,
                                gridState = gridState,
                                onPokemonClick = onPokemonClick,
                                onToggleCaught = viewModel::release
                            )
                        }
                    }
                }
            }
        }

        FilterSidePanel(
            visible = showFilters,
            selectedTypes = selectedTypes,
            onToggleType = viewModel::toggleType,
            onClearFilters = viewModel::clearTypes,
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        )
    }
}
