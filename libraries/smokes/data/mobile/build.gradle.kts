plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.smokes.data"
}

dependencies {
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:architecture:domain"))
    implementation(project(":libraries:wear:data"))
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.firebase.auth)
    implementation(libs.androidx.compose.runtime)

    implementation(libs.play.services.wearable)

    implementation(libs.timber)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
