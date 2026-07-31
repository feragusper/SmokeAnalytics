import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

kotlin {
    // Umbrella framework consumed by the SwiftUI app. Bundles the shared domain use cases and
    // the GitLive-Firebase data implementations, and exposes a Koin entry point for Swift.
    val xcf = XCFramework("Shared")

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            // Static keeps the SwiftUI side free of an embedded dynamic framework to sign.
            isStatic = true
            xcf.add(this)

            // Export the domain modules so their use cases and models are visible from Swift.
            export(project(":libraries:architecture:domain"))
            export(project(":libraries:smokes:domain"))
            export(project(":libraries:cravings:domain"))
            export(project(":libraries:preferences:domain"))
            export(project(":libraries:authentication:domain"))
            export(project(":features:home:domain"))
            export(project(":features:goals:domain"))
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // api + export above => Swift sees these types.
                api(project(":libraries:architecture:domain"))
                api(project(":libraries:smokes:domain"))
                api(project(":libraries:cravings:domain"))
                api(project(":libraries:preferences:domain"))
                api(project(":libraries:authentication:domain"))
                api(project(":features:home:domain"))
                api(project(":features:goals:domain"))

                // GitLive-Firebase repository implementations (shared with the web target).
                implementation(project(":libraries:smokes:data:web"))
                implementation(project(":libraries:cravings:data:web"))
                implementation(project(":libraries:preferences:data:web"))
                implementation(project(":libraries:authentication:data:web"))

                api(libs.koin.core)
            }
        }
    }
}
