@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.features.home.data"
}

dependencies {
    implementation(project(":features:home:domain"))
    implementation(project(":libraries:architecture:domain"))
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.firebase.auth)
    implementation(libs.androidx.compose.runtime)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
