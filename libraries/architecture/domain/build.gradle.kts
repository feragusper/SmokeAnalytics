// build.gradle.kts for libraries:architecture:domain

plugins {
    // Apply the Java Library plugin for modular Java code.
    `java-lib`
    // Enable Kotlin annotation processing.
    id("kotlin-kapt")
}

dependencies {
    // Use Javax Inject for dependency injection annotations.
    implementation(libs.javax.inject)
    // Use JUnit BOM to manage consistent versions of JUnit dependencies.
    testImplementation(platform(libs.junit.bom))
    // Include additional test dependencies bundled together.
    testImplementation(libs.bundles.test)
}

// Configure the test task to use JUnit Platform (JUnit 5).
tasks.test {
    useJUnitPlatform()
}
