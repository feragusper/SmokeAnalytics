plugins {
    `android-lib` // Custom plugin for Android library (replace with your actual plugin if applicable)
    id("com.android.library") // Android Library plugin
    id("org.jetbrains.kotlin.android") // Kotlin Android plugin
    alias(libs.plugins.compose.compiler) // Compose Compiler plugin for Jetpack Compose
}

android {
    namespace =
        "com.feragusper.smokeanalytics.libraries.chart" // Define the namespace for the library
}

dependencies {
    // Base dependencies for AndroidX libraries, including core, appcompat, etc.
    implementation(libs.bundles.androidx.base)

    // Material3 library for UI components and theming
    implementation(libs.material3)

    // Jetpack Compose dependencies, including Compose UI, Material, and other Compose components
    implementation(libs.bundles.compose)
}
