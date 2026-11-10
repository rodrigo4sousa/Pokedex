package io.github.rodrigo4sousa.pokedex.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// Same length as the detail page's enter and exit transitions in Navigation.
private const val SHARED_MILLIS = 350

// Set by Navigation for every destination. Null outside the NavHost (previews, tests), where the
// shared-element modifiers below do nothing.
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Where a detail page was opened from, so the same Pokémon in two grids never shares a key. */
object SpriteOrigin {
    const val HOME = "home"
    const val POKEDEX = "pokedex"
}

/** The shared-element key of a Pokémon's sprite, or null when there is nothing to animate from. */
fun spriteKey(origin: String?, id: Int?): String? =
    if (origin != null && id != null && id > 0) "$origin-$id" else null

/** The sprite flies between the grid card and the detail hero. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedSprite(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalNavAnimatedVisibilityScope.current
    if (key == null || sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        this@sharedSprite.sharedElement(
            sharedContentState = rememberSharedContentState("sprite-$key"),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ -> tween(SHARED_MILLIS) }
        )
    }
}

/** The box behind the sprite grows from the card into the detail hero. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedSpriteBackground(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalNavAnimatedVisibilityScope.current
    if (key == null || sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        // Remeasured rather than scaled, so the card's star and the hero's toggles keep their size.
        this@sharedSpriteBackground.sharedBounds(
            sharedContentState = rememberSharedContentState("sprite-bg-$key"),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = { _, _ -> tween(SHARED_MILLIS) },
            resizeMode = RemeasureToBounds
        )
    }
}
