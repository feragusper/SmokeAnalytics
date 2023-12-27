import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    `android-lib`
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.feragusper.smokeanalytics.libraries.authentication.presentation"

    defaultConfig {
        buildConfigField(
            "String",
            "GOOGLE_AUTH_SERVER_CLIENT_ID",
            gradleLocalProperties(rootDir).getProperty("google.auth.server.client.id"),
        )
    }
}

dependencies {
    implementation(project(":libraries:design"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    implementation(libs.material3)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.playservices.auth)
}
