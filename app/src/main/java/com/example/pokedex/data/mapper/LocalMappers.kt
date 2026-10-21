package com.example.pokedex.data.mapper

import com.example.pokedex.data.local.CaughtPokemonEntity
import com.example.pokedex.data.local.DailyRoundEntity
import com.example.pokedex.data.local.StreakEntity
import com.example.pokedex.domain.model.CaughtPokemon
import com.example.pokedex.domain.model.DailyRound
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.domain.model.RoundResult
import com.example.pokedex.domain.model.Streak
import java.time.LocalDate

fun CaughtPokemonEntity.toDomain(): CaughtPokemon = CaughtPokemon(
    pokemon = PokemonSummary(id = pokemonId, name = name, spriteUrl = spriteUrlFor(pokemonId)),
    caughtAt = caughtAt
)

/** The row stored when a Pokémon is caught; caughtAt defaults to now. */
fun caughtPokemonEntity(name: String, id: Int): CaughtPokemonEntity =
    CaughtPokemonEntity(name = name, pokemonId = id)

fun DailyRoundEntity.toDomain(): DailyRound = DailyRound(
    date = LocalDate.ofEpochDay(epochDay),
    speciesName = speciesName,
    speciesId = speciesId,
    wrongGuesses = wrongGuesses.split('\n').filter { it.isNotEmpty() },
    heartsLeft = heartsLeft,
    result = RoundResult.valueOf(result)
)

fun DailyRound.toEntity(): DailyRoundEntity = DailyRoundEntity(
    epochDay = date.toEpochDay(),
    speciesName = speciesName,
    speciesId = speciesId,
    wrongGuesses = wrongGuesses.joinToString("\n"),
    heartsLeft = heartsLeft,
    result = result.name
)

fun StreakEntity.toDomain(): Streak = Streak(
    count = count,
    lastWonDate = lastWonEpochDay?.let(LocalDate::ofEpochDay)
)

fun Streak.toEntity(): StreakEntity = StreakEntity(
    count = count,
    lastWonEpochDay = lastWonDate?.toEpochDay()
)
