plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

kotlin {
    jvm()

    js(IR) {
        browser()
    }

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val jsMain by getting
        val jsTest by getting
    }
}