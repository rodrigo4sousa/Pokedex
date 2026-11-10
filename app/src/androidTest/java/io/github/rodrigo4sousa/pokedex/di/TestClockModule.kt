package io.github.rodrigo4sousa.pokedex.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Replaces ClockModule in instrumented tests: "today" is always [TODAY], so the daily Pokémon is
 * known. With the recorded species list, that day's Pokémon is Bulbasaur.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ClockModule::class])
object TestClockModule {

    val TODAY: LocalDate = LocalDate.of(2026, 10, 27)

    @Provides
    fun provideClock(): Clock = Clock.fixed(TODAY.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
}
