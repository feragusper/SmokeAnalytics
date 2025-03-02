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

    compileOptions {
        // Set Java source and target compatibility to Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // Set the JVM target for Kotlin compilation.
        jvmTarget = Java.JVM_TARGET
    }

    buildFeatures {
        // Enable generation of BuildConfig class.
        buildConfig = true
    }

    composeOptions {
        // Specify the Kotlin compiler extension version for Jetpack Compose.
        kotlinCompilerExtensionVersion = Java.KOTLIN_COMPILER_EXTENSION_VERSION
    }

    @Suppress("UnstableApiUsage")
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

// Configure Kover code coverage reports using the centralized KoverConfig.
koverReport(KoverConfig(layout).koverReport)

// Additional Kover report configuration.
koverReport {
    defaults {
        // Merge default reports with those of the 'release' build variant.
        mergeWith("release")
    }

    // Configure reports for the 'release' build variant.
    androidReports("release") {
        // Additional release-specific configuration can be added here.
    }
}
