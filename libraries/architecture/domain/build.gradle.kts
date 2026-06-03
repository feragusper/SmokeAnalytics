import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    alias(libs.plugins.lighthouse)
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

extensions.configure<KoverProjectExtension>("kover", KoverConfig(layout).configure)

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
        val jsMain by getting
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}