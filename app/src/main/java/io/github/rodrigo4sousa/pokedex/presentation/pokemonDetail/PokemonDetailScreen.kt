package io.github.rodrigo4sousa.pokedex.presentation.pokemonDetail

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import io.github.rodrigo4sousa.pokedex.domain.model.EvolutionCondition
import io.github.rodrigo4sousa.pokedex.domain.model.EvolutionStage
import io.github.rodrigo4sousa.pokedex.domain.model.Gender
import io.github.rodrigo4sousa.pokedex.domain.model.Pokemon
import io.github.rodrigo4sousa.pokedex.domain.model.PokemonSummary
import io.github.rodrigo4sousa.pokedex.domain.model.Sprites
import io.github.rodrigo4sousa.pokedex.presentation.components.CatchStarButton
import io.github.rodrigo4sousa.pokedex.presentation.components.typeColor
import io.github.rodrigo4sousa.pokedex.presentation.navigation.sharedSprite
import io.github.rodrigo4sousa.pokedex.presentation.navigation.sharedSpriteBackground
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    onPokemonClick: (PokemonSummary) -> Unit,
    /** Shared-element key of the sprite the page was opened from; null when it wasn't a grid card. */
    spriteKey: String? = null,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val pokemon by viewModel.pokemon.observeAsState()
    val error by viewModel.error.observeAsState()
    val evolutionChain by viewModel.evolutionChain.observeAsState()
    val typeEffectiveness by viewModel.typeEffectiveness.observeAsState()
    val isCaught by viewModel.isCaught.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentError = error
            val currentPokemon = pokemon

            when {
                currentError != null -> ErrorState(
                    message = currentError,
                    onBack = onBack,
                    modifier = Modifier.align(Alignment.Center)
                )

                // With a placeholder sprite the hero is drawn at once, so it can take part in the transition.
                currentPokemon != null || viewModel.placeholderSpriteUrl != null -> PokemonContent(
                    pokemon = currentPokemon,
                    placeholderSpriteUrl = viewModel.placeholderSpriteUrl,
                    spriteKey = spriteKey,
                    evolutionChain = evolutionChain,
                    typeEffectiveness = typeEffectiveness,
                    isCaught = isCaught,
                    onToggleCaught = viewModel::toggleCaught,
                    onPokemonClick = onPokemonClick
                )

                else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun PokemonContent(
    pokemon: Pokemon?,
    placeholderSpriteUrl: String?,
    spriteKey: String?,
    evolutionChain: EvolutionStage?,
    typeEffectiveness: Map<String, Double>?,
    isCaught: Boolean?,
    onToggleCaught: () -> Unit,
    onPokemonClick: (PokemonSummary) -> Unit
) {
    val mainColor = pokemon?.types?.firstOrNull()?.let { typeColor(it) }
        ?: MaterialTheme.colorScheme.surfaceContainerHighest

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero
        HeroSprite(
            pokemon = pokemon,
            placeholderSpriteUrl = placeholderSpriteUrl,
            spriteKey = spriteKey,
            backgroundColor = mainColor.copy(alpha = 0.25f),
            isCaught = isCaught,
            onToggleCaught = onToggleCaught
        )

        if (pokemon == null) {
            CircularProgressIndicator()
            return@Column
        }

        // Id + name + types
        Column(
            modifier = Modifier.enterFromBelow(order = 0),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "#%03d".format(pokemon.id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = pokemon.name.toDisplayName(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pokemon.types.forEach { type -> TypeChip(name = type) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isCaught != null) {
                    CatchButton(isCaught = isCaught, onClick = onToggleCaught)
                }
                val cryUrl = pokemon.cryUrl
                if (cryUrl != null) {
                    CryButton(url = cryUrl)
                }
            }
        }

        // Height / weight (PokeAPI: decimetres and hectograms)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .enterFromBelow(order = 1),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Height",
                value = "%.1f m".format(pokemon.height / 10.0),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Weight",
                value = "%.1f kg".format(pokemon.weight / 10.0),
                modifier = Modifier.weight(1f)
            )
        }

        // Abilities
        if (pokemon.abilities.isNotEmpty()) {
            Section(title = "Abilities", modifier = Modifier.enterFromBelow(order = 2)) {
                InfoCard {
                    pokemon.abilities.forEachIndexed { index, ability ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ability.name.toDisplayName(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (ability.isHidden) {
                                HiddenTag()
                            }
                        }
                    }
                }
            }
        }

        // Base stats
        if (pokemon.stats.isNotEmpty()) {
            Section(title = "Base stats", modifier = Modifier.enterFromBelow(order = 3)) {
                InfoCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pokemon.stats.forEach { stat ->
                            StatBar(label = stat.name.toStatLabel(), value = stat.baseValue, color = mainColor)
                        }
                        HorizontalDivider()
                        StatBar(label = "Total", value = pokemon.stats.sumOf { it.baseValue }, color = null)
                    }
                }
            }
        }

        // Type effectiveness (how much damage it takes from each attacking type)
        if (!typeEffectiveness.isNullOrEmpty()) {
            Section(title = "Type effectiveness", modifier = Modifier.enterFromBelow(order = 4)) {
                InfoCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EffectivenessGroup(
                            title = "Weak to",
                            multipliers = typeEffectiveness.filterValues { it > 1.0 }
                                .entries.sortedByDescending { it.value }
                        )
                        EffectivenessGroup(
                            title = "Resistant to",
                            multipliers = typeEffectiveness.filterValues { it > 0.0 && it < 1.0 }
                                .entries.sortedBy { it.value }
                        )
                        EffectivenessGroup(
                            title = "Immune to",
                            multipliers = typeEffectiveness.filterValues { it == 0.0 }.entries.toList()
                        )
                    }
                }
            }
        }

        // Evolution chain (hidden for Pokémon that don't evolve)
        if (evolutionChain != null && evolutionChain.evolvesTo.isNotEmpty()) {
            Section(title = "Evolution", modifier = Modifier.enterFromBelow(order = 5)) {
                InfoCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        EvolutionTree(
                            stage = evolutionChain,
                            currentSpecies = pokemon.speciesName,
                            onPokemonClick = onPokemonClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fades and lifts a block into place once it is first shown. Blocks enter [order] x 40 ms apart, and
 * sections that load later (evolution, type effectiveness) animate when they appear.
 */
@Composable
private fun Modifier.enterFromBelow(order: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(order * 40L)
        progress.animateTo(1f, tween(durationMillis = 300))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

/** Until [pokemon] has loaded, shows [placeholderSpriteUrl] on a neutral background, without toggles. */
@Composable
private fun HeroSprite(
    pokemon: Pokemon?,
    placeholderSpriteUrl: String?,
    spriteKey: String?,
    backgroundColor: Color,
    isCaught: Boolean?,
    onToggleCaught: () -> Unit
) {
    val sprites = pokemon?.sprites
    val animatedBackground by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 400),
        label = "heroBackground"
    )
    var showBack by rememberSaveable { mutableStateOf(false) }
    var shiny by rememberSaveable { mutableStateOf(false) }
    var female by rememberSaveable { mutableStateOf(false) }

    // Flip like a card: the sprite swaps to the other side halfway through the turn.
    val rotation by animateFloatAsState(
        targetValue = if (showBack) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "spriteFlip"
    )
    val facingBack = rotation > 90f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .sharedSpriteBackground(spriteKey)
            .clip(RoundedCornerShape(28.dp))
            .background(animatedBackground),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = sprites?.let { it.pick(facingBack, shiny, female) ?: it.frontDefault ?: it.frontShiny }
                ?: placeholderSpriteUrl,
            contentDescription = pokemon?.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 64.dp)
                .sharedSprite(spriteKey)
                .graphicsLayer {
                    rotationY = if (facingBack) rotation - 180f else rotation
                    cameraDistance = 12f * density
                }
        )

        // Null until the caught state has loaded from the database.
        if (isCaught != null) {
            CatchStarButton(
                isCaught = isCaught,
                onClick = onToggleCaught,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }

        // A toggle is disabled when PokeAPI has no sprite for the combination it would switch to.
        if (sprites != null) Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpriteToggle(
                label = "Rotate",
                icon = Icons.Filled.Refresh,
                selected = showBack,
                enabled = sprites.pick(!showBack, shiny, female) != null,
                onClick = { showBack = !showBack }
            )
            SpriteToggle(
                label = "Shiny",
                icon = Icons.Filled.Star,
                selected = shiny,
                enabled = sprites.pick(showBack, !shiny, female) != null,
                onClick = { shiny = !shiny }
            )
            if (sprites.frontFemale != null) {
                SpriteToggle(
                    label = "♀ Female",
                    selected = female,
                    enabled = sprites.pick(showBack, shiny, !female) != null,
                    onClick = { female = !female }
                )
            }
        }
    }
}

@Composable
private fun SpriteToggle(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = icon?.let {
            { Icon(it, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

@Composable
private fun CatchButton(isCaught: Boolean, onClick: () -> Unit) {
    if (isCaught) {
        OutlinedButton(onClick = onClick) {
            Text("Release")
        }
    } else {
        Button(onClick = onClick) {
            Text("Catch")
        }
    }
}

@Composable
private fun CryButton(url: String) {
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(url) {
        onDispose {
            player?.release()
            player = null
        }
    }

    FilledTonalButton(
        onClick = {
            // Start over on every tap instead of queueing cries.
            player?.release()
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnPreparedListener { it.start() }
                setDataSource(url)
                prepareAsync()
            }
        }
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text("Play cry")
    }
}

@Composable
private fun HiddenTag() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "Hidden",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** One stat row; [color] is the bar colour, or null for a row without a bar (the total). */
@Composable
private fun StatBar(label: String, value: Int, color: Color?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
        if (color != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((value / MAX_BASE_STAT).coerceAtMost(1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun EffectivenessGroup(title: String, multipliers: List<Map.Entry<String, Double>>) {
    if (multipliers.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            multipliers.forEach { (type, multiplier) ->
                TypeChip(name = type, multiplier = multiplier)
            }
        }
    }
}

/** Draws [stage] followed by everything it evolves into; branches stack vertically. */
@Composable
private fun EvolutionTree(
    stage: EvolutionStage,
    currentSpecies: String,
    onPokemonClick: (PokemonSummary) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EvolutionMember(
            stage = stage,
            isCurrent = stage.pokemon.name == currentSpecies,
            onClick = { onPokemonClick(stage.pokemon) }
        )
        if (stage.evolvesTo.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stage.evolvesTo.forEach { next ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EvolutionArrow(condition = next.condition.describe())
                        EvolutionTree(
                            stage = next,
                            currentSpecies = currentSpecies,
                            onPokemonClick = onPokemonClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvolutionMember(stage: EvolutionStage, isCurrent: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isCurrent, onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = stage.pokemon.spriteUrl,
            contentDescription = stage.pokemon.name,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                )
        )
        Text(
            text = stage.pokemon.name.toDisplayName(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun EvolutionArrow(condition: String) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (condition.isNotEmpty()) {
            Text(
                text = condition,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TypeChip(name: String, multiplier: Double? = null) {
    val color = typeColor(name)
    Surface(
        shape = CircleShape,
        color = color
    ) {
        Text(
            text = if (multiplier == null) name.toDisplayName()
            else "${name.toDisplayName()} ${multiplier.toMultiplierLabel()}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}

/** "solar-power" -> "Solar Power" */
private fun String.toDisplayName(): String =
    split("-").joinToString(" ") { part ->
        part.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

// Highest base stat in the games (Blissey's HP), so bars are comparable between Pokémon.
private const val MAX_BASE_STAT = 255f

private fun String.toStatLabel(): String = when (this) {
    "hp" -> "HP"
    "special-attack" -> "Sp. Atk"
    "special-defense" -> "Sp. Def"
    else -> toDisplayName()
}

private fun Double.toMultiplierLabel(): String = when (this) {
    0.25 -> "¼×"
    0.5 -> "½×"
    else -> "${toInt()}×"
}

/** Short label for what triggers an evolution, e.g. "Lv. 16" or "Use Water Stone". */
private fun EvolutionCondition?.describe(): String {
    val condition = this ?: return ""
    val conditions = buildList {
        condition.minLevel?.let { add("Lv. $it") }
        condition.item?.let { add("Use ${it.toDisplayName()}") }
        condition.heldItem?.let { add("Hold ${it.toDisplayName()}") }
        condition.knownMove?.let { add("Know ${it.toDisplayName()}") }
        condition.knownMoveType?.let { add("Know a ${it.toDisplayName()} move") }
        condition.minHappiness?.let { add("High friendship") }
        condition.minAffection?.let { add("High affection") }
        condition.location?.let { add("At ${it.toDisplayName()}") }
        condition.timeOfDay?.takeIf { it.isNotBlank() }?.let { add(it.toDisplayName()) }
        when (condition.gender) {
            Gender.Female -> add("Female")
            Gender.Male -> add("Male")
            null -> {}
        }
        condition.tradeSpecies?.let { add("For ${it.toDisplayName()}") }
    }
    val trigger = condition.trigger
    return when {
        trigger == "trade" -> (listOf("Trade") + conditions).joinToString(", ")
        conditions.isEmpty() -> trigger?.toDisplayName().orEmpty()
        else -> conditions.joinToString(", ")
    }
}

private fun Sprites.pick(back: Boolean, shiny: Boolean, female: Boolean): String? = when {
    back && shiny && female -> backShinyFemale
    back && shiny -> backShiny
    back && female -> backFemale
    back -> backDefault
    shiny && female -> frontShinyFemale
    shiny -> frontShiny
    female -> frontFemale
    else -> frontDefault
}
