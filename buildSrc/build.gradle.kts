// buildSrc/build.gradle.kts
// This build script sets up the buildSrc module using the Kotlin DSL.
// It defines repositories and dependencies required to work with the version catalog
// and common Gradle plugins used throughout the project.

plugins {
    // Apply the Kotlin DSL plugin to enable Kotlin-based build scripts.
    `kotlin-dsl`
}

repositories {
    // Use Maven Central for general libraries.
    mavenCentral()
    // Google's Maven repository for Android-related dependencies.
    google()
    // Gradle Plugin Portal for accessing additional Gradle plugins.
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    // Import dependencies from the version catalog "libs" defined in settings.
    // These dependencies include the Kotlin, Android, Hilt, Kover, and SonarQube Gradle plugins.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.hilt.android.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.sonarqube.gradle.plugin)
}
