plugins {
    kotlin("multiplatform")
}

kotlin {
    // Android / JVM
    jvm()

    // Web (Compose Web / JS IR)
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kermit)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}