plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.preferences.data"
}

dependencies {
    implementation(project(":libraries:preferences:domain"))
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
