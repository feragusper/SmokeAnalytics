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
            "\"DM Sans\"",
            "\"Segoe UI\"",
            "\"Helvetica Neue\"",
            "Arial",
            "sans-serif"
        )
        property("text-rendering", "optimizeLegibility")
        property("--sa-color-primary", "#185ADB")
        property("--sa-color-onPrimary", "#F8FAFF")
        property("--sa-color-primary-soft", "rgba(24,90,219,0.12)")
        property("--sa-color-secondary", "#4D5B74")
        property("--sa-color-bg", "#F5F8FC")
        property("--sa-color-bg-accent", "#E9F0FF")
        property("--sa-color-onBg", "#111827")
        property("--sa-color-surface", "rgba(255,255,255,0.82)")
        property("--sa-color-surface-strong", "#FFFFFF")
        property("--sa-color-onSurface", "#162033")
        property("--sa-color-outline", "rgba(17,24,39,0.10)")
        property("--sa-color-outline-strong", "rgba(17,24,39,0.18)")
        property("--sa-color-danger", "#C0392B")
        property("--sa-color-danger-soft", "rgba(192,57,43,0.12)")
        property("--sa-color-success-soft", "rgba(14,159,110,0.14)")
        property("--sa-color-success", "#0E9F6E")
        property("--sa-radius-lg", "24px")
        property("--sa-radius-md", "18px")
        property("--sa-radius-sm", "14px")
        property("--sa-shadow-1", "0 18px 48px rgba(30,41,59,0.10)")
        property("--sa-shadow-2", "0 26px 56px rgba(30,41,59,0.16)")
        property(
            "--sa-shell-gradient",
            "radial-gradient(circle at top left, rgba(24,90,219,0.18), transparent 30%), linear-gradient(180deg, #F8FBFF 0%, #F5F8FC 42%, #EDF2F9 100%)"
        )
        property("--sa-transition-fast", "160ms ease")
        property("--sa-transition-page", "220ms cubic-bezier(0.22, 1, 0.36, 1)")
        backgroundColor(Color("var(--sa-color-bg)"))
        color(Color("var(--sa-color-onBg)"))
        padding(0.px)
        property("width", "100%")
        minHeight(100.vh)
        property("background-image", "var(--sa-shell-gradient)")
        property("background-attachment", "fixed")
    }

    val appRootDarkTokens by style {
        property("--sa-color-primary", "#8AB4FF")
        property("--sa-color-onPrimary", "#08152F")
        property("--sa-color-primary-soft", "rgba(138,180,255,0.18)")
        property("--sa-color-secondary", "#C5D1E5")
        property("--sa-color-bg", "#08111F")
        property("--sa-color-bg-accent", "#102036")
        property("--sa-color-onBg", "#F4F7FB")
        property("--sa-color-surface", "rgba(10,18,32,0.84)")
        property("--sa-color-surface-strong", "#0F1A2D")
        property("--sa-color-onSurface", "#EFF4FF")
        property("--sa-color-outline", "rgba(255,255,255,0.10)")
        property("--sa-color-outline-strong", "rgba(255,255,255,0.18)")
        property("--sa-color-danger-soft", "rgba(255,120,96,0.16)")
        property("--sa-color-success-soft", "rgba(57,202,154,0.16)")
        property(
            "--sa-shell-gradient",
            "radial-gradient(circle at top left, rgba(138,180,255,0.18), transparent 30%), linear-gradient(180deg, #08111F 0%, #0A1424 45%, #0B172A 100%)"
        )
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
        property(
            "background",
            "linear-gradient(180deg, rgba(255,255,255,0.90) 0%, rgba(255,255,255,0.70) 100%)"
        )
        property("backdrop-filter", "blur(18px)")
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
        property("width", "48px")
        property("height", "48px")
        property("border-radius", "16px")
        property("background", "linear-gradient(135deg, var(--sa-color-primary) 0%, #4F8CFF 100%)")
        color(Color("var(--sa-color-onPrimary)"))
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.Center)
        fontSize(20.px)
        fontWeight(700)
        property("box-shadow", "0 12px 24px rgba(24,90,219,0.28)")
        property("letter-spacing", "0.04em")
    }

    val brandText by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(4.px)
    }

    val sidebarTitle by style {
        fontSize(18.px)
        fontWeight(700)
        lineHeight("1.1")
    }

    val sidebarSubtitle by style {
        fontSize(13.px)
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
        justifyContent(JustifyContent.SpaceBetween)
        gap(8.px)
        padding(12.px, 14.px)
        property("border-radius", "16px")
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
        property("box-shadow", "0 18px 30px rgba(24,90,219,0.24)")
    }

    val navItemMeta by style {
        fontSize(12.px)
        opacity(0.78)
    }

    val navFooter by style {
        marginTop(12.px)
        padding(14.px)
        property("border-radius", "16px")
        backgroundColor(Color("var(--sa-color-bg-accent)"))
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
        gap(16.px)
        padding(22.px)
        property("border-radius", "var(--sa-radius-lg)")
        property(
            "background",
            "linear-gradient(135deg, rgba(24,90,219,0.12) 0%, rgba(255,255,255,0.80) 55%, rgba(255,255,255,0.92) 100%)"
        )
        property("border", "1px solid var(--sa-color-outline)")
        property("box-shadow", "var(--sa-shadow-1)")
        property("backdrop-filter", "blur(18px)")

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
        gap(8.px)
    }

    val pageHeroEyebrow by style {
        fontSize(12.px)
        fontWeight(700)
        property("letter-spacing", "0.12em")
        property("text-transform", "uppercase")
        color(Color("var(--sa-color-primary)"))
    }

    val pageHeroTitle by style {
        fontSize(30.px)
        fontWeight(700)
        lineHeight("1.05")
    }

    val pageHeroSubtitle by style {
        fontSize(15.px)
        lineHeight("1.5")
        color(Color("var(--sa-color-secondary)"))
        property("max-width", "720px")
    }

    val pageHeroActions by style {
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        property("flex-wrap", "wrap")
        justifyContent(JustifyContent.FlexEnd)
        gap(10.px)
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
        property("backdrop-filter", "blur(16px)")
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
        property("text-transform", "uppercase")
        property("letter-spacing", "0.08em")
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
        fontSize(13.px)
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
        backgroundColor(Color("rgba(255,255,255,0.44)"))
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
        backgroundColor(Color("rgba(255,255,255,0.66)"))
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
        property(
            "background",
            "linear-gradient(180deg, rgba(24,90,219,0.10) 0%, rgba(255,255,255,0.28) 100%)"
        )
        property("border", "1px dashed var(--sa-color-outline)")
        property("position", "relative")
        property("overflow", "hidden")
    }

    val skeletonBlock by style {
        property("width", "100%")
        property("border-radius", "12px")
        property(
            "background",
            "linear-gradient(90deg, rgba(17,24,39,0.08) 0%, rgba(17,24,39,0.16) 50%, rgba(17,24,39,0.08) 100%)"
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
