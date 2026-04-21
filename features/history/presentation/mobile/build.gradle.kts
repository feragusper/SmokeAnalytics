plugins {
    // Use the predefined android-lib plugin for Android library modules.
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
    namespace = "com.feragusper.smokeanalytics.features.history.presentation"

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
    // Authentication feature
    implementation(project(":features:authentication:presentation:mobile"))
    implementation(project(":features:home:domain"))

    // Architecture and presentation layers
    implementation(project(":libraries:architecture:presentation:mobile"))
    implementation(project(":libraries:architecture:domain"))

    // Authentication modules
    implementation(project(":libraries:authentication:domain"))
    implementation(project(":libraries:preferences:domain"))

    // Design system for consistent theming and UI components
    implementation(project(":libraries:design:mobile"))

    // Smoke feature dependencies
    implementation(project(":libraries:smokes:data:mobile"))
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:smokes:presentation"))

    // Core AndroidX libraries and Compose dependencies
    implementation(libs.bundles.androidx.base)
    implementation(libs.timber)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.bundles.androidx.navigation)

    // Dependency injection with Hilt
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(libs.compose.shimmer)

    // Unit testing dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Debug-specific dependencies for Compose UI tooling
    debugImplementation(libs.bundles.compose.debug)

    // Android Instrumentation Testing
    androidTestImplementation(libs.bundles.compose.test)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.bundles.android.test)
}
