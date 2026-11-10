package io.github.rodrigo4sousa.pokedex.presentation.guess

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.rodrigo4sousa.pokedex.data.GuessGameRepository
import io.github.rodrigo4sousa.pokedex.data.PokemonRepository
import io.github.rodrigo4sousa.pokedex.data.local.StreakEntity
import io.github.rodrigo4sousa.pokedex.domain.game.GuessGame
import io.github.rodrigo4sousa.pokedex.presentation.components.LoadError
import io.github.rodrigo4sousa.pokedex.testutil.FakeCaughtPokemonDao
import io.github.rodrigo4sousa.pokedex.testutil.FakeGuessGameDao
import io.github.rodrigo4sousa.pokedex.testutil.FakePokemonApi
import io.github.rodrigo4sousa.pokedex.testutil.MainDispatcherRule
import io.github.rodrigo4sousa.pokedex.testutil.getOrAwaitValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The species list fixture has 9 species. With the game's fixed shuffle, 2026-10-27 picks Bulbasaur,
 * 2026-10-28 Eevee and 2026-10-23 Giratina (whose default Pokémon is giratina-altered).
 */
class GuessViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakePokemonApi()
    private val gameDao = FakeGuessGameDao()

    private val bulbasaurDay = LocalDate.of(2026, 10, 27)
    private val artworkBase = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"

    private fun viewModel(date: LocalDate = bulbasaurDay) = GuessViewModel(
        pokemonRepository = PokemonRepository(api, FakeCaughtPokemonDao()),
        gameRepository = GuessGameRepository(gameDao),
        game = GuessGame(),
        clock = Clock.fixed(date.atTime(12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    )

    private fun GuessViewModel.playing() = uiState.getOrAwaitValue() as GuessUiState.Playing
    private fun GuessViewModel.finished() = uiState.getOrAwaitValue() as GuessUiState.Finished

    private fun streakWonOn(date: LocalDate, count: Int) {
        gameDao.streak = StreakEntity(count = count, lastWonEpochDay = date.toEpochDay())
    }

    @Test
    fun `today's Pokemon loads as a silhouette with three hearts, and the pick is saved`() {
        val state = viewModel().playing()

        assertEquals(
            GuessUiState.Playing(
                imageUrl = "${artworkBase}1.png",
                fallbackImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
                wrongGuesses = emptyList(),
                heartsLeft = 3,
                streak = 0,
                message = null
            ),
            state
        )
        assertEquals("bulbasaur", gameDao.rounds.getValue(bulbasaurDay.toEpochDay()).speciesName)
    }

    @Test
    fun `a correct guess reveals the Pokemon, raises the streak and saves both`() {
        val viewModel = viewModel()

        viewModel.guess(" BULBA-saur ")

        val state = viewModel.finished()
        assertEquals(true, state.won)
        assertEquals("bulbasaur", state.speciesName)
        assertEquals("bulbasaur", state.pokemonName)
        assertEquals("${artworkBase}1.png", state.imageUrl)
        assertEquals(1, state.streak)
        assertEquals("Won", gameDao.rounds.getValue(bulbasaurDay.toEpochDay()).result)
        assertEquals(StreakEntity(count = 1, lastWonEpochDay = bulbasaurDay.toEpochDay()), gameDao.streak)
    }

    @Test
    fun `the answer is the species name, and the detail opens its default Pokemon`() {
        val viewModel = viewModel(LocalDate.of(2026, 10, 23))

        viewModel.guess("Giratina")

        val state = viewModel.finished()
        assertEquals("giratina", state.speciesName)
        assertEquals("giratina-altered", state.pokemonName)
        assertEquals("${artworkBase}487.png", state.imageUrl)
    }

    @Test
    fun `three wrong guesses reveal the Pokemon and reset the streak`() {
        streakWonOn(bulbasaurDay.minusDays(1), count = 4)
        val viewModel = viewModel()

        viewModel.guess("Ivysaur")
        assertEquals(2, viewModel.playing().heartsLeft)
        viewModel.guess("Venusaur")
        viewModel.guess("Tauros")

        val state = viewModel.finished()
        assertEquals(false, state.won)
        assertEquals("bulbasaur", state.speciesName)
        assertEquals(listOf("ivysaur", "venusaur", "tauros"), state.wrongGuesses)
        assertEquals(0, state.streak)
        assertEquals(0, gameDao.streak!!.count)
    }

    @Test
    fun `a name that isn't a Pokemon, or one already guessed, shows a message and costs nothing`() {
        val viewModel = viewModel()

        viewModel.guess("Agumon")
        assertEquals(GuessMessage.NotAPokemon("Agumon"), viewModel.playing().message)
        assertEquals(3, viewModel.playing().heartsLeft)

        viewModel.guess("ivysaur")
        assertEquals(null, viewModel.playing().message)
        viewModel.guess("Ivy Saur")

        val state = viewModel.playing()
        assertEquals(GuessMessage.AlreadyGuessed("ivysaur"), state.message)
        assertEquals(2, state.heartsLeft)
        assertEquals(listOf("ivysaur"), state.wrongGuesses)
    }

    @Test
    fun `winning on consecutive days raises the streak each day`() {
        viewModel(bulbasaurDay).guess("bulbasaur")

        val nextDay = viewModel(bulbasaurDay.plusDays(1))
        assertEquals(1, nextDay.playing().streak)
        nextDay.guess("eevee")

        assertEquals(2, nextDay.finished().streak)
    }

    @Test
    fun `missing a day resets the streak`() {
        streakWonOn(bulbasaurDay.minusDays(2), count = 5)
        val viewModel = viewModel()

        assertEquals(0, viewModel.playing().streak)
        viewModel.guess("bulbasaur")

        assertEquals(1, viewModel.finished().streak)
    }

    @Test
    fun `the round survives a restart, and a finished round can't be played again`() {
        viewModel().guess("ivysaur")

        val restarted = viewModel()
        assertEquals(listOf("ivysaur"), restarted.playing().wrongGuesses)
        assertEquals(2, restarted.playing().heartsLeft)
        restarted.guess("bulbasaur")

        val nextLaunch = viewModel()
        val finished = nextLaunch.finished()
        nextLaunch.guess("ivysaur")

        assertEquals(finished, nextLaunch.finished())
        assertEquals(1, gameDao.streak!!.count)
    }

    @Test
    fun `a failed load shows the error, and retry loads the round`() {
        api.fail("pokemon-species?")
        val viewModel = viewModel()
        assertEquals(GuessUiState.Error(LoadError.NoConnection), viewModel.uiState.getOrAwaitValue())

        api.recover()
        viewModel.retry()

        assertEquals(3, viewModel.playing().heartsLeft)
    }
}
