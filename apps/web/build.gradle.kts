import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
}

private val smokeEnv: String = providers.gradleProperty("smoke.env").orNull ?: "staging"

private data class WebFirebaseConfig(
    val apiKey: String,
    val authDomain: String,
    val projectId: String,
    val storageBucket: String,
    val appId: String,
)

private val stagingConfig = WebFirebaseConfig(
    apiKey = "AIzaSyCQsNHxeSiaXTr5KugYx4AmMxpflL_O9lI",
    authDomain = "smoke-analytics-staging.firebaseapp.com",
    projectId = "smoke-analytics-staging",
    storageBucket = "smoke-analytics-staging.firebasestorage.app",
    appId = "1:1016019974225:web:ed48cf5c4e50e5357ee070",
)

private val prodConfig = WebFirebaseConfig(
    apiKey = "AIzaSyC4P6TscDf8CgRFvup2uouvixEVRklnYkc",
    authDomain = "smoke-analytics.firebaseapp.com",
    projectId = "smoke-analytics",
    storageBucket = "smoke-analytics.firebasestorage.app",
    appId = "1:235081091876:web:1f590358b355fa999141b1",
)

private fun selectedConfig(env: String): WebFirebaseConfig =
    if (env == "prod") prodConfig else stagingConfig

private val cfg = selectedConfig(smokeEnv)

buildkonfig {
    packageName = "com.feragusper.smokeanalytics.apps.web"

    defaultConfigs {
        buildConfigField(STRING, "SMOKE_ENV", smokeEnv)
        buildConfigField(STRING, "FIREBASE_API_KEY", cfg.apiKey)
        buildConfigField(STRING, "FIREBASE_AUTH_DOMAIN", cfg.authDomain)
        buildConfigField(STRING, "FIREBASE_PROJECT_ID", cfg.projectId)
        buildConfigField(STRING, "FIREBASE_STORAGE_BUCKET", cfg.storageBucket)
        buildConfigField(STRING, "FIREBASE_APP_ID", cfg.appId)
    }
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig { outputFileName = "smokeanalytics.js" }
        }
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(libs.kotlinx.coroutines.core)

                implementation(project(":libraries:architecture:domain"))
                implementation(project(":features:home:domain"))
                implementation(project(":features:home:presentation:web"))
                implementation(project(":features:history:presentation:web"))
                implementation(project(":features:authentication:presentation:web"))
                implementation(project(":features:stats:presentation:web"))
                implementation(project(":features:settings:presentation:web"))
                implementation(project(":libraries:smokes:domain"))
                implementation(project(":libraries:smokes:data:web"))
                implementation(project(":libraries:authentication:domain"))
                implementation(project(":libraries:authentication:data:web"))
                implementation(project(":libraries:design:web"))

                implementation(libs.gitlive.firebase.auth)
                implementation(libs.firebase.app)
            }
        }
    }
}

val prepareFirebaseHosting by tasks.registering(Sync::class) {
    dependsOn(tasks.named("jsBrowserProductionWebpack"))

    val webpackOut = layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")
    val resourcesOut = layout.buildDirectory.dir("processedResources/js/main")
    val firebaseOut = layout.buildDirectory.dir("firebaseHosting")

    from(webpackOut)
    from(resourcesOut)
    into(firebaseOut)
}