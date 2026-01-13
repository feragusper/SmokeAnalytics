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
import org.jetbrains.compose.web.css.height
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
            "Segoe UI",
            "Roboto",
            "Helvetica",
            "Arial",
            "sans-serif"
        )
        property("text-rendering", "optimizeLegibility")

        // Tokens (light defaults)
        property("--sa-color-primary", "#006A6A")
        property("--sa-color-onPrimary", "#FFFFFF")
        property("--sa-color-secondary", "#4A6363")
        property("--sa-color-bg", "#FFFFFF")
        property("--sa-color-onBg", "#000000")
        property("--sa-color-surface", "#DDE4E3")
        property("--sa-color-onSurface", "#161D1D")
        property("--sa-color-outline", "rgba(0,0,0,0.10)")

        property("--sa-radius-md", "16px")
        property("--sa-radius-sm", "12px")

        property("--sa-shadow-1", "0 6px 18px rgba(0,0,0,0.10)")
        property("--sa-shadow-2", "0 10px 30px rgba(0,0,0,0.12)")

        backgroundColor(Color("var(--sa-color-bg)"))
        color(Color("var(--sa-color-onBg)"))

        // Important: no padding here (web shell controls spacing)
        padding(0.px)
        property("width", "100%")
        minHeight(100.vh)
    }

    val appRootDarkTokens by style {
        property("--sa-color-primary", "#80D5D4")
        property("--sa-color-onPrimary", "#003737")
        property("--sa-color-secondary", "#B0CCCB")
        property("--sa-color-bg", "#000000")
        property("--sa-color-onBg", "#FFFFFF")
        property("--sa-color-surface", "#0E1514")
        property("--sa-color-onSurface", "#B0CCCB")
        property("--sa-color-outline", "rgba(255,255,255,0.12)")
    }

    // ---- Web shell (sidebar + main)
    val shell by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
        height(100.vh)
        property("width", "100%")
        backgroundColor(Color("var(--sa-color-bg)"))
        color(Color("var(--sa-color-onBg)"))
        property("overflow", "hidden")
    }

    val sidebar by style {
        property("width", "260px")
        property("flex", "0 0 260px")
        backgroundColor(Color("var(--sa-color-surface)"))
        color(Color("var(--sa-color-onSurface)"))
        property("border-right", "1px solid var(--sa-color-outline)")
        padding(16.px)
        property("box-sizing", "border-box")
    }

    val sidebarTitle by style {
        fontSize(14.px)
        fontWeight(700)
        marginBottom(12.px)
        opacity(0.9)
    }

    val navList by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(6.px)
    }

    val navItem by style {
        padding(10.px, 12.px)
        property("border-radius", "12px")
        cursor("pointer")
        property("user-select", "none")
        property("transition", "background-color 120ms ease")
        self + hover style {
            backgroundColor(Color("rgba(0,0,0,0.06)"))
        }
    }

    val navItemActive by style {
        backgroundColor(Color("var(--sa-color-primary)"))
        color(Color("var(--sa-color-onPrimary)"))
    }

    val main by style {
        property("flex", "1 1 auto")
        property("min-width", "0")
        property("overflow-y", "auto")
        padding(24.px)
        property("box-sizing", "border-box")
    }

    val mainInner by style {
        // Keep content readable but NOT centered with huge gutters
        property("width", "100%")
        maxWidth(1200.px)
        property("margin", "0 auto")
    }

    // ---- Existing pieces: keep them, but avoid forcing 720px container everywhere
    val statsRow by style {
        display(DisplayStyle.Grid)
        gridTemplateColumns("repeat(3, minmax(0, 1fr))")
        gap(12.px)

        media(mediaMaxWidth(1100.px)) { self { gridTemplateColumns("repeat(2, minmax(0, 1fr))") } }
        media(mediaMaxWidth(680.px)) { self { gridTemplateColumns("1fr") } }
    }

    val card by style {
        backgroundColor(Color("var(--sa-color-surface)"))
        color(Color("var(--sa-color-onSurface)"))
        property("border-radius", "var(--sa-radius-md)")
        padding(16.px)
        property("box-shadow", "var(--sa-shadow-1)")
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
    }

    val statCard by style {
        property("user-select", "none")
        cursor("pointer")
        property("transition", "transform 120ms ease, box-shadow 120ms ease")
        self + hover style {
            property("transform", "translateY(-1px)")
            property("box-shadow", "var(--sa-shadow-2)")
        }
    }

    val statTitle by style {
        fontSize(12.px)
        fontWeight(600)
        opacity(0.75)
    }

    val statValue by style {
        fontSize(40.px)
        fontWeight(700)
        lineHeight("1")
        marginTop(8.px)
    }

    // Titles / sections
    val sectionTitle by style {
        fontSize(14.px)
        fontWeight(700)
        marginTop(12.px)
    }

    // Since card value
    val sinceValue by style {
        fontSize(28.px)
        fontWeight(700)
        lineHeight("1.1")
    }

    // List container
    val list by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        gap(8.px)
    }

    // Buttons
    val button by style {
        property("border-radius", "999px")
        padding(10.px, 14.px)

        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }

        backgroundColor(Color("transparent"))
        color(Color("var(--sa-color-onSurface)"))
        cursor("pointer")

        property("user-select", "none")
        property("transition", "transform 120ms ease, box-shadow 120ms ease")

        self + hover style {
            property("transform", "translateY(-1px)")
            property("box-shadow", "var(--sa-shadow-1)")
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

    // List rows
    val listRow by style {
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.SpaceBetween)
        alignItems(AlignItems.Center)

        padding(12.px)
        property("border-radius", "var(--sa-radius-sm)")

        backgroundColor(Color("rgba(255,255,255,0.25)"))

        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
    }

    val timeText by style {
        fontSize(14.px)
        fontWeight(700)
    }

    val subText by style {
        fontSize(12.px)
        opacity(0.75)
        marginTop(2.px)
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
        padding(10.px, 12.px)
        property("border-radius", "999px")
        border {
            width(1.px)
            style(LineStyle.Solid)
            color(Color("var(--sa-color-outline)"))
        }
        backgroundColor(Color("transparent"))
        color(Color("var(--sa-color-onSurface)"))
    }

    val chartHeader by style {
        fontWeight(700)
        marginTop(2.px)
        property("margin-bottom", "8px")
    }

    val chartWrap by style {
        property("width", "100%")
        property("height", "420px")
    }

    init {
        // Full page reset
        "html, body, #root".style {
            property("height", "100%")
            property("width", "100%")
            property("margin", "0")
            property("padding", "0")
        }
    }
}