plugins {
    `java-lib`
    id("kotlin-kapt")
}

dependencies {
    implementation(libs.javax.inject)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}
