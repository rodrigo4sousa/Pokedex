package io.github.rodrigo4sousa.pokedex.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CaughtPokemonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pokemon: CaughtPokemonEntity)

    @Query("DELETE FROM caught_pokemon WHERE name = :name")
    suspend fun delete(name: String)

    @Query("SELECT EXISTS(SELECT 1 FROM caught_pokemon WHERE name = :name)")
    fun isCaught(name: String): LiveData<Boolean>

    @Query("SELECT * FROM caught_pokemon ORDER BY pokemonId")
    fun getAll(): LiveData<List<CaughtPokemonEntity>>
}
