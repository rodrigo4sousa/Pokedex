package com.example.pokedex.presentation.pokemonDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.PokemonRepository
import com.example.pokedex.domain.model.EvolutionStage
import com.example.pokedex.domain.model.Pokemon
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val name: String = checkNotNull(savedStateHandle["name"])

    private val _pokemon = MutableLiveData<Pokemon>()
    val pokemon: LiveData<Pokemon> = _pokemon

    // Keyed on the loaded Pokémon's name, not the nav argument: the evolution chain opens
    // pages by Pokédex number, which would never match the name stored when catching.
    val isCaught: LiveData<Boolean> = _pokemon.switchMap { pokemonRepository.isCaught(it.name) }

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _evolutionChain = MutableLiveData<EvolutionStage?>()
    val evolutionChain: LiveData<EvolutionStage?> = _evolutionChain

    private val _typeEffectiveness = MutableLiveData<Map<String, Double>>()
    val typeEffectiveness: LiveData<Map<String, Double>> = _typeEffectiveness

    init {
        loadPokemon()
    }

    fun loadPokemon() {
        viewModelScope.launch {
            val pokemon = try {
                pokemonRepository.getPokemon(name)
            } catch (e: Exception) {
                _error.value = "No Pokemon found!"
                return@launch
            }
            _pokemon.value = pokemon

            // Extra sections load after the main page; if one fails it just stays hidden.
            launch {
                try {
                    _evolutionChain.value = pokemonRepository.getEvolutionChain(pokemon.speciesName)
                } catch (e: Exception) {
                }
            }
            launch {
                try {
                    _typeEffectiveness.value =
                        pokemonRepository.getTypeEffectiveness(pokemon.types)
                } catch (e: Exception) {
                }
            }
        }
    }

    fun toggleCaught() {
        val pokemon = _pokemon.value ?: return
        viewModelScope.launch {
            if (isCaught.value == true) {
                pokemonRepository.releasePokemon(pokemon.name)
            } else {
                pokemonRepository.catchPokemon(pokemon.name, pokemon.id)
            }
        }
    }
}
