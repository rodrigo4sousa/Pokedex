package com.example.pokedex.di

import com.example.pokedex.data.remote.NetworkModule
import com.example.pokedex.data.remote.PokemonApi
import com.example.pokedex.data.remote.pokeApiRetrofit
import com.example.pokedex.testutil.MockWebServerRule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import jakarta.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Replaces NetworkModule in instrumented tests: Retrofit talks to MockWebServer on localhost.
 *
 * PokeAPI responses contain absolute URLs (the evolution chain in the species response is called
 * through @Url), so an interceptor also sends any https://pokeapi.co/api/v2/... request to the mock
 * server. No test reaches the real PokeAPI.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {

    private val BASE_URL = "http://localhost:${MockWebServerRule.PORT}/"

    private val redirectPokeApi = Interceptor { chain ->
        val url = chain.request().url
        val target = if (url.host == "pokeapi.co") {
            url.newBuilder()
                .scheme("http")
                .host("localhost")
                .port(MockWebServerRule.PORT)
                .encodedPath(url.encodedPath.removePrefix("/api/v2"))
                .build()
        } else {
            url
        }
        chain.proceed(chain.request().newBuilder().url(target).build())
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(redirectPokeApi).build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = pokeApiRetrofit(BASE_URL, client)

    @Provides
    @Singleton
    fun providePokemonApi(retrofit: Retrofit): PokemonApi = retrofit.create(PokemonApi::class.java)
}
