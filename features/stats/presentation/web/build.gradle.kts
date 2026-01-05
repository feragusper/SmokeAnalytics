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
                // --- Architecture / state handling ---
                implementation(project(":libraries:architecture:domain"))

                // --- Domain ---
                implementation(project(":libraries:smokes:domain"))

                // --- Coroutines ---
                implementation(libs.kotlinx.coroutines.core)

                // --- Compose Web ---
                implementation(compose.runtime)
                implementation(compose.html.core)

                implementation(npm("chart.js", "4.4.1"))
            }
        }

        val jsTest by getting
    }
}