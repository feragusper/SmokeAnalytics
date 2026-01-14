plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) { browser() }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                implementation(compose.runtime)
                implementation(compose.html.core)

                implementation(project(":libraries:architecture:domain"))
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:design:web"))
                implementation(project(":libraries:smokes:domain"))
            }
        }
    }
}