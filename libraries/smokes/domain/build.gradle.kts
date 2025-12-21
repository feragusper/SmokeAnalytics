plugins {
    id("kmp-lib")
}

kotlin {

    jvm()
    js(IR) { browser() }
    wasmJs { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libraries:architecture:domain"))
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":libraries:wear:domain"))
                implementation(libs.javax.inject) // solo si todavía usás @Inject en código jvm específico
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.bundles.test)
            }
        }

        // wasm/otros targets los sigue manejando tu plugin `kmp-lib`
    }
}

dependencies {
    add("jvmTestImplementation", platform(libs.junit.bom))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}