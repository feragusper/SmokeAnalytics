plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    maven("https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.hilt.android.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.sonarqube.gradle.plugin)
}