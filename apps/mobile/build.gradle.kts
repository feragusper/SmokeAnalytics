import com.google.common.base.Charsets
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

plugins {
    // Apply Android Application plugin.
    id("com.android.application")
    // Enable Kotlin annotation processing.
    id("kotlin-kapt")
    // Apply Kotlin Android plugin.
    kotlin("android")
    // Enable Dagger Hilt for dependency injection.
    id("dagger.hilt.android.plugin")
    // Apply Google Services plugin.
    id("com.google.gms.google-services")
    // Apply Compose Compiler plugin via version catalog alias.
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

// Construct the version name using a major.minor.patch pattern with the git code.
val majorMinorPatchVersionName = "0.6.0.$gitCode"

android {
    // Set the application namespace.
    namespace = "com.feragusper.smokeanalytics"
    // Set the compile SDK version from centralized configuration.
    compileSdk = Android.COMPILE_SDK

    defaultConfig {
        // Unique application ID.
        applicationId = "com.feragusper.smokeanalytics"
        // Set minimum and target SDK versions.
        minSdk = Android.MIN_SDK
        targetSdk = Android.TARGET_SDK
        // Define versionCode and versionName.
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

    // Define flavor dimensions.
    flavorDimensions += "environment"

    // Define product flavors: staging and production.
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
            dimension = "environment"
            resValue("string", "app_name", applicationName)
        }
    }

    compileOptions {
        // Set Java source and target compatibility to Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Enable Jetpack Compose.
        compose = true
        // Enable generation of BuildConfig.
        buildConfig = true
    }

    packaging {
        // Exclude unnecessary META-INF resources.
        resources {
            excludes += "META-INF/**/*"
        }
    }

    lint {
        // Disable specific lint rule.
        disable.add("EnsureInitializerMetadata")
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.RequiresOptIn"
        )
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
    implementation(project(":libraries:architecture:presentation:mobile"))
    implementation(project(":libraries:authentication:domain"))
    implementation(project(":libraries:design:mobile"))
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":features:authentication:presentation:mobile"))
    implementation(project(":features:chatbot:presentation"))
    implementation(project(":features:chatbot:domain"))
    implementation(project(":features:history:presentation:mobile"))
    implementation(project(":features:home:presentation:mobile"))
    implementation(project(":features:home:domain"))
    implementation(project(":features:settings:presentation:mobile"))
    implementation(project(":features:stats:presentation:mobile"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.base)
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.hilt)
    implementation(libs.timber)
    implementation(libs.animated.navigation.bar)

    debugImplementation(project(":features:devtools:presentation"))

    kapt(libs.hilt.compiler)
}

// Task to print the current version name to the console.
tasks.register("printVersionName") {
    doLast { println(majorMinorPatchVersionName) }
}
