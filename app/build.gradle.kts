plugins {
    id("com.android.application")
    id("kotlin-kapt")
    kotlin("android")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics"
    compileSdk = Android.compileSdk

    defaultConfig {
        applicationId = "com.feragusper.smokeanalytics"
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    lint {
        disable.add("EnsureInitializerMetadata")
    }
}

dependencies {
    implementation(libs.bundles.androidx.base)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.material3)
    implementation(libs.bundles.androidx.navigation)
    implementation(libs.hilt)
    implementation(project(":libraries:design"))
    implementation(project(":libraries:architecture:presentation"))
    implementation(project(":features:home:presentation"))
    kapt(libs.hilt.compiler)

    debugImplementation(libs.bundles.compose.debug)

    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.bundles.android.test)
}