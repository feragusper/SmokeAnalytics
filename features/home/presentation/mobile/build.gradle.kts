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
    namespace = "com.feragusper.smokeanalytics.features.home.presentation"

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

    // Authentication modules for user session management
    implementation(project(":libraries:authentication:domain"))

    // Home feature domain logic
    implementation(project(":features:home:domain"))

    // Smokes feature dependencies for data, domain, and presentation layers
    implementation(project(":libraries:smokes:data:mobile"))
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:smokes:presentation"))

    // Core AndroidX libraries and Compose dependencies
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.accompanist.swiperefresh)

    // Dependency injection with Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

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
