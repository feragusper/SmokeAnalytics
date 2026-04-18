plugins {
    `android-lib`
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.wear.data"
}

dependencies {
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:preferences:domain"))
    implementation(project(":libraries:architecture:domain"))
    implementation(project(":libraries:architecture:common"))
    implementation(project(":libraries:wear:domain"))

    implementation(libs.bundles.androidx.base)
    implementation(libs.timber)
    implementation(libs.play.services.wearable)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
