package io.github.rodrigo4sousa.pokedex.presentation.guess

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import io.github.rodrigo4sousa.pokedex.R
import io.github.rodrigo4sousa.pokedex.domain.model.DailyRound
import io.github.rodrigo4sousa.pokedex.presentation.components.ErrorMessage
import io.github.rodrigo4sousa.pokedex.presentation.components.typeColor
import java.util.Locale

@Composable
fun GuessScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: GuessViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.observeAsState(GuessUiState.Loading)

    GuessScreenContent(
        state = state,
        onGuess = viewModel::guess,
        onRetry = viewModel::retry,
        onOpenDetail = onOpenDetail
    )
}

@Composable
fun GuessScreenContent(
    state: GuessUiState,
    onGuess: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    // Neutral theme colours while playing, so nothing hints at the answer; the type colour once revealed.
    val revealedColor = (state as? GuessUiState.Finished)?.types?.firstOrNull()?.let { typeColor(it) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.guess_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            when (state) {
                GuessUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HERO_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is GuessUiState.Error -> ErrorMessage(
                    error = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.padding(top = 48.dp)
                )

                is GuessUiState.Playing -> {
                    HeroSilhouette(state.imageUrl, state.fallbackImageUrl, revealedName = null, revealedColor = null)
                    InfoCard {
                        Hearts(heartsLeft = state.heartsLeft)
                        GuessInput(onGuess = onGuess)
                        state.message?.let { GuessMessageText(it) }
                    }
                    StatsRow(streak = state.streak, wrongGuesses = state.wrongGuesses, accent = null)
                }

                is GuessUiState.Finished -> {
                    val name = state.speciesName.toDisplayName()
                    HeroSilhouette(state.imageUrl, state.fallbackImageUrl, revealedName = name, revealedColor = revealedColor)
                    InfoCard {
                        Text(
                            text = stringResource(if (state.won) R.string.guess_won else R.string.guess_lost),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.types.forEach { TypeBadge(it) }
                        }
                        Text(
                            text = stringResource(R.string.guess_come_back),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onOpenDetail(state.pokemonName) },
                            colors = revealedColor?.let {
                                ButtonDefaults.buttonColors(containerColor = it, contentColor = it.contentColor())
                            } ?: ButtonDefaults.buttonColors()
                        ) {
                            Text(stringResource(R.string.guess_open_details))
                        }
                    }
                    StatsRow(streak = state.streak, wrongGuesses = state.wrongGuesses, accent = revealedColor)
                }
            }
        }
    }
}

/**
 * The silhouette on a rounded hero card, like the detail page's sprite. The card is a neutral theme
 * colour during the round and turns to the Pokémon's type colour when it is revealed.
 */
@Composable
private fun HeroSilhouette(
    imageUrl: String?,
    fallbackImageUrl: String?,
    revealedName: String?,
    revealedColor: Color?
) {
    val neutral = MaterialTheme.colorScheme.surfaceContainerHighest
    val background by animateColorAsState(
        targetValue = revealedColor?.copy(alpha = 0.25f)?.compositeOver(MaterialTheme.colorScheme.surface) ?: neutral,
        animationSpec = tween(durationMillis = 800),
        label = "heroColor"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .clip(RoundedCornerShape(28.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        // Black on a light card, light grey on a dark one, so the shape always stands out.
        val tint = if (neutral.luminance() > 0.5f) Color.Black else Color(0xFFE6E6E6)
        Silhouette(imageUrl, fallbackImageUrl, revealedName, tint)
    }
}

/**
 * The artwork with every visible pixel painted [tint] until [revealedName] is set, then the real
 * artwork fades in. The description never names the Pokémon before the round ends.
 */
@Composable
private fun Silhouette(imageUrl: String?, fallbackImageUrl: String?, revealedName: String?, tint: Color) {
    val revealed = revealedName != null
    var model by remember(imageUrl) { mutableStateOf(imageUrl) }
    val useFallback = { if (model != fallbackImageUrl && fallbackImageUrl != null) model = fallbackImageUrl }
    val silhouetteAlpha by animateFloatAsState(
        targetValue = if (revealed) 0f else 1f,
        animationSpec = tween(durationMillis = 800),
        label = "reveal"
    )

    Box(
        modifier = Modifier
            .size(SILHOUETTE_SIZE)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = model,
            contentDescription = revealedName,
            contentScale = ContentScale.Fit,
            onError = { useFallback() },
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f - silhouetteAlpha)
        )
        AsyncImage(
            model = model,
            contentDescription = if (revealed) null else stringResource(R.string.guess_hidden_pokemon),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
            onError = { useFallback() },
            modifier = Modifier
                .fillMaxSize()
                .alpha(silhouetteAlpha)
        )
    }
}

/** Same card style as the detail page's sections. */
@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/** Streak and wrong guesses side by side; [accent] tints the streak card once the Pokémon is revealed. */
@Composable
private fun StatsRow(streak: Int, wrongGuesses: List<String>, accent: Color?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val streakDescription = stringResource(R.string.guess_streak, streak)
        StatCard(
            label = stringResource(R.string.guess_streak_label),
            containerColor = accent?.copy(alpha = 0.25f)?.compositeOver(MaterialTheme.colorScheme.surface)
                ?: MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics { contentDescription = streakDescription }
        ) {
            Text(
                text = streak.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        StatCard(
            label = stringResource(R.string.guess_wrong_guesses),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.weight(1f)
        ) {
            if (wrongGuesses.isEmpty()) {
                Text(
                    text = stringResource(R.string.guess_no_wrong_guesses),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                wrongGuesses.forEach { name ->
                    Text(text = name.toDisplayName(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // The card's own content colour, so the label stays readable on any container colour.
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = LocalContentColor.current.copy(alpha = 0.8f)
            )
            content()
        }
    }
}

@Composable
private fun Hearts(heartsLeft: Int) {
    val description = stringResource(R.string.guess_hearts_left, heartsLeft, DailyRound.MAX_HEARTS)
    Row(
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(DailyRound.MAX_HEARTS) { index ->
            Icon(
                imageVector = if (index < heartsLeft) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun GuessInput(onGuess: (String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (input.isNotBlank()) {
            onGuess(input)
            input = ""
        }
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text(stringResource(R.string.guess_name_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { submit() }),
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = submit, enabled = input.isNotBlank()) {
        Text(stringResource(R.string.guess_button))
    }
}

@Composable
private fun GuessMessageText(message: GuessMessage) {
    Text(
        text = when (message) {
            is GuessMessage.NotAPokemon -> stringResource(R.string.guess_not_a_pokemon, message.input)
            is GuessMessage.AlreadyGuessed ->
                stringResource(R.string.guess_already_guessed, message.speciesName.toDisplayName())
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
}

/** A type name on its type colour, like the chips on the detail page. */
@Composable
private fun TypeBadge(type: String) {
    val color = typeColor(type)
    Surface(shape = CircleShape, color = color) {
        Text(
            text = type.toDisplayName(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color.contentColor(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

/** Black or white, whichever reads better on this colour. */
private fun Color.contentColor(): Color = if (luminance() > 0.5f) Color.Black else Color.White

private val HERO_HEIGHT = 280.dp
private val SILHOUETTE_SIZE = 260.dp

/** "mr-mime" -> "Mr Mime" */
private fun String.toDisplayName(): String =
    split("-").joinToString(" ") { part ->
        part.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
