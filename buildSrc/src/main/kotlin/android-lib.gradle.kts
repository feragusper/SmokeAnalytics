plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

android {
    compileSdk = Android.compileSdk

    defaultConfig {
        minSdk = Android.minSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = Java.jvmTarget
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Java.kotlinCompilerExtensionVersion
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            )
        )
    }
}

val koverConfig = KoverConfig(layout)

koverReport(koverConfig.koverReport)

sonarqube(
    SonarConfig(
        koverConfig = koverConfig,
        project = project
    ).sonarExtension
)

koverReport {

    defaults {
        mergeWith("release")
    }

    // configure reports for 'release' build variant
    androidReports("release") {
        // same as for 'defaults' with the exception of 'mergeWith'
    }
}