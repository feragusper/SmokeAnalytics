package com.feragusper.smokeanalytics.libraries.design

import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.StyleSheet
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontFamily
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.media
import org.jetbrains.compose.web.css.mediaMaxWidth
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.style
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width

object SmokeWebStyles : StyleSheet() {

    val appRoot by style {
        fontFamily(
            "system-ui",
            "-apple-system",
            "\"Segoe UI\"",
            "Roboto",
            "Helvetica",
            "Arial",
            "sans-serif"
        )
        property("text-rendering", "optimizeLegibility")
        property("--sa-color-primary", "#006A6A")
        property("--sa-color-onPrimary", "#FFFFFF")
        property("--sa-color-primary-soft", "rgba(0,106,106,0.10)")
        property("--sa-color-secondary", "#4A6363")
        property("--sa-color-bg", "#FFFFFF")
        property("--sa-color-bg-accent", "#F4F8F8")
        property("--sa-color-onBg", "#000000")
        property("--sa-color-surface", "#DDE4E3")
        property("--sa-color-surface-strong", "#F5F8F8")
        property("--sa-color-onSurface", "#161D1D")
        property("--sa-color-outline", "rgba(22,29,29,0.10)")
        property("--sa-color-outline-strong", "rgba(22,29,29,0.18)")
        property("--sa-color-danger", "#BA1A1A")
        property("--sa-color-danger-soft", "rgba(186,26,26,0.10)")
        property("--sa-color-success-soft", "rgba(0,106,106,0.10)")
        property("--sa-color-success", "#006A6A")
        property("--sa-radius-lg", "20px")
        property("--sa-radius-md", "16px")
        property("--sa-radius-sm", "12px")
        property("--sa-shadow-1", "0 8px 24px rgba(0,0,0,0.06)")
        property("--sa-shadow-2", "0 12px 28px rgba(0,0,0,0.10)")
        property("--sa-transition-fast", "160ms ease")
        property("--sa-transition-page", "220ms cubic-bezier(0.22, 1, 0.36, 1)")
        backgroundColor(Color("var(--sa-color-bg)"))
        color(Color("var(--sa-color-onBg)"))
        padding(0.px)
        property("width", "100%")
        minHeight(100.vh)
    }

    val appRootDarkTokens by style {
        property("--sa-color-primary", "#80D5D4")
        property("--sa-color-onPrimary", "#003737")
        property("--sa-color-primary-soft", "rgba(128,213,212,0.14)")
        property("--sa-color-secondary", "#B0CCCB")
        property("--sa-color-bg", "#000000")
        property("--sa-color-bg-accent", "#051F1F")
        property("--sa-color-onBg", "#FFFFFF")
        property("--sa-color-surface", "#0E1514")
        property("--sa-color-surface-strong", "#051F1F")
        property("--sa-color-onSurface", "#B0CCCB")
        property("--sa-color-outline", "rgba(255,255,255,0.10)")
        property("--sa-color-outline-strong", "rgba(255,255,255,0.16)")
        property("--sa-color-danger", "#FFB4AB")
        property("--sa-color-danger-soft", "rgba(255,180,171,0.12)")
        property("--sa-color-success-soft", "rgba(128,213,212,0.12)")
        property("--sa-color-success", "#80D5D4")
    }

    val appRootReducedMotion by style {
        property("--sa-transition-fast", "1ms linear")
        property("--sa-transition-page", "1ms linear")
    }

    val shell by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        property("width", "100%")
        minHeight(100.vh)
        property("box-sizing", "border-box")
        padding(20.px)
        gap(20.px)

        media(mediaMaxWidth(900.px)) {
            self {
                flexDirection(FlexDirection.Column)
                padding(14.px)
                gap(14.px)
            }
        }
    }

    val sidebar by style {
        property("width", "280px")
        property("flex", "0 0 280px")
        property("box-sizing", "border-box")
        padding(20.px)
        property("border-radius", "var(--sa-radius-lg)")
        property("border", "1px solid var(--sa-color-outline)")
        backgroundColor(Color("var(--sa-color-surface-strong)"))
        property("box-shadow", "var(--sa-shadow-1)")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(18.px)

        media(mediaMaxWidth(900.px)) {
            self {
                property("width", "100%")
                property("flex", "0 0 auto")
            }
        }
    }

    val sidebarHeader by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(12.px)
    }

    val brandBadge by style {
        property("width", "40px")
        property("height", "40px")
        property("border-radius", "16px")
        backgroundColor(Color("var(--sa-color-primary-soft)"))
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        property("overflow", "hidden")
        property("border", "1px solid var(--sa-color-outline)")
    }

    val brandText by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(4.px)
    }

    val sidebarTitle by style {
        fontSize(16.px)
        fontWeight(700)
        lineHeight("1.1")
    }

    val sidebarSubtitle by style {
        fontSize(12.px)
        color(Color("var(--sa-color-secondary)"))
        lineHeight("1.3")
    }

    val navList by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(8.px)

        media(mediaMaxWidth(900.px)) {
            self {
                property("flex-wrap", "wrap")
                flexDirection(FlexDirection.Row)
            }
        }
    }

    val navItem by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        gap(8.px)
        padding(12.px, 14.px)
        property("border-radius", "12px")
        cursor("pointer")
        property("user-select", "none")
        property(
            "transition",
            "transform var(--sa-transition-fast), background-color var(--sa-transition-fast), box-shadow var(--sa-transition-fast), color var(--sa-transition-fast)"
        )
        color(Color("var(--sa-color-onSurface)"))
        backgroundColor(Color("transparent"))
        self + hover style {
            backgroundColor(Color("var(--sa-color-primary-soft)"))
            property("transform", "translateY(-1px)")
        }

        media(mediaMaxWidth(900.px)) {
            self {
                property("flex", "1 1 150px")
            }
        }
    }

    val navItemActive by style {
        backgroundColor(Color("var(--sa-color-primary)"))
        color(Color("var(--sa-color-onPrimary)"))
    }

    val navItemMeta by style {
        fontSize(12.px)
        opacity(0.78)
    }

    val navFooter by style {
        marginTop(12.px)
        padding(14.px)
        property("border-radius", "16px")
        backgroundColor(Color("var(--sa-color-surface)"))
        color(Color("var(--sa-color-onSurface)"))
        property("border", "1px solid var(--sa-color-outline)")
    }

    val navFooterTitle by style {
        fontSize(13.px)
        fontWeight(700)
        marginBottom(6.px)
    }

    val navFooterBody by style {
        fontSize(13.px)
        lineHeight("1.45")
        color(Color("var(--sa-color-secondary)"))
    }

    val main by style {
        property("flex", "1 1 auto")
        property("min-width", "0")
        property("box-sizing", "border-box")
    }

    val mainInner by style {
        property("width", "100%")
        maxWidth(1240.px)
        property("margin", "0 auto")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(16.px)
    }

    val pageTransition by style {
        property("animation", "sa-page-enter var(--sa-transition-page) both")
    }

    val pageHero by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        gap(12.px)
        padding(18.px)
        property("border-radius", "var(--sa-radius-lg)")
        backgroundColor(Color("var(--sa-color-surface-strong)"))
        property("border", "1px solid var(--sa-color-outline)")
        property("box-shadow", "var(--sa-shadow-1)")

        media(mediaMaxWidth(780.px)) {
            self {
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Start)
            }
        }
    }

    val pageHeroText by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(4.px)
    }

    val pageHeroEyebrow by style {
        fontSize(12.px)
        fontWeight(700)
        property("letter-spacing", "0.10em")
        property("text-transform", "uppercase")
        color(Color("var(--sa-color-primary)"))
    }

    val pageHeroTitle by style {
        fontSize(24.px)
        fontWeight(700)
        lineHeight("1.1")
    }

    val pageHeroSubtitle by style {
        fontSize(14.px)
        lineHeight("1.5")
        color(Color("var(--sa-color-secondary)"))
        property("max-width", "720px")
    }

    val pageHeroActions by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        property("flex-wrap", "wrap")
        justifyContent(JustifyContent.FlexEnd)
        gap(8.px)
    }

    val statusPill by style {
        property("display", "inline-flex")
        alignItems(AlignItems.Center)
        gap(8.px)
        padding(8.px, 12.px)
        property("border-radius", "999px")
        backgroundColor(Color("var(--sa-color-primary-soft)"))
        color(Color("var(--sa-color-primary)"))
        fontSize(12.px)
        fontWeight(700)
        property("border", "1px solid var(--sa-color-outline)")
    }

    val statusPillBusy by style {
        backgroundColor(Color("var(--sa-color-success-soft)"))
        color(Color("var(--sa-color-success)"))
    }

    val statusPillError by style {
        backgroundColor(Color("var(--sa-color-danger-soft)"))
        color(Color("var(--sa-color-danger)"))
    }

    val sectionHeader by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        gap(12.px)
        padding(4.px, 2.px)

        media(mediaMaxWidth(780.px)) {
            self {
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Start)
            }
        }
    }

    val sectionHeaderText by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(6.px)
    }

    val sectionTitle by style {
        fontSize(16.px)
        fontWeight(700)
        lineHeight("1.15")
    }

    val sectionBody by style {
        fontSize(14.px)
        lineHeight("1.5")
        color(Color("var(--sa-color-secondary)"))
    }

    val sectionActions by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        property("flex-wrap", "wrap")
        justifyContent(JustifyContent.FlexEnd)
        gap(8.px)
    }

    val panelStack by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(16.px)
    }

    val statsRow by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("repeat(3, minmax(0, 1fr))")
        gap(14.px)

        media(mediaMaxWidth(1100.px)) {
            self { gridTemplateColumns("repeat(2, minmax(0, 1fr))") }
        }
        media(mediaMaxWidth(680.px)) {
            self { gridTemplateColumns("1fr") }
        }
    }

    val skeletonGrid by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("repeat(3, minmax(0, 1fr))")
        gap(14.px)

        media(mediaMaxWidth(1100.px)) {
            self { gridTemplateColumns("repeat(2, minmax(0, 1fr))") }
        }
        media(mediaMaxWidth(680.px)) {
            self { gridTemplateColumns("1fr") }
        }
    }

    val card by style {
        backgroundColor(Color("var(--sa-color-surface)"))
        color(Color("var(--sa-color-onSurface)"))
        property("border-radius", "var(--sa-radius-md)")
        padding(18.px)
        property("box-shadow", "var(--sa-shadow-1)")
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
    }

    val surfaceMuted by style {
        opacity(0.64)
        property("transition", "opacity var(--sa-transition-fast)")
    }

    val statCard by style {
        property("user-select", "none")
        cursor("pointer")
        property(
            "transition",
            "transform var(--sa-transition-fast), box-shadow var(--sa-transition-fast), border-color var(--sa-transition-fast)"
        )
        self + hover style {
            property("transform", "translateY(-2px)")
            property("box-shadow", "var(--sa-shadow-2)")
            property("border-color", "var(--sa-color-outline-strong)")
        }
    }

    val statTitle by style {
        fontSize(12.px)
        fontWeight(700)
        opacity(0.72)
        property("letter-spacing", "0.02em")
    }

    val statValue by style {
        fontSize(42.px)
        fontWeight(700)
        lineHeight("1")
        marginTop(12.px)
    }

    val sinceValue by style {
        fontSize(30.px)
        fontWeight(700)
        lineHeight("1.1")
    }

    val helperText by style {
        fontSize(12.px)
        color(Color("var(--sa-color-secondary)"))
        marginTop(8.px)
        lineHeight("1.45")
    }

    val list by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(10.px)
    }

    val listRow by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
        gap(12.px)
        padding(14.px)
        property("border-radius", "var(--sa-radius-sm)")
        backgroundColor(Color("var(--sa-color-surface-strong)"))
        property("border", "1px solid var(--sa-color-outline)")

        media(mediaMaxWidth(700.px)) {
            self {
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Start)
            }
        }
    }

    val timeText by style {
        fontSize(15.px)
        fontWeight(700)
    }

    val subText by style {
        fontSize(12.px)
        opacity(0.75)
        marginTop(4.px)
        lineHeight("1.4")
    }

    val button by style {
        property("border-radius", "999px")
        padding(11.px, 16.px)
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
        backgroundColor(Color("transparent"))
        color(Color("var(--sa-color-onSurface)"))
        cursor("pointer")
        fontWeight(700)
        property("user-select", "none")
        property(
            "transition",
            "transform var(--sa-transition-fast), box-shadow var(--sa-transition-fast), background-color var(--sa-transition-fast), border-color var(--sa-transition-fast), opacity var(--sa-transition-fast)"
        )
        self + hover style {
            property("transform", "translateY(-1px)")
            property("box-shadow", "var(--sa-shadow-1)")
            property("border-color", "var(--sa-color-outline-strong)")
        }
    }

    val buttonPrimary by style {
        backgroundColor(Color("var(--sa-color-primary)"))
        color(Color("var(--sa-color-onPrimary)"))
        border {
            width(0.px)
            style(LineStyle.None)
            color(Color.transparent)
        }
    }

    val buttonDanger by style {
        backgroundColor(Color("var(--sa-color-danger-soft)"))
        color(Color("var(--sa-color-danger)"))
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("rgba(192,57,43,0.16)"))
        }
    }

    val buttonDisabled by style {
        opacity(0.45)
        cursor("not-allowed")
        property("box-shadow", "none")
        property("transform", "none")
    }

    val statsToolbar by style {
        display(DisplayStyle.Flex)
        property("flex-wrap", "wrap")
        gap(12.px)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)
    }

    val periodPills by style {
        display(DisplayStyle.Flex)
        property("flex-wrap", "wrap")
        gap(8.px)
        alignItems(AlignItems.Center)
    }

    val dateControls by style {
        display(DisplayStyle.Flex)
        property("flex-wrap", "wrap")
        gap(8.px)
        alignItems(AlignItems.Center)
    }

    val dateLabel by style {
        fontWeight(700)
        property("min-width", "160px")
        property("text-align", "center")
    }

    val dateInput by style {
        padding(11.px, 14.px)
        property("border-radius", "999px")
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
        backgroundColor(Color("var(--sa-color-surface-strong)"))
        color(Color("var(--sa-color-onSurface)"))
    }

    val chartHeader by style {
        fontWeight(700)
        marginBottom(10.px)
    }

    val chartWrap by style {
        property("width", "100%")
        property("height", "420px")
    }

    val chartSkeleton by style {
        property("width", "100%")
        property("height", "320px")
        property("border-radius", "16px")
        backgroundColor(Color("var(--sa-color-surface-strong)"))
        property("border", "1px dashed var(--sa-color-outline)")
        property("position", "relative")
        property("overflow", "hidden")
    }

    val skeletonBlock by style {
        property("width", "100%")
        property("border-radius", "12px")
        property(
            "background",
            "linear-gradient(90deg, rgba(22,29,29,0.10) 0%, rgba(22,29,29,0.18) 50%, rgba(22,29,29,0.10) 100%)"
        )
        property("background-size", "220% 100%")
        property("animation", "sa-shimmer 1.4s linear infinite")
    }

    val emptyState by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        alignItems(AlignItems.Start)
        gap(10.px)
    }

    val emptyStateTitle by style {
        fontSize(18.px)
        fontWeight(700)
    }

    val emptyStateBody by style {
        fontSize(14.px)
        lineHeight("1.55")
        color(Color("var(--sa-color-secondary)"))
    }

    val inlineError by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(10.px)
        property("border", "1px solid rgba(192,57,43,0.16)")
        backgroundColor(Color("var(--sa-color-danger-soft)"))
    }

    val inlineErrorTitle by style {
        fontSize(16.px)
        fontWeight(700)
        color(Color("var(--sa-color-danger)"))
    }

    val inlineErrorBody by style {
        fontSize(14.px)
        lineHeight("1.5")
        color(Color("var(--sa-color-onSurface)"))
    }

    init {
        "html, body, #root".style {
            property("height", "100%")
            property("width", "100%")
            property("margin", "0")
            property("padding", "0")
        }

        "body".style {
            backgroundColor(Color("var(--sa-color-bg)"))
            color(Color("var(--sa-color-onBg)"))
        }

        "button, input".style {
            property("font", "inherit")
        }
    }
}
