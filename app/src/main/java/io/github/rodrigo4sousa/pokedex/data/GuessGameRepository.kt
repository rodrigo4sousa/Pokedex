package io.github.rodrigo4sousa.pokedex.data

import io.github.rodrigo4sousa.pokedex.data.local.GuessGameDao
import io.github.rodrigo4sousa.pokedex.data.mapper.toDomain
import io.github.rodrigo4sousa.pokedex.data.mapper.toEntity
import io.github.rodrigo4sousa.pokedex.domain.model.DailyRound
import io.github.rodrigo4sousa.pokedex.domain.model.Streak
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate

/** The guessing game's saved state in Room: one round per day and the streak. */
@Singleton
class GuessGameRepository @Inject constructor(
    private val dao: GuessGameDao
) {

    suspend fun getRound(date: LocalDate): DailyRound? = dao.getRound(date.toEpochDay())?.toDomain()

    suspend fun saveRound(round: DailyRound) {
        dao.saveRound(round.toEntity())
    }

    /** The saved streak, or an empty one before the first round ends. */
    suspend fun getStreak(): Streak = dao.getStreak()?.toDomain() ?: Streak()

    suspend fun saveStreak(streak: Streak) {
        dao.saveStreak(streak.toEntity())
    }
}
