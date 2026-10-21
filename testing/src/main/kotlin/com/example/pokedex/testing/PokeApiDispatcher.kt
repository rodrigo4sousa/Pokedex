package com.example.pokedex.testing

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.CopyOnWriteArrayList

/**
 * MockWebServer dispatcher that answers PokeAPI paths with the recorded [Fixtures]:
 * a path with no fixture gets a 404, and [failPath] makes matching paths answer 500.
 */
class PokeApiDispatcher : Dispatcher() {

    // The server thread writes these while the test thread reads them.
    private val failures = CopyOnWriteArrayList<String>()
    private val received = CopyOnWriteArrayList<String>()

    /** Paths received so far, query included (e.g. "/pokemon?limit=100000&offset=0"), in order. */
    val requests: List<String> get() = received

    /** Every later request whose path starts with [prefix] answers 500. */
    fun failPath(prefix: String) {
        failures += prefix
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path.orEmpty()
        received += path
        if (failures.any { path.startsWith(it) }) return MockResponse().setResponseCode(500)

        val fixture = fixtureFor(path) ?: return notFound()
        if (!Fixtures.exists(fixture)) return notFound()
        return MockResponse()
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(Fixtures.read(fixture))
    }

    companion object {

        /** The fixture file that answers [path], or null for a path PokeAPI wouldn't serve. */
        fun fixtureFor(path: String): String? {
            val route = path.substringBefore('?').trim('/')
            val hasQuery = '?' in path
            val parts = route.split('/')
            return when {
                route == "pokemon" && hasQuery -> "pokemon-list.json"
                route == "pokemon-species" && hasQuery -> "pokemon-species-count.json"
                parts.size == 2 && parts[0] in RESOURCES -> "${parts[0]}/${parts[1]}.json"
                else -> null
            }
        }

        private val RESOURCES = setOf("pokemon", "pokemon-species", "evolution-chain", "type")

        private fun notFound() = MockResponse().setResponseCode(404).setBody("Not Found")
    }
}
