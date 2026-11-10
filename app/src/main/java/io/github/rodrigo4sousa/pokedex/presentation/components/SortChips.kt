package io.github.rodrigo4sousa.pokedex.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class PokemonSort(val label: String) {
    Number("Number"),
    Name("Name"),
    DateCaught("Date caught")
}

/** Sort chips on the left, the filter button (badged with how many filters are on) on the right. */
@Composable
fun SortChips(
    options: List<PokemonSort>,
    sort: PokemonSort,
    onSortChange: (PokemonSort) -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            FilterChip(
                selected = sort == option,
                onClick = { onSortChange(option) },
                label = { Text(option.label) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onFilterClick) {
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) Badge { Text("$activeFilterCount") }
                }
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Filters")
            }
        }
    }
}
