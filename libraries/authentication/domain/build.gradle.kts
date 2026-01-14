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
        val commonMain by getting

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }

    }
}

dependencies {
    add("jvmTestImplementation", platform(libs.junit.bom))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}