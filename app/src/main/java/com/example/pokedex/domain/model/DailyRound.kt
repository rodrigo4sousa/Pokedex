package com.example.pokedex.domain.model

import java.time.LocalDate

/** One day of the guessing game: the hidden species and how the player is doing. */
data class DailyRound(
    val date: LocalDate,
    /** PokeAPI species name, e.g. "giratina"; the answer and the name revealed at the end. */
    val speciesName: String,
    val speciesId: Int,
    /** Wrong species guessed so far, in order. */
    val wrongGuesses: List<String> = emptyList(),
    val heartsLeft: Int = MAX_HEARTS,
    val result: RoundResult = RoundResult.Playing
) {
    companion object {
        const val MAX_HEARTS = 3
    }
}

enum class RoundResult { Playing, Won, Lost }

/** Days won in a row; [lastWonDate] tells whether the run is still alive. */
data class Streak(
    val count: Int = 0,
    val lastWonDate: LocalDate? = null
)
