plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    js(IR) {
        browser()
        binaries.library()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.library()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                runtimeOnly(libs.junit.jupiter.engine)

                implementation(libs.kluent)
                implementation(libs.coroutines.test)
                implementation(libs.app.cash.turbine)
            }
        }

        val jsMain by getting
        val jsTest by getting

        val wasmJsMain by getting
        val wasmJsTest by getting
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}