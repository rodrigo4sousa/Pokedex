plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Version: -PappVersionName=1.2.3 wins (the release workflow passes the tag); otherwise the latest vX.Y.Z git
// tag; otherwise 0.0.0-dev. The name is MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-rcN, and versionCode is computed
// from it in buildSrc/AppVersion.kt (1.2.3-rc1 is 10200301, 1.2.3 is 10200399). Other names fail the build.
val appVersionName: String = providers.gradleProperty("appVersionName")
    .orElse(
        providers.exec {
            commandLine("git", "describe", "--tags", "--abbrev=0", "--match", "v[0-9]*")
            isIgnoreExitValue = true
        }.standardOutput.asText.map { it.trim().removePrefix("v") }
    )
    .map { it.ifEmpty { AppVersion.DEV_NAME } }
    .get()
val appVersionCode: Int = AppVersion.code(appVersionName)

// Release signing comes from environment variables or Gradle properties, never from files in the repo.
// Without all four, the release build is simply left unsigned.
fun signingValue(name: String): String? =
    providers.environmentVariable(name).orElse(providers.gradleProperty(name)).orNull

android {
    namespace = "io.github.rodrigo4sousa.pokedex"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.rodrigo4sousa.pokedex"
        minSdk = 31
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "io.github.rodrigo4sousa.pokedex.HiltTestRunner"
    }

    val releaseKeystore = signingValue("KEYSTORE_FILE")
    val releaseStorePassword = signingValue("KEYSTORE_PASSWORD")
    val releaseKeyAlias = signingValue("KEY_ALIAS")
    val releaseKeyPassword = signingValue("KEY_PASSWORD")
    val canSignRelease = listOf(releaseKeystore, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
        .all { !it.isNullOrEmpty() }

    signingConfigs {
        if (canSignRelease) {
            create("release") {
                storeFile = file(releaseKeystore!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // R8 shrinks and obfuscates; the keep rules for Gson DTOs are in src/main/keepRules/rules.keep.
            optimization {
                enable = true
            }
            if (canSignRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    // MigrationTestHelper reads the exported Room schemas as androidTest assets.
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }
}

// Room writes each database version's schema here; the JSON files are committed.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    // JVM tests (src/test): unit and JVM integration
    testImplementation(project(":testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)

    // Instrumented tests (src/androidTest): device integration and system
    androidTestImplementation(project(":testing"))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.coil.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    constraints {
        // room-testing's room-migration reads schemas with kotlinx-serialization-json 1.8.1. Test APKs must use
        // the app's versions, so the app needs core 1.8.1 too (Navigation brings 1.7.3); otherwise
        // MigrationTestHelper fails with AbstractMethodError.
        implementation(libs.kotlinx.serialization.core) {
            because("align with kotlinx-serialization-json 1.8.1 used by room-testing")
        }
    }
}