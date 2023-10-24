buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.hilt.android.gradle.plugin)
        classpath(libs.kover.gradle.plugin)
        classpath(libs.sonarqube.gradle.plugin)
    }
}

plugins {
    id("com.google.gms.google-services") version "4.3.15" apply false
    sonarqube
}