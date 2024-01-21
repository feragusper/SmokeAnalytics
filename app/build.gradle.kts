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
    id("com.google.gms.google-services")
}

val gitCode: Int by lazy {
    val stdout = ByteArrayOutputStream()
    rootProject.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = stdout
    }
    stdout.toString().trim().toInt()
}

val majorMinorPatchVersionName = "0.1.0.$gitCode"

android {
    namespace = "com.feragusper.smokeanalytics"
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "com.feragusper.smokeanalytics"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = gitCode
        versionName = majorMinorPatchVersionName
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("$rootDir/debug.keystore")
        }
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = Java.jvmTarget
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
        kotlinCompilerExtensionVersion = Java.kotlinCompilerExtensionVersion
    }

    packaging {
        resources {
            excludes += "META-INF/**/*"
        }
    }

    lint {
        disable.add("EnsureInitializerMetadata")
    }
}

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
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.hilt)
    implementation(libs.timber)
    implementation(project(":libraries:design"))
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":features:home:presentation"))
    implementation(project(":features:settings:presentation"))
    kapt(libs.hilt.compiler)

}

task("printVersionName") {
    doLast {
        println(majorMinorPatchVersionName)
    }
}
