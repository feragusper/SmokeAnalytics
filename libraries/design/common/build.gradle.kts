plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // nada android/web acá; solo kotlin común
        }
    }
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.design.common"
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        minSdk = Android.MIN_SDK
    }
}