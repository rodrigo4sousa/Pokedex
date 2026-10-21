package com.example.pokedex.presentation.guess

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.GuessGameRepository
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.domain.game.GuessGame
import com.example.pokedex.domain.game.GuessOutcome
import com.example.pokedex.domain.model.DailyRound
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.RoundResult
import com.example.pokedex.domain.model.Streak
import com.example.pokedex.presentation.components.LoadError
import com.example.pokedex.presentation.components.toLoadError
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

sealed interface GuessUiState {
    data object Loading : GuessUiState

    data class Error(val error: LoadError) : GuessUiState

    data class Playing(
        /** Official artwork, drawn as a silhouette; [fallbackImageUrl] if it fails to load. */
        val imageUrl: String?,
        val fallbackImageUrl: String?,
        val wrongGuesses: List<String>,
        val heartsLeft: Int,
        val streak: Int,
        val message: GuessMessage?
    ) : GuessUiState

    data class Finished(
        /** The species name, the answer shown to the user. */
        val speciesName: String,
        /** The species' default Pokémon, which the detail page opens. */
        val pokemonName: String,
        /** Type names in slot order; only revealed once the round is over, so they can't hint at the answer. */
        val types: List<String>,
        val imageUrl: String?,
        val fallbackImageUrl: String?,
        val won: Boolean,
        val wrongGuesses: List<String>,
        val streak: Int
    ) : GuessUiState
}

/** Feedback for a guess that didn't count. */
sealed interface GuessMessage {
    data class NotAPokemon(val input: String) : GuessMessage
    data class AlreadyGuessed(val speciesName: String) : GuessMessage
}

@HiltViewModel
class GuessViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val gameRepository: GuessGameRepository,
    private val game: GuessGame,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableLiveData<GuessUiState>(GuessUiState.Loading)
    val uiState: LiveData<GuessUiState> = _uiState

    private var loadJob: Job? = null
    private var today: LocalDate = LocalDate.now(clock)
    private var speciesNames: List<String> = emptyList()
    private var round: DailyRound? = null
    private var pokemon: Pokemon? = null
    private var streak = Streak()

    init {
        load()
    }

    /** Reruns the load that failed. */
    fun retry() = load()

    fun guess(input: String) {
        val current = round ?: return
        val result = game.guess(current, input, speciesNames)
        when (val outcome = result.outcome) {
            GuessOutcome.RoundOver -> return
            GuessOutcome.NotAPokemon -> publish(GuessMessage.NotAPokemon(input.trim()))
            is GuessOutcome.AlreadyGuessed -> publish(GuessMessage.AlreadyGuessed(outcome.speciesName))
            GuessOutcome.Correct, is GuessOutcome.Wrong -> {
                round = result.round
                if (result.round.result != RoundResult.Playing) {
                    streak = game.afterRound(streak, today, result.round.result)
                }
                publish(message = null)
                val finishedStreak = streak.takeIf { result.round.result != RoundResult.Playing }
                viewModelScope.launch {
                    gameRepository.saveRound(result.round)
                    finishedStreak?.let { gameRepository.saveStreak(it) }
                }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = GuessUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                today = LocalDate.now(clock)
                val species = pokemonRepository.getAllSpecies()
                // The first load of the day saves the pick, so it stays put for the rest of the day.
                val todaysRound = gameRepository.getRound(today)
                    ?: game.newRound(today, species).also { gameRepository.saveRound(it) }
                val defaultPokemon = pokemonRepository.getDefaultPokemon(todaysRound.speciesName)
                speciesNames = species.map { it.name }
                streak = gameRepository.getStreak()
                round = todaysRound
                pokemon = defaultPokemon
                publish(message = null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GuessUiState.Error(e.toLoadError())
            }
        }
    }

    private fun publish(message: GuessMessage?) {
        val current = round ?: return
        val shown = pokemon ?: return
        val imageUrl = shown.artworkUrl ?: shown.sprites.frontDefault
        val fallbackImageUrl = shown.sprites.frontDefault
        _uiState.value = if (current.result == RoundResult.Playing) {
            GuessUiState.Playing(
                imageUrl = imageUrl,
                fallbackImageUrl = fallbackImageUrl,
                wrongGuesses = current.wrongGuesses,
                heartsLeft = current.heartsLeft,
                streak = game.currentStreak(streak, today),
                message = message
            )
        } else {
            GuessUiState.Finished(
                speciesName = current.speciesName,
                pokemonName = shown.name,
                types = shown.types,
                imageUrl = imageUrl,
                fallbackImageUrl = fallbackImageUrl,
                won = current.result == RoundResult.Won,
                wrongGuesses = current.wrongGuesses,
                streak = game.currentStreak(streak, today)
            )
        }
    }
}
