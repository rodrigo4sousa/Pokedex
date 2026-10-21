package com.example.pokedex.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.pokedex.data.local.CaughtPokemonDao
import com.example.pokedex.data.mapper.caughtPokemonEntity
import com.example.pokedex.data.mapper.memberNames
import com.example.pokedex.data.mapper.toDamageMultipliers
import com.example.pokedex.data.mapper.toDomain
import com.example.pokedex.data.mapper.toPokemonSummary
import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.domain.model.CaughtPokemon
import com.example.pokedex.domain.model.EvolutionStage
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.domain.model.PokemonSummary
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Single access point to Pokémon data. Takes DTOs and entities in, gives domain models out. */
@Singleton
class PokemonRepository @Inject constructor(
    private val api: PokemonApi,
    private val caughtPokemonDao: CaughtPokemonDao
) {

    // Type -> names of its Pokémon; a type's members don't change, so each is fetched once.
    private val typeMembers = mutableMapOf<String, Set<String>>()

    suspend fun getPokemon(name: String): Pokemon = api.getPokemon(name).toDomain()

    suspend fun getAllPokemons(): List<PokemonSummary> =
        api.getPokemonPage(limit = 100000, offset = 0).results.map { it.toPokemonSummary() }

    /** Number of species in the National Pokédex (1025 as of Gen 9); forms aren't counted. */
    suspend fun getSpeciesCount(): Int = api.getPokemonSpeciesCount().count

    suspend fun getEvolutionChain(speciesName: String): EvolutionStage? {
        val chainUrl = api.getPokemonSpecies(speciesName).evolution_chain?.url ?: return null
        return api.getEvolutionChain(chainUrl).chain.toDomain()
    }

    /**
     * Damage multiplier each attacking type deals to a Pokémon with these types,
     * e.g. Grass/Poison takes 4× from nothing, 2× from Fire and ½× from Water.
     * Neutral (1×) types are left out.
     */
    suspend fun getTypeEffectiveness(types: List<String>): Map<String, Double> {
        val relations = coroutineScope {
            types.map { type -> async { api.getType(type).damage_relations } }.awaitAll()
        }
        return relations.toDamageMultipliers()
    }

    suspend fun getPokemonNamesOfType(type: String): Set<String> =
        typeMembers[type] ?: api.getType(type).memberNames()
            .also { typeMembers[type] = it }

    fun isCaught(name: String): LiveData<Boolean> = caughtPokemonDao.isCaught(name)

    /** Every caught Pokémon, ordered by Pokédex number; updates after each catch or release. */
    fun getCaught(): LiveData<List<CaughtPokemon>> =
        caughtPokemonDao.getAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun catchPokemon(name: String, id: Int) {
        caughtPokemonDao.insert(caughtPokemonEntity(name = name, id = id))
    }

    suspend fun releasePokemon(name: String) {
        caughtPokemonDao.delete(name)
    }
}
