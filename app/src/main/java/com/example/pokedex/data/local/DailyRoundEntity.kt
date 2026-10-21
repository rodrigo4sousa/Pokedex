package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One day of the guessing game, keyed by LocalDate.toEpochDay(). */
@Entity(tableName = "daily_round")
data class DailyRoundEntity(
    @PrimaryKey val epochDay: Long,
    val speciesName: String,
    val speciesId: Int,
    /** Wrong guesses, one species name per line. */
    val wrongGuesses: String,
    val heartsLeft: Int,
    /** RoundResult name: Playing, Won or Lost. */
    val result: String
)
