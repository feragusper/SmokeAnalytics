package com.feragusper.smokeanalytics

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

/**
 * The scaffold for the web application.
 *
 * @param tab The current tab.
 * @param onTabSelected The callback for when a tab is selected.
 * @param content The content to display.
 */
@Composable
fun WebScaffold(
    tab: WebTab,
    onTabSelected: (WebTab) -> Unit,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            height(100.vh)
        }
    }) {
        Div({
            style {
                flexGrow(1)
                padding(16.px)
            }
        }) {
            content()
        }

        WebBottomNav(
            selected = tab,
            onSelected = onTabSelected,
        )
    }
}

@Composable
private fun WebBottomNav(
    selected: WebTab,
    onSelected: (WebTab) -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceAround)
            padding(12.px)
            property(
                "border-top",
                "1px solid lightgray"
            )
        }
    }) {
        WebTab.entries.forEach { tab ->
            WebNavItem(
                label = tab.label(),
                selected = tab == selected,
                onClick = { onSelected(tab) },
            )
        }
    }
}

@Composable
private fun WebNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Div({
        onClick { onClick() }
        style {
            cursor("pointer")
            fontWeight(if (selected) "bold" else "normal")
        }
    }) {
        Text(label)
    }
}

private fun WebTab.label(): String = when (this) {
    WebTab.Home -> "Home"
    WebTab.Stats -> "Stats"
    WebTab.Settings -> "Settings"
}