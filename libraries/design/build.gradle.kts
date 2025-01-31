plugins {
    `android-lib`
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.design"
}

dependencies {
    implementation(libs.bundles.androidx.base)
    implementation(libs.material3)
    implementation(libs.bundles.compose)
}
