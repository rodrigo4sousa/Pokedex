package io.github.rodrigo4sousa.pokedex.testutil

import io.github.rodrigo4sousa.pokedex.data.remote.PokemonApi
import io.github.rodrigo4sousa.pokedex.data.remote.dto.CountDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.EvolutionChainDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.PokemonDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.PokemonPageDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.PokemonSpeciesDto
import io.github.rodrigo4sousa.pokedex.data.remote.dto.TypeDto
import io.github.rodrigo4sousa.pokedex.testing.Fixtures
import com.google.gson.Gson
import java.io.IOException

/**
 * PokemonApi backed by the recorded fixtures, parsed with the same Gson the app's
 * GsonConverterFactory uses. Missing fixtures throw like a 404 would.
 *
 * Each call is identified by its REST path ("pokemon/bulbasaur", "type/grass", "pokemon?" for the
 * list); [fail] makes every call whose path starts with a prefix throw, until [recover].
 */
class FakePokemonApi : PokemonApi {

    private val gson = Gson()
    private val failures = mutableMapOf<String, Throwable>()

    /** Paths requested so far, in order. */
    val requests = mutableListOf<String>()

    fun fail(pathPrefix: String, error: Throwable = IOException("Fake failure for $pathPrefix")) {
        failures[pathPrefix] = error
    }

    fun failAll(error: Throwable = IOException("Fake failure")) = fail("", error)

    /** Undoes every [fail], so a retry can succeed. */
    fun recover() = failures.clear()

    override suspend fun getPokemon(name: String): PokemonDto =
        load("pokemon/$name", "pokemon/$name.json")

    override suspend fun getPokemonPage(limit: Int, offset: Int): PokemonPageDto =
        load("pokemon?limit=$limit&offset=$offset", "pokemon-list.json")

    override suspend fun getPokemonSpeciesPage(limit: Int, offset: Int): PokemonPageDto =
        load("pokemon-species?limit=$limit&offset=$offset", "pokemon-species-list.json")

    override suspend fun getPokemonSpecies(name: String): PokemonSpeciesDto =
        load("pokemon-species/$name", "pokemon-species/$name.json")

    override suspend fun getEvolutionChain(url: String): EvolutionChainDto {
        val id = url.trimEnd('/').substringAfterLast('/')
        return load("evolution-chain/$id", "evolution-chain/$id.json")
    }

    override suspend fun getType(name: String): TypeDto =
        load("type/$name", "type/$name.json")

    override suspend fun getPokemonSpeciesCount(): CountDto =
        load("pokemon-species?limit=1", "pokemon-species-count.json")

    private inline fun <reified T> load(path: String, fixture: String): T {
        requests += path
        failures.entries.firstOrNull { path.startsWith(it.key) }?.let { throw it.value }
        if (!Fixtures.exists(fixture)) throw IOException("HTTP 404: no fixture for $path")
        return gson.fromJson(Fixtures.read(fixture), T::class.java)
    }
}
