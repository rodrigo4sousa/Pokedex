package io.github.rodrigo4sousa.pokedex.testutil

import io.github.rodrigo4sousa.pokedex.data.local.DailyRoundEntity
import io.github.rodrigo4sousa.pokedex.data.local.GuessGameDao
import io.github.rodrigo4sousa.pokedex.data.local.StreakEntity

/** In-memory GuessGameDao. Behaves like the Room one: saving a row with an existing key replaces it. */
class FakeGuessGameDao : GuessGameDao {

    val rounds = linkedMapOf<Long, DailyRoundEntity>()
    var streak: StreakEntity? = null

    override suspend fun getRound(epochDay: Long): DailyRoundEntity? = rounds[epochDay]

    override suspend fun saveRound(round: DailyRoundEntity) {
        rounds[round.epochDay] = round
    }

    override suspend fun getStreak(): StreakEntity? = streak

    override suspend fun saveStreak(streak: StreakEntity) {
        this.streak = streak
    }
}
