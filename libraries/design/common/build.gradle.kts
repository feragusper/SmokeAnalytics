plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = "com.feragusper.smokeanalytics.libraries.design.common"
        compileSdk = Android.COMPILE_SDK
        minSdk = Android.MIN_SDK
        withHostTestBuilder {}
        withDeviceTestBuilder {}
    }

    js(IR) {
        browser()
    }
}
