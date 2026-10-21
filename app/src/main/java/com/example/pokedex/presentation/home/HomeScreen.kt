package com.example.pokedex.presentation.home

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.presentation.components.FilterSidePanel
import com.example.pokedex.presentation.components.PokemonGrid
import com.example.pokedex.presentation.components.PokemonSearchBar
import com.example.pokedex.presentation.components.PokemonSort
import com.example.pokedex.presentation.components.SortChips

@Composable
fun HomeScreen(
    onPokemonClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val pokemons by viewModel.pokemons.observeAsState(emptyList())
    val query by viewModel.query.observeAsState("")
    val sort by viewModel.sort.observeAsState(PokemonSort.Number)
    val caughtNames by viewModel.caughtNames.observeAsState(emptySet())
    val selectedTypes by viewModel.selectedTypes.observeAsState(emptySet())

    HomeScreenContent(
        pokemons = pokemons,
        caughtNames = caughtNames,
        query = query,
        sort = sort,
        selectedTypes = selectedTypes,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onToggleType = viewModel::toggleType,
        onClearTypes = viewModel::clearTypes,
        onPokemonClick = onPokemonClick,
        onToggleCaught = viewModel::toggleCaught
    )
}

@Composable
fun HomeScreenContent(
    pokemons: List<PokemonSummary>,
    caughtNames: Set<String>,
    query: String,
    sort: PokemonSort,
    selectedTypes: Set<String>,
    onQueryChange: (String) -> Unit,
    onSortChange: (PokemonSort) -> Unit,
    onToggleType: (String) -> Unit,
    onClearTypes: () -> Unit,
    onPokemonClick: (String) -> Unit,
    onToggleCaught: (PokemonSummary) -> Unit
) {
    val gridState = rememberLazyGridState()
    var showFilters by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(query, sort, selectedTypes) {
        gridState.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.systemBarsPadding()) {
                Text(
                    text = "Pokédex",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp)
                )

                PokemonSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
                )

                SortChips(
                    options = listOf(PokemonSort.Number, PokemonSort.Name),
                    sort = sort,
                    onSortChange = onSortChange,
                    activeFilterCount = selectedTypes.size,
                    onFilterClick = { showFilters = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (pokemons.isEmpty()) {
                        Text(
                            text = "No Pokémon to show",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        PokemonGrid(
                            pokemons = pokemons,
                            caughtNames = caughtNames,
                            gridState = gridState,
                            onPokemonClick = onPokemonClick,
                            onToggleCaught = onToggleCaught
                        )
                    }
                }
            }
        }

        FilterSidePanel(
            visible = showFilters,
            selectedTypes = selectedTypes,
            onToggleType = onToggleType,
            onClearFilters = onClearTypes,
            onDismiss = { showFilters = false }
        )
    }
}
