package io.github.rodrigo4sousa.pokedex.testutil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import io.github.rodrigo4sousa.pokedex.data.local.CaughtPokemonDao
import io.github.rodrigo4sousa.pokedex.data.local.CaughtPokemonEntity

/**
 * In-memory CaughtPokemonDao. Behaves like the Room one: insert ignores a name that is already
 * caught, getAll() is ordered by pokemonId, and every LiveData it returns updates after each change.
 * Needs InstantTaskExecutorRule (LiveData updates are posted).
 */
class FakeCaughtPokemonDao : CaughtPokemonDao {

    private val rows = linkedMapOf<String, CaughtPokemonEntity>()
    private val all = MutableLiveData<List<CaughtPokemonEntity>>(emptyList())

    override suspend fun insert(pokemon: CaughtPokemonEntity) {
        if (pokemon.name in rows) return
        rows[pokemon.name] = pokemon
        publish()
    }

    override suspend fun delete(name: String) {
        if (rows.remove(name) != null) publish()
    }

    override fun isCaught(name: String): LiveData<Boolean> = all.map { list -> list.any { it.name == name } }

    override fun getAll(): LiveData<List<CaughtPokemonEntity>> = all

    private fun publish() {
        all.postValue(rows.values.sortedBy { it.pokemonId })
    }
}
