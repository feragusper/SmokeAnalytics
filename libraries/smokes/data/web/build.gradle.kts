plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    js(IR) {
        browser()
        binaries.library()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":libraries:smokes:domain"))
                implementation(project(":libraries:architecture:domain"))
                implementation("dev.gitlive:firebase-auth:2.1.0")
                implementation("dev.gitlive:firebase-firestore:2.1.0")
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}