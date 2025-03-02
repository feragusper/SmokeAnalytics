import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("kotlin-kapt")
    kotlin("android")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.compose.compiler)
}

val gitCode: Int by lazy {
    val stdout = ByteArrayOutputStream()
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()

    process.inputStream.use { inputStream ->
        inputStream.copyTo(stdout)
    }

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw IllegalStateException("Git command failed with exit code $exitCode")
    }

    stdout.toString().trim().toInt()
}

val majorMinorPatchVersionName = "0.3.0.$gitCode"

android {
    namespace = "com.feragusper.smokeanalytics"
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        applicationId = "com.feragusper.smokeanalytics"
        minSdk = Android.MIN_SDK
        targetSdk = Android.TARGET_SDK
        versionCode = gitCode
        versionName = majorMinorPatchVersionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = Java.JVM_TARGET
        freeCompilerArgs = listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.RequiresOptIn",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Java.KOTLIN_COMPILER_EXTENSION_VERSION
    }

    flavorDimensions += "environment"

    productFlavors {
        val applicationName = "Smoke Analytics"
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDefault = true
            resValue("string", "app_name", "$applicationName (Staging)")
        }
        create("production") {
            resValue("string", "app_name", applicationName)
            dimension = "environment"
        }
    }
}

dependencies {
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":libraries:architecture:common"))
    implementation(project(":libraries:wear:domain"))
    implementation(project(":libraries:wear:data"))
    implementation(project(":libraries:smokes:data"))
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.hilt)
    implementation(libs.timber)
    implementation(project(":libraries:design"))
    implementation(libs.androidx.tiles)
    implementation(libs.horologist.composables)
    implementation(libs.horologist.tiles)
    implementation(libs.horologist.compose.tools)
    implementation(libs.androidx.protolayout.material)
    implementation(libs.androidx.protolayout.core)
    kapt(libs.hilt.compiler)
    implementation(libs.play.services.wearable)
    implementation(libs.androidx.tiles.material)
}