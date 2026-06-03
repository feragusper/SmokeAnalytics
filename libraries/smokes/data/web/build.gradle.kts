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
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.firestore)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}