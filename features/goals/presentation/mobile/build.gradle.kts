plugins {
    `android-lib`
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.feragusper.smokeanalytics.features.goals.presentation"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":features:goals:domain"))
    implementation(project(":libraries:architecture:presentation:mobile"))
    implementation(project(":libraries:architecture:domain"))
    implementation(project(":libraries:authentication:domain"))
    implementation(project(":libraries:authentication:presentation:mobile"))
    implementation(project(":libraries:design:mobile"))
    implementation(project(":libraries:preferences:domain"))
    implementation(project(":libraries:smokes:domain"))

    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    debugImplementation(libs.bundles.compose.debug)
}
