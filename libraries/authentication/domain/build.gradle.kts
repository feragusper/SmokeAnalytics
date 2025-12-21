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
                // No platform deps here
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            // No javax.inject needed anymore in domain
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.test)
            }
        }

        val jsMain by getting
        val jsTest by getting

        val wasmJsMain by getting
        val wasmJsTest by getting
    }
}

dependencies {
    add("jvmTestImplementation", platform(libs.junit.bom))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}