import com.android.build.gradle.BaseExtension
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties

// Apply the SonarQube plugin globally.
apply(plugin = "org.sonarqube")

// Global SonarQube configuration.
sonar {
    properties {
        // Set SonarQube organization and server details.
        property("sonar.organization", "feragusper")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "SmokeAnalytics")
        property("sonar.projectKey", "feragusper_SmokeAnalytics")
    }
}

// Configure SonarQube settings for each subproject after it is evaluated.
subprojects {
    afterEvaluate {
        sonar {
            properties {
                // Coverage paths apply to all modules.
                filesSafeProperty(
                    "sonar.coverage.jacoco.xmlReportPaths",
                    "${layout.buildDirectory.get()}/${KoverConfig.KOVER_REPORT_DIR}/${KoverConfig.KOVER_REPORT_XML_FILE}",
                    "${layout.buildDirectory.get()}/reports/kover/report.xml",
                )

                // Exclude specific classes from coverage reports.
                val exclusions = KoverConfig.koverReportExclusionsClasses.toMutableList()

                if (hasAndroidExtension()) {
                    // Plugin 7.3.0 double-indexes AndroidManifest.xml — exclude it.
                    exclusions.add("**/AndroidManifest.xml")

                    // Let plugin auto-detect sources/binaries; only set test results.
                    property(
                        "sonar.junit.reportPaths",
                        "${layout.buildDirectory.get()}/test-results/testDebugUnitTest"
                    )
                } else {
                    // Non-Android modules: configure sources, tests, and settings manually.
                    filesSafeProperty(
                        "sonar.sources",
                        "$projectDir/src/main",
                        "$projectDir/src/commonMain",
                        "$projectDir/src/jvmMain",
                        "$projectDir/src/jsMain",
                        "$projectDir/src/webMain",
                        "$projectDir/src/wasmJsMain",
                    )
                    filesSafeProperty(
                        "sonar.tests",
                        "$projectDir/src/test/java",
                        "$projectDir/src/test/kotlin",
                        "$projectDir/src/commonTest",
                        "$projectDir/src/jvmTest",
                        "$projectDir/src/jsTest",
                        "$projectDir/src/webTest",
                        "$projectDir/src/wasmJsTest",
                    )
                    property("sonar.java.coveragePlugin", "jacoco")
                    property("sonar.import_unknown_files", true)

                    if (plugins.hasPlugin(JavaPlugin::class.java)) {
                        property(
                            "sonar.junit.reportPaths",
                            "${layout.buildDirectory.get()}/test-results/test"
                        )
                    }
                }

                property("sonar.exclusions", exclusions.joinToString(","))
                property("sonar.coverage.exclusions", exclusions.joinToString(","))
            }
        }
    }
}

// Extension function to simplify configuring SonarQube in projects.
fun Project.sonar(configuration: SonarExtension.() -> Unit) {
    extensions.getByType(SonarExtension::class.java).apply(configuration)
}

// Extension function that sets a Sonar property only if the specified files exist.
fun SonarProperties.filesSafeProperty(name: String, vararg files: String) {
    files.filter { File(it).exists() }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(",")
        ?.let { property(name, it) }
}

// Helper function to determine if the Android extension is applied to the project.
fun Project.hasAndroidExtension() = extensions.findByType(BaseExtension::class.java) != null
