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
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:authentication:data:web"))
                implementation(project(":libraries:authentication:presentation:web"))

                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.firebase.app)
            }
        }
    }
}