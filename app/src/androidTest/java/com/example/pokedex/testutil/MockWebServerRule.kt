package com.example.pokedex.testutil

import com.example.pokedex.testing.PokeApiDispatcher
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.ExternalResource

/**
 * Starts MockWebServer on [PORT] with the fixture dispatcher for each test. TestNetworkModule points
 * the app at http://localhost:[PORT]/.
 *
 * Rule order in tests: this rule first, then HiltAndroidRule, then the Compose rule, because the
 * activity (and its first network call) starts when the Compose rule starts.
 */
class MockWebServerRule : ExternalResource() {

    val dispatcher = PokeApiDispatcher()
    lateinit var server: MockWebServer
        private set

    override fun before() {
        server = MockWebServer().apply {
            dispatcher = this@MockWebServerRule.dispatcher
            start(PORT)
        }
    }

    override fun after() {
        server.shutdown()
    }

    companion object {
        const val PORT = 18080
    }
}
