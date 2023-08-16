@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-library`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.features.home.presentation"
}

dependencies {
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":libraries:design"))

    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.hilt)
    implementation(libs.bundles.androidx.navigation)

    kapt(libs.hilt.compiler)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutinesTest)

    debugImplementation(libs.bundles.compose.debug)

    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.bundles.android.test)
}