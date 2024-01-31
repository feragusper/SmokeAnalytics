@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.features.stats.presentation"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":libraries:design"))

    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.hilt)
    implementation(libs.bundles.androidx.navigation)

    kapt(libs.hilt.compiler)
}