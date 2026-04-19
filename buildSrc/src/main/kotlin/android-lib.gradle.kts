import com.android.build.api.dsl.LibraryExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Apply the Android Library plugin for building library modules.
    id("com.android.library")
    // Apply the Kover plugin for code coverage reporting.
    id("org.jetbrains.kotlinx.kover")
    // Apply the SonarQube plugin for static code analysis.
    id("org.sonarqube")
}

extensions.configure<LibraryExtension>("android") {
    // Set the compile SDK version using centralized configuration.
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        // Set the minimum SDK version from centralized configuration.
        minSdk = Android.MIN_SDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

// Configure Kover code coverage reports using the centralized KoverConfig.
extensions.configure<KoverProjectExtension>("kover", KoverConfig(layout).configure)

// Additional Kover report configuration.
kover {
    reports {
        total {
            html { onCheck.set(true) }
            xml  { onCheck.set(true) }
        }
    }
}
