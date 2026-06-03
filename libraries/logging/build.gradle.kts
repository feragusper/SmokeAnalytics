plugins {
    alias(libs.plugins.lighthouse)
    kotlin("multiplatform")
}

kotlin {
    jvm()

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kermit)
            }
        }
    }
}