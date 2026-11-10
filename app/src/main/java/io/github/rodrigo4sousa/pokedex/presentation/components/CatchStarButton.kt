package io.github.rodrigo4sousa.pokedex.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val CaughtStarColor = Color(0xFFFFC107)

/** Star that catches or releases a Pokémon: gold when caught, grey when not. */
@Composable
fun CatchStarButton(
    isCaught: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Icon(
            Icons.Filled.Star,
            contentDescription = if (isCaught) "Release" else "Catch",
            tint = if (isCaught) CaughtStarColor else MaterialTheme.colorScheme.outline
        )
    }
}
