plugins {
    // Use the predefined android-lib plugin from the project build script
    `android-lib`
    // Apply the Compose Compiler plugin using the version catalog alias
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library
    namespace = "com.feragusper.smokeanalytics.libraries.architecture.presentation"

    buildFeatures {
        buildConfig = true
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
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    // Include Timber for logging
    implementation(libs.timber)
    // Use Hilt's compiler for annotation processing
}
