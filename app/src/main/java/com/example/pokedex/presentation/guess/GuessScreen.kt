package com.example.pokedex.presentation.guess

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.pokedex.R
import com.example.pokedex.domain.model.DailyRound
import com.example.pokedex.presentation.components.ErrorMessage
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.guess_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            when (state) {
                GuessUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SILHOUETTE_SIZE),
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
                    Silhouette(state.imageUrl, state.fallbackImageUrl, revealedName = null)
                    Hearts(heartsLeft = state.heartsLeft)
                    GuessInput(onGuess = onGuess)
                    state.message?.let { GuessMessageText(it) }
                    WrongGuesses(state.wrongGuesses)
                    StreakText(state.streak)
                }

                is GuessUiState.Finished -> {
                    val name = state.speciesName.toDisplayName()
                    Silhouette(state.imageUrl, state.fallbackImageUrl, revealedName = name)
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
                    StreakText(state.streak)
                    WrongGuesses(state.wrongGuesses)
                    Text(
                        text = stringResource(R.string.guess_come_back),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { onOpenDetail(state.pokemonName) }) {
                        Text(stringResource(R.string.guess_open_details))
                    }
                }
            }
        }
    }
}

/**
 * The artwork with every visible pixel painted one colour until [revealedName] is set, then the
 * real artwork fades in. The description never names the Pokémon before the round ends.
 */
@Composable
private fun Silhouette(imageUrl: String?, fallbackImageUrl: String?, revealedName: String?) {
    val revealed = revealedName != null
    var model by remember(imageUrl) { mutableStateOf(imageUrl) }
    val useFallback = { if (model != fallbackImageUrl && fallbackImageUrl != null) model = fallbackImageUrl }
    val silhouetteAlpha by animateFloatAsState(
        targetValue = if (revealed) 0f else 1f,
        animationSpec = tween(durationMillis = 800),
        label = "reveal"
    )
    // Black on a light background, light grey on a dark one, so the shape always stands out.
    val tint = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) Color.Black else Color(0xFFE6E6E6)

    Box(
        modifier = Modifier.size(SILHOUETTE_SIZE),
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

@Composable
private fun WrongGuesses(names: List<String>) {
    if (names.isEmpty()) return
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.guess_wrong_guesses),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        names.forEach { name ->
            Text(text = name.toDisplayName(), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun StreakText(streak: Int) {
    Text(
        text = stringResource(R.string.guess_streak, streak),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private val SILHOUETTE_SIZE = 260.dp

/** "mr-mime" -> "Mr Mime" */
private fun String.toDisplayName(): String =
    split("-").joinToString(" ") { part ->
        part.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
