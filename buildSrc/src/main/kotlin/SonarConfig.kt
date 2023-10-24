import gradle.kotlin.dsl.accessors._f648986658d1b3d71eaad053e4d5b59c.android
import org.gradle.api.Action
import org.gradle.api.Project
import org.sonarqube.gradle.SonarExtension
import org.sonarqube.gradle.SonarProperties
import java.io.File
import java.lang.System.setProperty

class SonarConfig(
    private val koverConfig: KoverConfig,
    private val project: Project,
) {
    val sonarExtension = Action<SonarExtension> {

        properties {
            property("sonar.sourceEncoding", "UTF-8")

            property("sonar.organization", "feragusper")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.projectName", "SmokeAnalytics")
            property("sonar.projectKey", "feragusper_SmokeAnalytics")

            filesProperty("sonar.sources", "${project.projectDir}/src/main/java")
            filesProperty("sonar.tests", "${project.projectDir}/src/test/java")

            property("sonar.exclusions", "build/**,**/assets/**,**/test/**,**/androidTest/**")

            property("sonar.java.coveragePlugin", "jacoco")

            property("sonar.import_unknown_files", true)

            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                koverConfig.koverReportFileXML.get().asFile.absolutePath
            )

            property("sonar.binaries", "build/intermediates/classes/debug")
            property("sonar.java.binaries", "build/intermediates/classes/debug")
            property("sonar.java.test.binaries", "build/intermediates/classes/debug")
        }

    }

    private fun SonarProperties.filesProperty(name: String, vararg files: String) {
        property(name, files.filter { File(it).exists() }.joinToString(","))
    }

}