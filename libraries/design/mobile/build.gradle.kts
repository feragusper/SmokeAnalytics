plugins {
    `android-lib`
    id("com.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.design.mobile"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":libraries:design:common"))

    implementation(libs.bundles.androidx.base)
    implementation(libs.material3)
    implementation(libs.bundles.compose)
}
