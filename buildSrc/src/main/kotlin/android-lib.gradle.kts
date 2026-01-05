import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Apply the Android Library plugin for building library modules.
    id("com.android.library")
    // Apply the Kotlin Android plugin for Kotlin support in Android.
    kotlin("android")
    // Apply the Kover plugin for code coverage reporting.
    id("org.jetbrains.kotlinx.kover")
    // Apply the SonarQube plugin for static code analysis.
    id("org.sonarqube")
}

android {
    // Set the compile SDK version using centralized configuration.
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        // Set the minimum SDK version from centralized configuration.
        minSdk = Android.MIN_SDK
    }

    buildFeatures {
        // Enable generation of BuildConfig class.
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            // Use JUnit Platform for unit tests.
            it.useJUnitPlatform()
        }
    }

    packaging {
        // Exclude specific resource files from the final AAR package.
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

// Configure Kover code coverage reports using the centralized KoverConfig.
extensions.configure<KoverProjectExtension>("kover", KoverConfig(layout).configure)

// Additional Kover report configuration.
kover {
    reports {
        variant("release") {
            html { onCheck.set(true) }
            xml  { onCheck.set(true) }
        }
    }
}
