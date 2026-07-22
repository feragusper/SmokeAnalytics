plugins {
    // Apply the android-lib plugin for Android library modules.
    `android-lib`


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
    implementation(project(":libraries:architecture:domain"))

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
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

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
