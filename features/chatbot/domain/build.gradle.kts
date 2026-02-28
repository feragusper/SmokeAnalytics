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

    }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }