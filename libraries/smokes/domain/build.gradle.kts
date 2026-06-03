@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kmp-lib")
}

kotlin {

    jvm()
    js(IR) { browser() }
    wasmJs { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:architecture:domain"))
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":libraries:wear:domain"))
                implementation(libs.javax.inject) // solo si todavía usás @Inject en código jvm específico
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}