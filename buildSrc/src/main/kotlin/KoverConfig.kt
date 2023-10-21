import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class KoverConfig(
    private val layout: ProjectLayout
) {

    private val koverReportDir = "smoke-analytics-report"
    val koverReportFileXML: Provider<RegularFile> = layout.buildDirectory.file("$koverReportDir/result.xml")

    val koverReport = Action<kotlinx.kover.gradle.plugin.dsl.KoverReportExtension> {
        // common filters for all reports of all variants
        filters {
            // exclusions for reports
            excludes {
                // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                classes(
                    "**.*Application",
                    "**.*Activity",
                )
                // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                packages("com.another.subpackage")
                // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                annotatedBy("*Generated*")
            }
        }

        // configure default reports - for Kotlin/JVM or Kotlin/MPP projects or merged android variants
        defaults {
            // configure XML report
            xml {
                //  generate an XML report when running the `check` task
                onCheck = false

                // XML report file
                setReportFile(koverReportFileXML)
            }

            html {
                //  generate an XML report when running the `check` task
                onCheck = true

                // XML report file
                setReportDir(layout.buildDirectory.dir("$koverReportDir/html-result"))
            }

            // configure verification
            verify {
                //  verify coverage when running the `check` task
                onCheck = true

                // add verification rule
                rule {
                    // check this rule during verification
                    isEnabled = true

                    // specify the code unit for which coverage will be aggregated
                    entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                    // specify verification bound for this rule
                    bound {
                        // lower bound
                        minValue = 0

                        // upper bound
                        maxValue = 100

                        // specify which units to measure coverage for
                        metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE

                        // specify an aggregating function to obtain a single value that will be checked against the lower and upper boundaries
                        aggregation =
                            kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                    }

                    // add lower bound for percentage of covered lines
                    minBound(0)

                    // add upper bound for percentage of covered lines
                    maxBound(100)
                }
            }
        }
    }
}