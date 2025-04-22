import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    // Use the predefined android-lib plugin for Android library modules.
    `android-lib`
    // Enable Kotlin annotation processing.
    id("kotlin-kapt")
    // Apply Dagger Hilt plugin for dependency injection.
    id("dagger.hilt.android.plugin")
    // Apply the Compose Compiler plugin using the version catalog alias.
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library.
    namespace = "com.feragusper.smokeanalytics.libraries.authentication.presentation"

    defaultConfig {
        // Add a BuildConfig field for Google Auth Server Client ID, loaded from local properties.
        buildConfigField(
            "String",
            "GOOGLE_AUTH_SERVER_CLIENT_ID",
            gradleLocalProperties(rootDir, providers)
                .getProperty("google.auth.server.client.id")
                .asBuildConfigString(),
        )
    }

    buildFeatures {
        // Enable Jetpack Compose support.
        compose = true
    }
}

dependencies {
    // Include the design library for consistent theming and UI components.
    implementation(project(":libraries:design"))

    // Use the Compose BOM for consistent Compose library versions.
    implementation(platform(libs.androidx.compose.bom))
    // Include a bundle of Jetpack Compose libraries.
    implementation(libs.bundles.compose)
    // Include Dagger Hilt for dependency injection.
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    // Include Material3 components for modern UI design.
    implementation(libs.material3)

    // Firebase Authentication dependencies.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.identity.googleid)

    // Include Timber for logging.
    implementation(libs.timber)
}
