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
            // Android components & generated Hilt wiring
            "*.Application", "*.Application\$*", "*.*Activity", "*.*Activity\$*", "*.Hilt_*",
            // Navigation graphs
            "*.*Navigator", "*.*Navigator\$*", "*.*NavigationGraph*",
            // Compose UI code (not unit-testable without instrumentation)
            "*.*View", "*.*ViewKt", "*.*ViewKt\$*",
            "*.*ViewState*Kt", "*.*ViewState*Kt\$*",
            "*.ComposableSingletons\$*",
            "*.*ScreenKt", "*.*ScreenKt\$*", "*.*Screen\$*",
            // i18n descriptor→string resolvers (Compose stringResource glue, not unit-testable)
            "*.GoalProgressTextMobileKt", "*.GoalProgressTextMobileKt\$*",
            // Design tokens (Color, Typography, Theme)
            "*.ColorKt", "*.ColorKt\$*", "*.TypographyKt", "*.TypographyKt\$*",
            "*.ThemeKt", "*.ThemeKt\$*", "*.PaletteTokens", "*.PaletteTokens\$*",
            // Compose shared presentation components
            "*.DatePickerDialog*", "*.EmptySmokes*", "*.SwipeToDismissRow*",
            "*.StatKt", "*.StatKt\$*",
            // Google Sign-In Compose component
            "*.GoogleSignInComponent*",
            // About screen (pure Compose UI)
            "*.*AboutView*",
            // Standard exclusion directories
            "*.compose.*", "*.di.*", "*.extensions.*",
            // BuildConfig generated
            "*.BuildConfig",
            // WearSync platform-specific
            "*.WearSyncManagerImpl*",
            // Firebase Analytics platform wrapper (thin adapter over the Firebase SDK)
            "*.FirebaseAnalyticsTracker", "*.FirebaseAnalyticsTracker\$*",
            // Abstract MVI framework (tested transitively through concrete ViewModels)
            "*.MVIViewModel", "*.MVIViewModel\$*",
            // Hilt generated code
            "hilt_aggregated_deps.*", "*_HiltModules*", "*_Factory*", "*_MembersInjector*",
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
                        // Per-module floor: no individual module may drop below 65%.
                        // The project-wide aggregate target is 80% (enforced by Sonar).
                        minBound(65, CoverageUnit.LINE, AggregationType.COVERED_PERCENTAGE)
                        maxBound(100, CoverageUnit.LINE, AggregationType.COVERED_PERCENTAGE)
                    }
                }
            }
        }
    }
}