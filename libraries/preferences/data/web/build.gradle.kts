plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    js(IR) {
        browser()
        binaries.library()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // Shared GitLive-Firebase implementation, consumed by both the web (js) and iOS targets.
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:preferences:domain"))
                implementation(project(":libraries:architecture:domain"))
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
