import org.gradle.api.Action
import org.sonarqube.gradle.SonarProperties
import java.lang.System.setProperty

class SonarConfig(
    private val koverConfig: KoverConfig
) {

    val sonarProperties = Action<SonarProperties> {
        setProperty("sonar.sourceEncoding", "UTF-8")
        setProperty("sonar.coverage.jacoco.xmlReportPaths", koverConfig.koverReportFileXML.get().asFile.absolutePath)
    }
}