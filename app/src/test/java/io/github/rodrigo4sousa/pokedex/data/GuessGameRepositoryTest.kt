package io.github.rodrigo4sousa.pokedex.data

import io.github.rodrigo4sousa.pokedex.data.local.DailyRoundEntity
import io.github.rodrigo4sousa.pokedex.domain.model.DailyRound
import io.github.rodrigo4sousa.pokedex.domain.model.RoundResult
import io.github.rodrigo4sousa.pokedex.domain.model.Streak
import io.github.rodrigo4sousa.pokedex.testutil.FakeGuessGameDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GuessGameRepositoryTest {

    private val dao = FakeGuessGameDao()
    private val repository = GuessGameRepository(dao)
    private val today = LocalDate.of(2026, 10, 21)

    @Test
    fun `a round is saved under its date and read back unchanged`() = runTest {
        val round = DailyRound(
            date = today,
            speciesName = "giratina",
            speciesId = 487,
            wrongGuesses = listOf("mr-mime", "type-null"),
            heartsLeft = 1,
            result = RoundResult.Playing
        )

        repository.saveRound(round)

        assertEquals(
            DailyRoundEntity(today.toEpochDay(), "giratina", 487, "mr-mime\ntype-null", 1, "Playing"),
            dao.rounds[today.toEpochDay()]
        )
        assertEquals(round, repository.getRound(today))
        assertNull(repository.getRound(today.plusDays(1)))
    }

    @Test
    fun `a round without wrong guesses reads back with an empty list`() = runTest {
        val round = DailyRound(date = today, speciesName = "eevee", speciesId = 133, result = RoundResult.Won)

        repository.saveRound(round)

        assertEquals(emptyList<String>(), repository.getRound(today)!!.wrongGuesses)
    }

    @Test
    fun `saving the round again replaces it`() = runTest {
        val round = DailyRound(date = today, speciesName = "eevee", speciesId = 133)
        repository.saveRound(round)

        repository.saveRound(round.copy(heartsLeft = 2, wrongGuesses = listOf("tauros")))

        assertEquals(1, dao.rounds.size)
        assertEquals(2, repository.getRound(today)!!.heartsLeft)
    }

    @Test
    fun `the streak starts empty and keeps its last win`() = runTest {
        assertEquals(Streak(), repository.getStreak())

        repository.saveStreak(Streak(count = 3, lastWonDate = today))

        assertEquals(Streak(count = 3, lastWonDate = today), repository.getStreak())
    }
}
