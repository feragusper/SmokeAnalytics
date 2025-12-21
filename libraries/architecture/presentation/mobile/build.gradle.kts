plugins {
    // Use the predefined android-lib plugin from the project build script
    `android-lib`
    // Enable Kotlin annotation processing
    id("kotlin-kapt")
    // Apply Dagger Hilt plugin for dependency injection
    id("dagger.hilt.android.plugin")
    // Apply the Compose Compiler plugin using the version catalog alias
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library
    namespace = "com.feragusper.smokeanalytics.libraries.architecture.presentation"

    buildFeatures {
        // Enable Jetpack Compose support.
        compose = true
    }
}

dependencies {
    // Use the Compose BOM for consistent Compose library versions
    implementation(platform(libs.androidx.compose.bom))
    // Include a bundle of Jetpack Compose libraries
    implementation(libs.bundles.compose)
    // Include Dagger Hilt for dependency injection
    implementation(libs.hilt)
    // Include Timber for logging
    implementation(libs.timber)
    // Use Hilt's compiler for annotation processing
    kapt(libs.hilt.compiler)
}
