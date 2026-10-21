package com.example.pokedex.testing

/**
 * Recorded PokeAPI responses in src/main/resources/fixtures/, loaded through the classloader so
 * JVM and instrumented tests read them the same way. Names are relative, e.g. "pokemon/bulbasaur.json".
 */
object Fixtures {

    fun exists(name: String): Boolean = resource(name) != null

    fun read(name: String): String =
        requireNotNull(resource(name)) { "No fixture named $name" }.readText(Charsets.UTF_8)

    private fun resource(name: String) = Fixtures::class.java.classLoader.getResource("fixtures/$name")
}
