/*
 * settings.gradle.kts
 * This file configures plugin management and dependency resolution for the project.
 * It also sets the root project name and includes all the subprojects.
 */

pluginManagement {
    repositories {
        // Google's Maven repository for Android dependencies
        google()
        // Maven Central repository for general Java/Kotlin libraries
        mavenCentral()
        // Gradle Plugin Portal for accessing Gradle plugins
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Ensure that only the repositories defined here are used,
    // and fail if any project defines its own repositories.
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        // Google's Maven repository for Android dependencies
        google()
        // Maven Central repository for general Java/Kotlin libraries
        mavenCentral()
    }
}

// Set the root project name
rootProject.name = "SmokeAnalytics"

// Include application modules
include(":apps:mobile")
include(":apps:wear")

// Include feature modules
include(":features:authentication:presentation")
include(":features:devtools:presentation")
include(":features:history:presentation")
include(":features:home:domain")
include(":features:home:presentation")
include(":features:settings:presentation")
include(":features:stats:presentation")
include(":features:chatbot:presentation")
include(":features:chatbot:domain")
include(":features:chatbot:data")

// Include library modules
include(":libraries:architecture:common")
include(":libraries:architecture:domain")
include(":libraries:architecture:presentation")
include(":libraries:authentication:data")
include(":libraries:authentication:domain")
include(":libraries:authentication:presentation")
include(":libraries:design")
include(":libraries:smokes:data")
include(":libraries:smokes:domain")
include(":libraries:smokes:presentation")
include(":libraries:wear:data")
include(":libraries:wear:domain")