plugins {
    // Apply the Java Library plugin for this module.
    `java-lib`
    // Enable Kotlin annotation processing.
    id("kotlin-kapt")
}

dependencies {
    // Architecture domain module for shared business logic.
    implementation(project(":libraries:architecture:domain"))
    // Smokes domain module for smoke-related business logic.
    implementation(project(":libraries:smokes:domain"))
    implementation(project(":libraries:authentication:domain"))

    // Dependency injection using javax.inject annotations.
    implementation(libs.javax.inject)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}

tasks.test {
    // Use JUnit Platform for running tests.
    useJUnitPlatform()
}
