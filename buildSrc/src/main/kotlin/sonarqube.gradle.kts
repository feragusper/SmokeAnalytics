import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties

apply(plugin = "org.sonarqube")

sonar {
    properties {
        property("sonar.organization", "feragusper")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "SmokeAnalytics")
        property("sonar.projectKey", "feragusper_SmokeAnalytics")
    }
}

subprojects {
    afterEvaluate {
        sonar {
            properties {
                filesSafeProperty(
                    "sonar.sources",
                    "$projectDir/src/main",
                    "$projectDir/build.gradle",
                )
                filesSafeProperty(
                    "sonar.tests",
                    "$projectDir/src/test/java",
                    "$projectDir/src/test/kotlin",
                )
                property("sonar.exclusions", "build/**,**/assets/**,**/test/**,**/androidTest/**")
                property("sonar.java.coveragePlugin", "jacoco")
                property("sonar.import_unknown_files", true)

                if (hasAndroidExtension()) {
                    val kotlinClasses = "$buildDir/tmp/kotlin-classes"
                    val javaClasses = "$buildDir/intermediates/javac"

                    filesSafeProperty(
                        "sonar.java.binaries",
                        "$kotlinClasses/debug",
                        "$javaClasses/debug",
                    )
                    filesSafeProperty(
                        "sonar.java.test.binaries",
                        "$kotlinClasses/debugUnitTest",
                        "$javaClasses/debugUnitTest",
                    )
                    property("sonar.junit.reportPaths", "$buildDir/test-results/testDebugUnitTest")
                    property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/smoke-analytics-report/result.xml")
                } else if (plugins.hasPlugin(JavaPlugin::class.java)) {
                    property("sonar.junit.reportPaths", "$buildDir/test-results/test")
                    property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/smoke-analytics-report/result.xml")
                }
            }
        }
    }
}

fun Project.sonar(configuration: SonarExtension.() -> Unit) {
    extensions.getByType(SonarExtension::class.java).apply(configuration)
}

fun SonarProperties.filesSafeProperty(name: String, vararg files: String) {
    property(name, files.filter { File(it).exists() }.joinToString(","))
}

fun Project.hasAndroidExtension() = extensions.findByType(BaseExtension::class.java) != null