package com.example.pokedex.domain.game

import com.example.pokedex.domain.model.DailyRound
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.model.RoundResult
import com.example.pokedex.domain.model.Streak
import jakarta.inject.Inject
import java.text.Normalizer
import java.time.LocalDate
import java.util.Random

/** What a guess did to the round. */
sealed interface GuessOutcome {
    data object Correct : GuessOutcome
    data class Wrong(val speciesName: String) : GuessOutcome
    data object NotAPokemon : GuessOutcome
    data class AlreadyGuessed(val speciesName: String) : GuessOutcome
    data object RoundOver : GuessOutcome
}

data class GuessResult(val round: DailyRound, val outcome: GuessOutcome)

/** Rules of the daily "guess the Pokémon" game. Plain Kotlin: no Android, no I/O. */
class GuessGame @Inject constructor() {

    /**
     * Today's species. The list is shuffled once with a fixed seed and walked one day at a time,
     * so every species comes up once before any repeats. Forms (IDs above 10000) are left out.
     */
    fun dailyPick(date: LocalDate, species: List<PokemonSummary>): PokemonSummary {
        val order = species
            .filter { it.id in 1..MAX_SPECIES_ID }
            .sortedBy { it.id }
            .shuffled(Random(SHUFFLE_SEED))
        require(order.isNotEmpty()) { "No species to pick from" }
        return order[Math.floorMod(date.toEpochDay(), order.size.toLong()).toInt()]
    }

    fun newRound(date: LocalDate, species: List<PokemonSummary>): DailyRound {
        val pick = dailyPick(date, species)
        return DailyRound(date = date, speciesName = pick.name, speciesId = pick.id)
    }

    /** Lowercase, accents removed, letters and digits only: "Mr. Mime" and "mr-mime" both give "mrmime". */
    fun normalize(name: String): String =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .lowercase()
            .filter { it in 'a'..'z' || it in '0'..'9' }

    fun matches(input: String, speciesName: String): Boolean {
        val key = normalize(input)
        return key.isNotEmpty() && key == normalize(speciesName)
    }

    /**
     * Applies [input] to [round]. Only a wrong species costs a heart; a name that isn't a species,
     * or one already guessed, leaves the round as it was. A finished round doesn't change.
     */
    fun guess(round: DailyRound, input: String, speciesNames: Collection<String>): GuessResult {
        if (round.result != RoundResult.Playing) return GuessResult(round, GuessOutcome.RoundOver)
        if (matches(input, round.speciesName)) {
            return GuessResult(round.copy(result = RoundResult.Won), GuessOutcome.Correct)
        }
        val guessed = speciesNames.firstOrNull { matches(input, it) }
            ?: return GuessResult(round, GuessOutcome.NotAPokemon)
        if (guessed in round.wrongGuesses) {
            return GuessResult(round, GuessOutcome.AlreadyGuessed(guessed))
        }
        val heartsLeft = round.heartsLeft - 1
        val next = round.copy(
            wrongGuesses = round.wrongGuesses + guessed,
            heartsLeft = heartsLeft,
            result = if (heartsLeft == 0) RoundResult.Lost else RoundResult.Playing
        )
        return GuessResult(next, GuessOutcome.Wrong(guessed))
    }

    /** The streak to show on [today]: a run whose last win is older than yesterday is over. */
    fun currentStreak(streak: Streak, today: LocalDate): Int {
        val lastWon = streak.lastWonDate ?: return 0
        return if (lastWon >= today.minusDays(1)) streak.count else 0
    }

    /** The streak after today's round ended with [result]. */
    fun afterRound(streak: Streak, today: LocalDate, result: RoundResult): Streak = when (result) {
        RoundResult.Won -> Streak(
            count = if (streak.lastWonDate == today.minusDays(1)) streak.count + 1 else 1,
            lastWonDate = today
        )
        RoundResult.Lost -> Streak(count = 0, lastWonDate = streak.lastWonDate)
        RoundResult.Playing -> streak
    }

    private companion object {
        // Any fixed value works; changing it reshuffles every future day.
        const val SHUFFLE_SEED = 151L
        const val MAX_SPECIES_ID = 10000
    }
}
