plugins {
    `java-lib`
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":libraries:architecture:domain"))
    implementation(project(":libraries:smokes:domain"))
    implementation(libs.javax.inject)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}
