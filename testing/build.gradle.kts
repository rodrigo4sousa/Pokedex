import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Test-only module: recorded PokeAPI fixtures and the MockWebServer dispatcher that serves them.
// Used by both app/src/test and app/src/androidTest; holds no app classes.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(libs.okhttp.mockwebserver)
}
