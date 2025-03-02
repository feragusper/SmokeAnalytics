// Root build script for the SmokeAnalytics project.

buildscript {
    repositories {
        // Google's Maven repository for Android dependencies.
        google()
        // Maven Central repository for general Java/Kotlin libraries.
        mavenCentral()
        // Gradle Plugin Portal for accessing additional Gradle plugins.
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        // Android Gradle Plugin (version defined in version catalog).
        classpath(libs.android.gradle.plugin)
        // Kotlin Gradle Plugin for Kotlin support.
        classpath(libs.kotlin.gradle.plugin)
        // Hilt Gradle Plugin for dependency injection.
        classpath(libs.hilt.android.gradle.plugin)
        // Kover Gradle Plugin for code coverage reporting.
        classpath(libs.kover.gradle.plugin)
        // SonarQube Gradle Plugin for static code analysis.
        classpath(libs.sonarqube.gradle.plugin)
    }
}

plugins {
    // Google Services plugin is declared here but not applied by default.
    // Apply it in the respective modules as needed.
    id("com.google.gms.google-services") version "4.4.2" apply false

    // Apply the SonarQube plugin globally for static code analysis.
    sonarqube
}