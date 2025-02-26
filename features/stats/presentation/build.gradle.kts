plugins {
    // Apply the android-lib plugin for Android library modules.
    `android-lib`

    // Enable Kotlin annotation processing.
    id("kotlin-kapt")

    // Apply Dagger Hilt plugin for dependency injection.
    id("dagger.hilt.android.plugin")

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
}

dependencies {
    // Architecture and presentation layers
    implementation(project(":libraries:architecture:presentation"))

    // Design system for consistent theming and UI components
    implementation(project(":libraries:design"))

    // Domain layer for accessing smoke-related data
    implementation(project(":libraries:smokes:domain"))

    // Chart library for visualizing statistics
    implementation(project(":libraries:chart"))

    // Core AndroidX libraries and Compose dependencies
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.bundles.androidx.navigation)

    // Dependency injection with Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
}
