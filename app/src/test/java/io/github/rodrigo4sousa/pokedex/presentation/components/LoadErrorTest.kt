package io.github.rodrigo4sousa.pokedex.presentation.components

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class LoadErrorTest {

    @Test
    fun `a network failure means no connection`() {
        assertEquals(LoadError.NoConnection, IOException("offline").toLoadError())
    }

    @Test
    fun `an HTTP error status means the server failed`() {
        val error = HttpException(Response.error<Any>(500, "".toResponseBody()))

        assertEquals(LoadError.Server, error.toLoadError())
    }

    @Test
    fun `anything else falls back to unknown`() {
        assertEquals(LoadError.Unknown, IllegalStateException("bug").toLoadError())
    }
}
