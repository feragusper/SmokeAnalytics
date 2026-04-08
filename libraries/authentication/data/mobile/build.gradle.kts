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
    namespace = "com.feragusper.smokeanalytics.libraries.authentication.data"
}

dependencies {
    // Include the domain layer of the authentication library as a dependency.
    implementation(project(":libraries:authentication:domain"))

    // Dagger Hilt dependencies for dependency injection.
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    // Firebase Authentication dependencies.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Compose runtime for state management and composition.
    implementation(libs.androidx.compose.runtime)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
