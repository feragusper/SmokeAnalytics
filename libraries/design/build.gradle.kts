plugins {
    `android-lib`
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.design"
}

dependencies {
    implementation(libs.bundles.androidx.base)
    implementation(libs.material3)
}
