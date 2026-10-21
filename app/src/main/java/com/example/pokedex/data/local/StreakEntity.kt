package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The guessing game's streak: a single row with id 0. */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val count: Int,
    val lastWonEpochDay: Long?
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
