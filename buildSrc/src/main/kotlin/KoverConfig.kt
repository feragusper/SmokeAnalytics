import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class KoverConfig(
    private val layout: ProjectLayout
) {

    companion object {
        internal const val KOVER_REPORT_DIR = "smoke-analytics-report"
        internal const val KOVER_REPORT_XML_FILE = "result.xml"
        internal val koverReportExclusionsClasses = listOf(
            "**/*Application.*",
            "**/*Activity.*",
            "**/*Navigator.*",
            "**/*NavigationGraph.*",
            "**/*View.*",
            "**/*Color.kt",
            "**/*Typography.kt",
            "**/compose/**",
            "**/di/**",
            "**/extensions/**"
        )
    }

    // Defines the provider for the XML report file location.
    private val koverReportFileXML: Provider<RegularFile> =
        layout.buildDirectory.file("$KOVER_REPORT_DIR/$KOVER_REPORT_XML_FILE")

    // Configure the Kover report settings.
    val koverReport = Action<kotlinx.kover.gradle.plugin.dsl.KoverReportExtension> {
        // Configure common filters for all reports.
        filters {
            excludes {
                // Exclude classes by fully-qualified name (wildcards '*' and '?' are supported).
                classes(koverReportExclusionsClasses)
                // Exclude all classes in the specified package and its subpackages.
                packages("com.another.subpackage")
                // Exclude all classes and functions annotated with matching annotations.
                annotatedBy("*Generated*")
            }
        }

        // Configure default report generation.
        defaults {
            // XML Report configuration.
            xml {
                // Generate an XML report when running the `check` task.
                onCheck = true
                // Set the destination file for the XML report.
                setReportFile(koverReportFileXML)
            }

            // HTML Report configuration.
            html {
                // Generate an HTML report when running the `check` task.
                onCheck = true
                // Set the destination directory for the HTML report.
                setReportDir(layout.buildDirectory.dir("$KOVER_REPORT_DIR/html-result"))
            }

            // Verification configuration to enforce coverage rules.
            verify {
                // Verify coverage during the `check` task.
                onCheck = true
                // Define a verification rule.
                rule {
                    // Enable this verification rule.
                    isEnabled = true

                    // Specify the grouping entity for which coverage is aggregated.
                    entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                    // Define the coverage bounds.
                    bound {
                        // Lower coverage bound.
                        minValue = 0
                        // Upper coverage bound.
                        maxValue = 100
                        // Metric to measure (e.g., lines).
                        metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
                        // Aggregation type to compute the coverage percentage.
                        aggregation =
                            kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                    }

                    // Additional lower bound for percentage of covered lines.
                    minBound(0)
                    // Additional upper bound for percentage of covered lines.
                    maxBound(100)
                }
            }
        }
    }
}
