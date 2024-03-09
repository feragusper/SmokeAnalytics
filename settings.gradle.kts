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
include(":features:devtools:presentation")
include(":features:home:domain")
include(":features:home:presentation")
include(":features:settings:presentation")
include(":features:stats:presentation")
include(":libraries:architecture:domain")
include(":libraries:architecture:presentation")
include(":libraries:authentication:data")
include(":libraries:authentication:domain")
include(":libraries:authentication:presentation")
include(":libraries:design")
include(":libraries:smokes:data")
include(":libraries:smokes:domain")

 