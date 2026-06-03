plugins {
    // Apply the android-lib plugin for Android library modules.
    `android-lib`
    // Enable Kotlin annotation processing.
    id("com.google.devtools.ksp")

    // Apply Dagger Hilt plugin for dependency injection.
    id("com.google.dagger.hilt.android")

    // Apply the Compose Compiler plugin using the version catalog alias.
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library.
    namespace = "com.feragusper.smokeanalytics.features.stats.presentation"

    defaultConfig {
        // Specify the test instrumentation runner for Android tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        // Enable Jetpack Compose support.
        compose = true
    }
}

dependencies {
    // Architecture and presentation layers
    implementation(project(":libraries:architecture:presentation:mobile"))

    // Design system for consistent theming and UI components
    implementation(project(":libraries:design:mobile"))

    // Domain layer for accessing smoke-related data
    implementation(project(":libraries:preferences:domain"))
    implementation(project(":libraries:smokes:domain"))

    // Core AndroidX libraries and Compose dependencies
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.bundles.androidx.navigation)

    // Dependency injection with Hilt
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.views)
    implementation(libs.compose.shimmer)

    // Unit testing dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
