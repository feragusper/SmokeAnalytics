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
        commonMain {
            dependencies {
                implementation(project(":libraries:design:common"))
            }
        }
        jsMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(compose.html.svg)
            }
        }
    }
}