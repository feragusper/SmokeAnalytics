plugins {
    // Use the custom java-lib plugin for a modular Java/Kotlin library.
    `java-lib`
    // Enable Kotlin annotation processing.
    id("kotlin-kapt")
}

dependencies {
    // Use javax.inject for dependency injection annotations.
    implementation(libs.javax.inject)
}

tasks.test {
    // Configure the test task to use JUnit Platform (JUnit 5).
    useJUnitPlatform()
}
