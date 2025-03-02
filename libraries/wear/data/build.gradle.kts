plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.wear.data"
}

dependencies {
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:architecture:domain"))
    implementation(project(":libraries:architecture:common"))
    implementation(project(":libraries:wear:domain"))
    implementation(libs.bundles.androidx.base)
    implementation(libs.timber)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
}