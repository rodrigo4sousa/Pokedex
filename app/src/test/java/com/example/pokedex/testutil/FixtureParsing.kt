package com.example.pokedex.testutil

import com.example.pokedex.testing.Fixtures
import com.google.gson.Gson

/** A recorded fixture parsed into [T] with the same Gson the app's GsonConverterFactory uses. */
inline fun <reified T> fixture(name: String): T = Gson().fromJson(Fixtures.read(name), T::class.java)
