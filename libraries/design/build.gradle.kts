plugins {
    // Use the predefined android-lib plugin for Android library modules.
    `android-lib`
    // Apply the Android Library plugin for building reusable UI components.
    id("com.android.library")
    // Apply the Kotlin Android plugin for using Kotlin in Android modules.
    id("org.jetbrains.kotlin.android")
    // Apply the Compose Compiler plugin using the version catalog alias.
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library.
    namespace = "com.feragusper.smokeanalytics.libraries.design"

    buildFeatures {
        // Enable Jetpack Compose support.
        compose = true
    }
}

dependencies {
    // Core AndroidX libraries bundle for base functionality.
    implementation(libs.bundles.androidx.base)
    // Material 3 design components for modern UI design.
    implementation(libs.material3)
    // Include a bundle of Jetpack Compose libraries.
    implementation(libs.bundles.compose)
}
