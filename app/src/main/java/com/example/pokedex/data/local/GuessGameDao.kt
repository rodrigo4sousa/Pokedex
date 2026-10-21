package com.example.pokedex.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface GuessGameDao {

    @Query("SELECT * FROM daily_round WHERE epochDay = :epochDay")
    suspend fun getRound(epochDay: Long): DailyRoundEntity?

    @Upsert
    suspend fun saveRound(round: DailyRoundEntity)

    @Query("SELECT * FROM streak WHERE id = ${StreakEntity.SINGLE_ROW_ID}")
    suspend fun getStreak(): StreakEntity?

    @Upsert
    suspend fun saveStreak(streak: StreakEntity)
}
