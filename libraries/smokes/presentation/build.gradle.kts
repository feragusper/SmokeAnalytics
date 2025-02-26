plugins {
    // Use the predefined android-lib plugin for Android library modules.
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
    namespace = "com.feragusper.smokeanalytics.libraries.smokes.presentation"
}

dependencies {
    // Include the design library for consistent theming and UI components.
    implementation(project(":libraries:design"))
    // Include the architecture domain module for shared domain logic.
    implementation(project(":libraries:architecture:domain"))

    // Use the Compose BOM for consistent Compose library versions.
    implementation(platform(libs.androidx.compose.bom))
    // Include a bundle of Jetpack Compose libraries.
    implementation(libs.bundles.compose)
    // Include Material3 components for modern UI design.
    implementation(libs.material3)

    // Dagger Hilt dependencies for dependency injection.
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
}
