plugins { id("kmp-lib") } // tu convención: jvm + wasmJs + kover/sonar

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:architecture:domain"))
                implementation(project(":libraries:smokes:domain"))
                // ⛔️ No metas javax.inject acá (es JVM-only)
            }
        }

        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser()
            binaries.library()
        }

        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }

        val jvmMain by getting {
            dependencies {
                // Si usás @Inject en constructores del dominio:
                implementation(libs.javax.inject)
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

        // wasmJsMain / wasmJsTest se quedan vacíos por ahora
    }
}

// (Opcional) Si venías usando BOM de JUnit:
dependencies {
    add("jvmTestImplementation", platform(libs.junit.bom))
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }