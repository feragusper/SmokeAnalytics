plugins {
    // Use the custom java-lib plugin for a modular Java/Kotlin library.
    `java-lib`
}

dependencies {
    // Use javax.inject for dependency injection annotations.
    implementation(libs.javax.inject)

    // Unit testing dependencies.
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}

tasks.test {
    // Configure the test task to use JUnit Platform (JUnit 5).
    useJUnitPlatform()
}
