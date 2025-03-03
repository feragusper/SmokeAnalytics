import com.google.common.base.Charsets
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

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

    signingConfigs {
        // Debug signing configuration using the debug keystore.
        getByName("debug") {
            storeFile = file("$rootDir/debug.keystore")
        }
        // Release signing configuration using properties from a file.
        create("release") {
            val properties = properties("release.keystore.properties")
            storeFile = file("$rootDir/release.keystore")
            storePassword = properties.getProperty("storePassword")
            keyAlias = properties.getProperty("keyAlias")
            keyPassword = properties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            // Disable code minification for release builds.
            isMinifyEnabled = false
            // Configure ProGuard rules.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the release signing configuration.
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            // Use debug signing configuration.
            signingConfig = signingConfigs.getByName("debug")
        }
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

// Utility function to load properties from a file located at the root directory.
fun properties(propertiesFileName: String): Properties {
    val properties = Properties()
    val propertiesFile = File(rootDir, propertiesFileName)
    if (propertiesFile.isFile) {
        InputStreamReader(FileInputStream(propertiesFile), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    }
    return properties
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