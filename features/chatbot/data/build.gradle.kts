import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    // Use the predefined android-lib plugin for Android library modules.
    `android-lib`
    // Enable Kotlin annotation processing.
    id("com.google.devtools.ksp")
    // Apply Dagger Hilt plugin for dependency injection.
    id("com.google.dagger.hilt.android")
    // Apply the Compose Compiler plugin using the version catalog alias.
    alias(libs.plugins.compose.compiler)
}

android {
    // Set the namespace for the Android library.
    namespace = "com.feragusper.smokeanalytics.features.chatbot.data"

    val localProperties = gradleLocalProperties(rootDir, providers)
    val requestedTasks = gradle.startParameter.taskNames.joinToString(" ").lowercase()
    val coachApiKey = when {
        "production" in requestedTasks -> {
            localProperties.getProperty("google.ai.client.generativeai.api.key.production")
                ?: localProperties.getProperty("google.ai.client.generativeai.api.key")
        }

        else -> {
            localProperties.getProperty("google.ai.client.generativeai.api.key.staging")
                ?: localProperties.getProperty("google.ai.client.generativeai.api.key")
        }
    }.orEmpty()

    defaultConfig {
        // Add a BuildConfig field for Google Auth Server Client ID, loaded from local properties.
        buildConfigField(
            "String",
            "GOOGLE_AI_CLIENT_GENERATIVEAI_API_KEY",
            coachApiKey.asBuildConfigString(),
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
    ksp(libs.hilt.compiler)

    // Compose runtime for state management and composition.
    implementation(libs.androidx.compose.runtime)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
