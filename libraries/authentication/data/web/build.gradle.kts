plugins {
    kotlin("multiplatform")
    // Si tu setup ya usa compose multiplatform en web:
    // id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:authentication:domain"))
                implementation(libs.gitlive.firebase.auth) // o la versión que uses en el resto
            }
        }
    }
}