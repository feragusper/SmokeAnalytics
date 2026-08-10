import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

// Configure Kover code coverage reports using the centralized KoverConfig.
extensions.configure<KoverProjectExtension>("kover", KoverConfig(layout).configure)

kotlin {
    jvm()

    js(IR) {
        browser()
    }

    // iOS targets for the SwiftUI app sharing this domain/data code.
    // Default hierarchy template auto-creates the intermediate iosMain/iosTest source sets.
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val jsMain by getting
        val jsTest by getting
    }
}