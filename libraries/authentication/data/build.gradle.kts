@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.authentication.data"
}

dependencies {
    implementation(project(":libraries:authentication:domain"))
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.playservices.auth)

    implementation(libs.androidx.compose.runtime)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
