package io.github.rodrigo4sousa.pokedex.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2: adds the guessing game's tables. caught_pokemon is untouched. The statements are the
 * ones Room exported in app/schemas/.../2.json, so the migrated file matches the v2 entities.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `daily_round` (`epochDay` INTEGER NOT NULL, " +
                "`speciesName` TEXT NOT NULL, `speciesId` INTEGER NOT NULL, `wrongGuesses` TEXT NOT NULL, " +
                "`heartsLeft` INTEGER NOT NULL, `result` TEXT NOT NULL, PRIMARY KEY(`epochDay`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `streak` (`id` INTEGER NOT NULL, `count` INTEGER NOT NULL, " +
                "`lastWonEpochDay` INTEGER, PRIMARY KEY(`id`))"
        )
    }
}
