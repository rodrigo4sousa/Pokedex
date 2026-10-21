package com.example.pokedex.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.domain.model.CaughtPokemon
import com.example.pokedex.domain.model.PokemonSummary
import com.example.pokedex.presentation.components.PokemonSort
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var allPokemons: List<PokemonSummary> = emptyList()

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    private val _sort = MutableLiveData(PokemonSort.Number)
    val sort: LiveData<PokemonSort> = _sort

    private val _selectedTypes = MutableLiveData<Set<String>>(emptySet())
    val selectedTypes: LiveData<Set<String>> = _selectedTypes

    // Members of each selected type, filled in as PokeAPI answers.
    private val typeMembers = mutableMapOf<String, Set<String>>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val caught: LiveData<List<CaughtPokemon>> = repository.getCaught()

    val caughtNames: LiveData<Set<String>> =
        caught.map { caught -> caught.map { it.pokemon.name }.toSet() }

    private val _pokemons = MutableLiveData<List<PokemonSummary>>(emptyList())
    val pokemons: LiveData<List<PokemonSummary>> = _pokemons

    init {
        viewModelScope.launch {
            try {
                allPokemons = repository.getAllPokemons()
                updateList()
            } catch (e: Exception) {
                _error.value = "Failed to load Pokémon"
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        updateList()
    }

    fun onSortChange(newSort: PokemonSort) {
        _sort.value = newSort
        updateList()
    }

    fun toggleType(type: String) {
        val selected = _selectedTypes.value.orEmpty()
        if (type in selected) {
            _selectedTypes.value = selected - type
            updateList()
            return
        }
        _selectedTypes.value = selected + type
        viewModelScope.launch {
            try {
                typeMembers[type] = repository.getPokemonNamesOfType(type)
            } catch (e: Exception) {
                _error.value = "Failed to load $type Pokémon"
                _selectedTypes.value = _selectedTypes.value.orEmpty() - type
            }
            updateList()
        }
    }

    fun clearTypes() {
        _selectedTypes.value = emptySet()
        updateList()
    }

    fun toggleCaught(pokemon: PokemonSummary) {
        viewModelScope.launch {
            if (pokemon.name in caughtNames.value.orEmpty()) {
                repository.releasePokemon(pokemon.name)
            } else {
                repository.catchPokemon(pokemon.name, pokemon.id)
            }
        }
    }

    private fun updateList() {
        val q = _query.value.orEmpty().trim()
        val types = _selectedTypes.value.orEmpty()
        val allowed = types.flatMap { typeMembers[it].orEmpty() }.toSet()
        val filtered = allPokemons.filter {
            (q.isEmpty() || it.name.contains(q, ignoreCase = true)) &&
                (types.isEmpty() || it.name in allowed)
        }

        // PokeAPI already lists Pokémon by number, so only the name sort needs work.
        _pokemons.value = when (_sort.value) {
            PokemonSort.Name -> filtered.sortedBy { it.name }
            else -> filtered
        }
    }
}