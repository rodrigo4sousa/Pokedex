package io.github.rodrigo4sousa.pokedex.presentation.pokedex

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.github.rodrigo4sousa.pokedex.data.PokemonRepository
import io.github.rodrigo4sousa.pokedex.domain.model.CaughtPokemon
import io.github.rodrigo4sousa.pokedex.domain.model.PokemonSummary
import io.github.rodrigo4sousa.pokedex.presentation.components.PokemonSort
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private val caught: LiveData<List<CaughtPokemon>> = repository.getCaught()

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val _sort = MutableLiveData(PokemonSort.Number)
    val sort: LiveData<PokemonSort> = _sort

    private val _selectedTypes = MutableLiveData<Set<String>>(emptySet())
    val selectedTypes: LiveData<Set<String>> = _selectedTypes

    // Members of each selected type, filled in as PokeAPI answers.
    private val typeMembers = MutableLiveData<Map<String, Set<String>>>(emptyMap())

    // Total species for the "Caught x / y" counter; stays null if PokeAPI can't be reached.
    private val _total = MutableLiveData<Int?>()
    val total: LiveData<Int?> = _total

    // Everything caught, ignoring the search.
    val caughtCount: LiveData<Int> = caught.map { it.size }

    // Caught Pokémon as cards, so the Pokédex can reuse the Home grid.
    val caughtPokemons: LiveData<List<PokemonSummary>> = MediatorLiveData<List<PokemonSummary>>().apply {
        fun update() {
            val all = caught.value ?: return
            val q = _query.value.orEmpty().trim()
            val types = _selectedTypes.value.orEmpty()
            val members = typeMembers.value.orEmpty()
            val allowed = types.flatMap { members[it].orEmpty() }.toSet()
            val sorted = when (_sort.value) {
                PokemonSort.Name -> all.sortedBy { it.pokemon.name }
                PokemonSort.DateCaught -> all.sortedByDescending { it.caughtAt }
                else -> all.sortedBy { it.pokemon.id }
            }
            value = sorted
                .map { it.pokemon }
                .filter { q.isEmpty() || it.name.contains(q, ignoreCase = true) }
                .filter { types.isEmpty() || it.name in allowed }
        }
        addSource(caught) { update() }
        addSource(_query) { update() }
        addSource(_sort) { update() }
        addSource(_selectedTypes) { update() }
        addSource(typeMembers) { update() }
    }

    init {
        viewModelScope.launch {
            try {
                _total.value = repository.getSpeciesCount()
            } catch (e: Exception) {
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onSortChange(newSort: PokemonSort) {
        _sort.value = newSort
    }

    fun toggleType(type: String) {
        val selected = _selectedTypes.value.orEmpty()
        if (type in selected) {
            _selectedTypes.value = selected - type
            return
        }
        _selectedTypes.value = selected + type
        viewModelScope.launch {
            try {
                typeMembers.value = typeMembers.value.orEmpty() + (type to repository.getPokemonNamesOfType(type))
            } catch (e: Exception) {
                _selectedTypes.value = _selectedTypes.value.orEmpty() - type
            }
        }
    }

    fun clearTypes() {
        _selectedTypes.value = emptySet()
    }

    fun release(pokemon: PokemonSummary) {
        viewModelScope.launch {
            repository.releasePokemon(pokemon.name)
        }
    }
}
