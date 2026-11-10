package io.github.rodrigo4sousa.pokedex.testutil

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Observes the LiveData until a value matches [predicate] and returns it. Room notifies observers
 * asynchronously after a write, so device tests wait for the expected state instead of the first value.
 * Use with InstantTaskExecutorRule. (The JVM copy in src/test returns the first value.)
 */
fun <T> LiveData<T>.awaitValue(
    timeoutSeconds: Long = 5,
    predicate: (T) -> Boolean = { true }
): T {
    var match: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> { value ->
        if (predicate(value)) {
            match = value
            latch.countDown()
        }
    }
    observeForever(observer)
    try {
        if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
            throw TimeoutException("No LiveData value matched within $timeoutSeconds s (last: $value)")
        }
    } finally {
        removeObserver(observer)
    }
    @Suppress("UNCHECKED_CAST")
    return match as T
}
