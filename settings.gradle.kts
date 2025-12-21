pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
include(":features:settings:presentation:mobile")
include(":features:settings:presentation:web")
include(":features:stats:presentation:mobile")
include(":features:stats:presentation:web")
include(":features:chatbot:presentation")
include(":features:chatbot:domain")
include(":features:chatbot:data")

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
include(":libraries:design")
include(":libraries:logging")
include(":libraries:smokes:data:mobile")
include(":libraries:smokes:data:web")
include(":libraries:smokes:domain")
include(":libraries:smokes:presentation")
include(":libraries:wear:data")
include(":libraries:wear:domain")