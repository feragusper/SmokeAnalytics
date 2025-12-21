plugins {
    kotlin("multiplatform")
    // Si tu setup ya usa compose multiplatform en web:
    // id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:authentication:domain"))
                implementation("dev.gitlive:firebase-auth:1.13.0") // o la versión que uses en el resto
            }
        }
    }
}