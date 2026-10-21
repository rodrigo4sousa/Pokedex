package com.example.pokedex

import android.app.Application
import android.content.Context
import android.graphics.Color
import androidx.test.runner.AndroidJUnitRunner
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.test.FakeImageLoaderEngine
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Runs instrumented tests in HiltTestApplication, so @HiltAndroidTest classes can swap modules
 * (TestNetworkModule, TestDatabaseModule). Also installs a fake Coil engine as the singleton
 * ImageLoader, so no sprite is ever downloaded during tests.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        val engine = FakeImageLoaderEngine.Builder()
            .default(ColorImage(Color.LTGRAY))
            .build()
        SingletonImageLoader.setUnsafe(
            ImageLoader.Builder(app)
                .components { add(engine) }
                .build()
        )
    }
}
