plugins {
    `android-lib`
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.cravings.data"
}

dependencies {
    implementation(project(":libraries:cravings:domain"))
    implementation(project(":libraries:architecture:domain"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    implementation(libs.timber)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
