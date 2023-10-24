plugins {
    id("java-library")
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("org.sonarqube")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val koverConfig = KoverConfig(layout)

koverReport(koverConfig.koverReport)

sonarqube(
    SonarConfig(
        koverConfig = koverConfig,
        project = project
    ).sonarExtension
)

project.tasks["sonarqube"].dependsOn "koverReport"