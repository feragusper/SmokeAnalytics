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
                implementation(project(":libraries:architecture:domain"))
                implementation(project(":libraries:smokes:domain"))
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:logging"))
                implementation(project(":libraries:design:web"))
                implementation(project(":features:home:domain"))

                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}