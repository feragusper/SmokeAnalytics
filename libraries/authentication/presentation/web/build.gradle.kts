plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                // ─────────── Domain ───────────
                implementation(project(":libraries:authentication:domain"))

                // ─────────── Coroutines ───────────
                implementation(libs.kotlinx.coroutines.core)

                // ─────────── Compose Web (DOM) ───────────
                implementation(compose.runtime)
                implementation(compose.html.core)

                // ─────────── Firebase (GitLive, JS) ───────────
                implementation("dev.gitlive:firebase-auth:1.13.0")
                implementation("dev.gitlive:firebase-app:1.13.0")
            }
        }

        val jsTest by getting
    }
}