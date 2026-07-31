plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    js(IR) {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kermit)
            }
        }
    }
}