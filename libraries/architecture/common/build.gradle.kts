plugins {
    // Use the predefined android-lib plugin from the project build script
    `android-lib`
    // Enable Kotlin annotation processing
    id("kotlin-kapt")
    // Apply Dagger Hilt plugin for dependency injection
    id("dagger.hilt.android.plugin")
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
    // Include Dagger Hilt for dependency injection
    implementation(libs.hilt)
    // Use Hilt's compiler for annotation processing
    kapt(libs.hilt.compiler)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}