import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Apply the Java Library plugin for modular Java projects.
    id("java-library")
    // Apply the Kotlin JVM plugin to enable Kotlin support on the JVM.
    kotlin("jvm")
    // Apply the Kover plugin for code coverage reporting.
    id("org.jetbrains.kotlinx.kover")
    // Apply the SonarQube plugin for static code analysis.
    id("org.sonarqube")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    // Set both source and target compatibility to Java 17.
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Configure Kover code coverage reports using the centralized KoverConfig.
// 'layout' is a Gradle-provided property representing the project layout.
koverReport(KoverConfig(layout).koverReport)
