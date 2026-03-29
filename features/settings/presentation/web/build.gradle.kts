plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":libraries:design:web"))
                implementation(project(":libraries:architecture:domain"))
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:authentication:presentation:web"))
                implementation(project(":libraries:preferences:domain"))
                implementation(project(":libraries:smokes:domain"))
                implementation(project(":features:goals:domain"))

                implementation(compose.runtime)
                implementation(compose.html.core)
            }
        }
    }
}
