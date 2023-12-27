pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmokeAnalytics"
include(":app")
include(":libraries:design")
include(":libraries:architecture:presentation")
include(":libraries:architecture:domain")
include(":libraries:authentication:presentation")
include(":libraries:authentication:domain")
include(":libraries:authentication:data")
include(":features:settings:presentation")
include(":features:home:presentation")
include(":features:home:data")
include(":features:home:domain")

 