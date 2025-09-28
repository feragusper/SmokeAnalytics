import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

class KoverConfig(private val layout: ProjectLayout) {

    companion object {
        internal const val KOVER_REPORT_DIR = "smoke-analytics-report"
        internal const val KOVER_REPORT_XML_FILE = "result.xml"
        internal val koverReportExclusionsClasses = listOf(
            "**/*Application.*","**/*Activity.*","**/*Navigator.*","**/*NavigationGraph.*",
            "**/*View.*","**/*Color.kt","**/*Typography.kt","**/compose/**","**/di/**","**/extensions/**",
        )
    }

    private val xmlFile: Provider<RegularFile> =
        layout.buildDirectory.file("$KOVER_REPORT_DIR/$KOVER_REPORT_XML_FILE")

    val configure: Action<KoverProjectExtension> = Action {
        reports {
            filters {
                excludes {
                    classes(koverReportExclusionsClasses)
                    annotatedBy("*Generated*")
                }
            }
            total {
                xml {
                    onCheck.set(true)
                    xmlFile.set(this@KoverConfig.xmlFile)
                }
                html {
                    onCheck.set(true)
                    htmlDir.set(layout.buildDirectory.dir("$KOVER_REPORT_DIR/html-result"))
                }
                verify {
                    onCheck.set(true)
                    rule {
                        // Si tu versión expone 'groupBy' como Property:
                        runCatching { groupBy.set(GroupingEntityType.APPLICATION) }
                            .onFailure { /* fallback for older API: */
                                @Suppress("UNUSED_EXPRESSION") (GroupingEntityType.APPLICATION)
                            }
                        minBound(0, CoverageUnit.LINE, AggregationType.COVERED_PERCENTAGE)
                        maxBound(100, CoverageUnit.LINE, AggregationType.COVERED_PERCENTAGE)
                    }
                }
            }
        }
    }
}