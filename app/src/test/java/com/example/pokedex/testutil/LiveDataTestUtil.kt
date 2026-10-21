package com.example.pokedex.testutil

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Observes the LiveData until it has a value and returns it. Use with InstantTaskExecutorRule,
 * so LiveData updates run synchronously on the test thread.
 */
fun <T> LiveData<T>.getOrAwaitValue(time: Long = 2, unit: TimeUnit = TimeUnit.SECONDS): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    observeForever(observer)
    try {
        if (!latch.await(time, unit)) throw TimeoutException("LiveData value was never set.")
    } finally {
        removeObserver(observer)
    }
    @Suppress("UNCHECKED_CAST")
    return data as T
}

/**
 * Keeps the LiveData observed while [block] runs, the way a screen would. Needed when the code under
 * test reads `.value` of a mapped LiveData (e.g. HomeViewModel.toggleCaught reads caughtNames).
 */
fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
    val observer = Observer<T> { }
    observeForever(observer)
    try {
        block()
    } finally {
        removeObserver(observer)
    }
}
