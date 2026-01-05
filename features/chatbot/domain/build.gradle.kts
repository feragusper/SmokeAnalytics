plugins { id("kmp-lib") }

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:architecture:domain"))
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:smokes:domain"))
            }
        }

        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            binaries.library()
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.javax.inject)
                implementation(project(":libraries:smokes:domain"))
            }
        }

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
    }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }