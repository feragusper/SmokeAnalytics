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
    namespace = "com.feragusper.smokeanalytics.features.chatbot.data"

    defaultConfig {
        // Add a BuildConfig field for Google Auth Server Client ID, loaded from local properties.
        buildConfigField(
            "String",
            "GOOGLE_AI_CLIENT_GENERATIVEAI_API_KEY",
            gradleLocalProperties(rootDir, providers)
                .getProperty("google.ai.client.generativeai.api.key")
                .asBuildConfigString(),
        )
    }
}

dependencies {
    implementation(project(":features:chatbot:domain"))
    implementation(project(":libraries:smokes:domain"))

    implementation(libs.generativeai)

    implementation(libs.kotlinx.datetime)

    // Dagger Hilt dependencies for dependency injection.
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // Compose runtime for state management and composition.
    implementation(libs.androidx.compose.runtime)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
