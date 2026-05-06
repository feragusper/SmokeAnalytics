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
            if (hasUnsupportedAgp9AndroidPlugin()) {
                setSkipProject(true)
                return@sonar
            }

            properties {
                // Safely set source directories if they exist.
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
                filesSafeProperty(
                    "sonar.coverage.jacoco.xmlReportPaths",
                    "${layout.buildDirectory.get()}/${KoverConfig.KOVER_REPORT_DIR}/${KoverConfig.KOVER_REPORT_XML_FILE}",
                    "${layout.buildDirectory.get()}/reports/kover/report.xml",
                )

                // Exclude specific classes from coverage reports using KoverConfig settings.
                property(
                    "sonar.exclusions",
                    KoverConfig.koverReportExclusionsClasses.joinToString(separator = ",")
                )
                property(
                    "sonar.coverage.exclusions",
                    KoverConfig.koverReportExclusionsClasses.joinToString(separator = ",")
                )

                // Specify the coverage plugin.
                property("sonar.java.coveragePlugin", "jacoco")
                // Import files that SonarQube might not recognize by default.
                property("sonar.import_unknown_files", true)

                // Configure settings specific to Android projects.
                if (hasAndroidExtension()) {
                    val kotlinClasses = "${layout.buildDirectory.get()}/tmp/kotlin-classes"
                    val javaClasses = "${layout.buildDirectory.get()}/intermediates/javac"

                    filesSafeProperty(
                        "sonar.java.binaries",
                        "$kotlinClasses/debug",
                        "$javaClasses/debug"
                    )
                    filesSafeProperty(
                        "sonar.java.test.binaries",
                        "$kotlinClasses/debugUnitTest",
                        "$javaClasses/debugUnitTest"
                    )
                    property(
                        "sonar.junit.reportPaths",
                        "${layout.buildDirectory.get()}/test-results/testDebugUnitTest"
                    )
                    property(
                        "sonar.coverage.jacoco.xmlReportPaths",
                        "${layout.buildDirectory.get()}/${KoverConfig.KOVER_REPORT_DIR}/${KoverConfig.KOVER_REPORT_XML_FILE}"
                    )
                } else if (plugins.hasPlugin(JavaPlugin::class.java)) {
                    // Configure settings for non-Android (pure Java) projects.
                    property(
                        "sonar.junit.reportPaths",
                        "${layout.buildDirectory.get()}/test-results/test"
                    )
                    property(
                        "sonar.coverage.jacoco.xmlReportPaths",
                        "${layout.buildDirectory.get()}/${KoverConfig.KOVER_REPORT_DIR}/${KoverConfig.KOVER_REPORT_XML_FILE}"
                    )
                }
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

fun Project.hasUnsupportedAgp9AndroidPlugin() = listOf(
    "com.android.application",
    "com.android.library",
    "com.android.test",
    "com.android.dynamic-feature",
    "com.android.kotlin.multiplatform.library",
).any(plugins::hasPlugin)
