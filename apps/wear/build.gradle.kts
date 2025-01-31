plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.wear"
}

dependencies {
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":libraries:wear:data"))
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.hilt)
    implementation(libs.timber)
    implementation(project(":libraries:design"))
    implementation(libs.androidx.tiles)
    implementation(libs.horologist.composables)
    implementation(libs.horologist.tiles)
    implementation(libs.horologist.compose.tools)
    implementation(libs.androidx.protolayout.material)
    implementation(libs.androidx.protolayout.core)
    kapt(libs.hilt.compiler)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.tiles.material)
}