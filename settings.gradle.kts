pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

// Set the root project name
rootProject.name = "SmokeAnalytics"

// Include application modules
include(":apps:mobile")
include(":apps:wear")
include(":apps:web")

// Include feature modules
include(":features:authentication:presentation:mobile")
include(":features:authentication:presentation:web")
include(":features:devtools:presentation")
include(":features:history:presentation:mobile")
include(":features:history:presentation:web")
include(":features:home:domain")
include(":features:home:presentation:mobile")
include(":features:home:presentation:web")
include(":features:goals:domain")
include(":features:settings:presentation:mobile")
include(":features:settings:presentation:web")
include(":features:stats:presentation:mobile")
include(":features:stats:presentation:web")

// Include library modules
include(":libraries:architecture:common")
include(":libraries:architecture:domain")
include(":libraries:architecture:presentation:mobile")
include(":libraries:architecture:presentation:web")
include(":libraries:authentication:data:mobile")
include(":libraries:authentication:data:web")
include(":libraries:authentication:domain")
include(":libraries:authentication:presentation:mobile")
include(":libraries:authentication:presentation:web")
include(":libraries:design:common")
include(":libraries:design:mobile")
include(":libraries:design:web")
include(":libraries:logging")
include(":libraries:preferences:data:mobile")
include(":libraries:preferences:data:web")
include(":libraries:preferences:domain")
include(":libraries:smokes:data:mobile")
include(":libraries:smokes:data:web")
include(":libraries:smokes:domain")
include(":libraries:smokes:presentation")
include(":libraries:wear:data")
include(":libraries:wear:domain")
