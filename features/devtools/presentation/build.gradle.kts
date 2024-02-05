@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.features.devtools.presentation"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":libraries:design"))
    implementation(project(":libraries:authentication:presentation"))
    implementation(project(":libraries:authentication:domain"))
    implementation(project(":libraries:authentication:data"))

    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.hilt)
    implementation(libs.bundles.androidx.navigation)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.playservices.auth)

    kapt(libs.hilt.compiler)

    testImplementation(libs.bundles.test)

    debugImplementation(libs.bundles.compose.debug)

    androidTestImplementation(libs.bundles.compose.test)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.bundles.android.test)
}