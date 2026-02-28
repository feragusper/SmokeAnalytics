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
        val jsMain by getting
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}