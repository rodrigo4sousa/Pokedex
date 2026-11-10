package io.github.rodrigo4sousa.pokedex.domain.game

import io.github.rodrigo4sousa.pokedex.domain.model.DailyRound
import io.github.rodrigo4sousa.pokedex.domain.model.PokemonSummary
import io.github.rodrigo4sousa.pokedex.domain.model.RoundResult
import io.github.rodrigo4sousa.pokedex.domain.model.Streak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GuessGameTest {

    private val game = GuessGame()
    private val today = LocalDate.of(2026, 10, 21)

    /** Every species in the National Pokédex, plus two forms that must never be picked. */
    private val allSpecies = (1..1025).map { PokemonSummary(it, "species-$it", "") } +
        PokemonSummary(10007, "giratina-origin", "") + PokemonSummary(10034, "charizard-mega-x", "")

    private val names = listOf("bulbasaur", "ivysaur", "venusaur", "mr-mime", "type-null", "farfetchd", "flabebe", "giratina")

    private fun round(answer: String = "bulbasaur") = DailyRound(date = today, speciesName = answer, speciesId = 1)

    // --- Daily pick ---

    @Test
    fun `the same date always gives the same species`() {
        val first = game.dailyPick(today, allSpecies)

        assertEquals(first, game.dailyPick(today, allSpecies))
        assertEquals(first, game.dailyPick(LocalDate.of(2026, 10, 21), allSpecies.shuffled()))
    }

    @Test
    fun `different dates give different species, and none repeats until all have come up`() {
        val days = (0L until 1025L).map { game.dailyPick(today.plusDays(it), allSpecies) }

        assertNotEquals(days[0], days[1])
        assertEquals(1025, days.toSet().size)
        assertEquals(days[0], game.dailyPick(today.plusDays(1025), allSpecies))
    }

    @Test
    fun `forms are never picked`() {
        val picks = (0L until 1025L).map { game.dailyPick(today.plusDays(it), allSpecies).id }

        assertTrue(picks.all { it <= 1025 })
    }

    @Test
    fun `a new round starts with three hearts and no guesses`() {
        val round = game.newRound(today, allSpecies)

        assertEquals(today, round.date)
        assertEquals(game.dailyPick(today, allSpecies).name, round.speciesName)
        assertEquals(3, round.heartsLeft)
        assertEquals(RoundResult.Playing, round.result)
        assertTrue(round.wrongGuesses.isEmpty())
    }

    // --- Name matching ---

    @Test
    fun `matching ignores case, spaces, hyphens, dots, apostrophes and accents`() {
        assertTrue(game.matches("Mr. Mime", "mr-mime"))
        assertTrue(game.matches("  BULBASAUR ", "bulbasaur"))
        assertTrue(game.matches("Type: Null", "type-null"))
        assertTrue(game.matches("Farfetch’d", "farfetchd"))
        assertTrue(game.matches("Flabébé", "flabebe"))
        assertFalse(game.matches("Ivysaur", "bulbasaur"))
        assertFalse(game.matches(" - ", "bulbasaur"))
    }

    @Test
    fun `the answer is the species, so a species whose default Pokemon has a suffix matches its plain name`() {
        // PokeAPI's default Pokémon for the giratina species is "giratina-altered".
        val result = game.guess(round("giratina"), "Giratina", names)

        assertEquals(GuessOutcome.Correct, result.outcome)
        assertEquals(RoundResult.Won, result.round.result)
        assertEquals(GuessOutcome.NotAPokemon, game.guess(round(), "giratina-altered", names).outcome)
    }

    // --- Guesses ---

    @Test
    fun `a correct guess wins the round without costing a heart`() {
        val result = game.guess(round(), "Bulbasaur", names)

        assertEquals(GuessOutcome.Correct, result.outcome)
        assertEquals(RoundResult.Won, result.round.result)
        assertEquals(3, result.round.heartsLeft)
    }

    @Test
    fun `a wrong guess costs a heart and is listed`() {
        val result = game.guess(round(), "Ivysaur", names)

        assertEquals(GuessOutcome.Wrong("ivysaur"), result.outcome)
        assertEquals(2, result.round.heartsLeft)
        assertEquals(listOf("ivysaur"), result.round.wrongGuesses)
        assertEquals(RoundResult.Playing, result.round.result)
    }

    @Test
    fun `a name that isn't a Pokemon costs nothing`() {
        val start = round()
        val result = game.guess(start, "Agumon", names)

        assertEquals(GuessOutcome.NotAPokemon, result.outcome)
        assertSame(start, result.round)
    }

    @Test
    fun `a repeated guess costs nothing, however it is written`() {
        val afterOne = game.guess(round(), "Mr. Mime", names).round
        val result = game.guess(afterOne, "mr mime", names)

        assertEquals(GuessOutcome.AlreadyGuessed("mr-mime"), result.outcome)
        assertEquals(2, result.round.heartsLeft)
        assertEquals(listOf("mr-mime"), result.round.wrongGuesses)
    }

    @Test
    fun `the third wrong guess loses the round`() {
        var current = round()
        listOf("ivysaur", "venusaur", "mr-mime").forEach { current = game.guess(current, it, names).round }

        assertEquals(RoundResult.Lost, current.result)
        assertEquals(0, current.heartsLeft)
        assertEquals(listOf("ivysaur", "venusaur", "mr-mime"), current.wrongGuesses)
    }

    @Test
    fun `no replay after the round ends`() {
        val won = game.guess(round(), "bulbasaur", names).round
        var lost = round()
        listOf("ivysaur", "venusaur", "mr-mime").forEach { lost = game.guess(lost, it, names).round }

        assertEquals(GuessResult(won, GuessOutcome.RoundOver), game.guess(won, "bulbasaur", names))
        assertEquals(GuessResult(lost, GuessOutcome.RoundOver), game.guess(lost, "bulbasaur", names))
    }

    // --- Streak ---

    @Test
    fun `a win after yesterday's win raises the streak`() {
        val streak = Streak(count = 4, lastWonDate = today.minusDays(1))

        assertEquals(Streak(count = 5, lastWonDate = today), game.afterRound(streak, today, RoundResult.Won))
    }

    @Test
    fun `a first win starts the streak at one`() {
        assertEquals(Streak(count = 1, lastWonDate = today), game.afterRound(Streak(), today, RoundResult.Won))
    }

    @Test
    fun `the third miss resets the streak`() {
        val streak = Streak(count = 4, lastWonDate = today.minusDays(1))

        val after = game.afterRound(streak, today, RoundResult.Lost)

        assertEquals(0, after.count)
        assertEquals(0, game.currentStreak(after, today))
    }

    @Test
    fun `a missed day resets the streak`() {
        val streak = Streak(count = 4, lastWonDate = today.minusDays(2))

        assertEquals(0, game.currentStreak(streak, today))
        assertEquals(Streak(count = 1, lastWonDate = today), game.afterRound(streak, today, RoundResult.Won))
    }

    @Test
    fun `the streak still shows while today's round is in progress`() {
        val streak = Streak(count = 4, lastWonDate = today.minusDays(1))

        assertEquals(4, game.currentStreak(streak, today))
        assertEquals(4, game.currentStreak(streak.copy(lastWonDate = today), today))
    }
}
