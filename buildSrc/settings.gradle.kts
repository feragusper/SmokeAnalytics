// buildSrc/settings.gradle.kts
// This file configures the version catalog for dependencies used throughout the project.
// It creates a catalog named "libs" that references the TOML file in the gradle directory of the project root.
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
