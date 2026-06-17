plugins {
    // Use the predefined android-lib plugin from the project build script
    `android-lib`
}

android {
    // Set the namespace for the Android library
    namespace = "com.feragusper.smokeanalytics.libraries.architecture.common"

    buildFeatures {
        // Enable generation of BuildConfig class.
        buildConfig = true
    }
}

dependencies {
    // Koin for dependency injection (pure Kotlin, no annotation processing).
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
