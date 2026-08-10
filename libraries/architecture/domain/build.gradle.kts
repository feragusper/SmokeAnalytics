import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

extensions.configure<KoverProjectExtension>("kover", KoverConfig(layout).configure)

kotlin {
    // Pin to 17 like the kmp-lib convention: without this the jvm target compiles with the
    // Gradle daemon's JDK, and a newer daemon emits bytecode the Java-17 consumers can't load.
    jvmToolchain(17)

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

    iosArm64()
    iosSimulatorArm64()

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