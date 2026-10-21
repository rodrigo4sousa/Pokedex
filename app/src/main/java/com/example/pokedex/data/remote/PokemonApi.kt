package com.example.pokedex.data.remote

import com.example.pokedex.data.remote.dto.CountDto
import com.example.pokedex.data.remote.dto.EvolutionChainDto
import com.example.pokedex.data.remote.dto.PokemonDto
import com.example.pokedex.data.remote.dto.PokemonPageDto
import com.example.pokedex.data.remote.dto.PokemonSpeciesDto
import com.example.pokedex.data.remote.dto.TypeDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface PokemonApi {

    @GET("pokemon/{name}")
    suspend fun getPokemon(
        @Path("name") name: String
    ): PokemonDto


    @GET("pokemon")
    suspend fun getPokemonPage(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonPageDto


    // Species only: one entry per Pokédex number, without forms.
    @GET("pokemon-species")
    suspend fun getPokemonSpeciesPage(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonPageDto


    @GET("pokemon-species/{name}")
    suspend fun getPokemonSpecies(
        @Path("name") name: String
    ): PokemonSpeciesDto


    // Takes the full URL PokeAPI gives in the species response.
    @GET
    suspend fun getEvolutionChain(
        @Url url: String
    ): EvolutionChainDto


    @GET("type/{name}")
    suspend fun getType(
        @Path("name") name: String
    ): TypeDto


    // Only the total is used; limit=1 keeps the response tiny.
    @GET("pokemon-species?limit=1")
    suspend fun getPokemonSpeciesCount(): CountDto


}
