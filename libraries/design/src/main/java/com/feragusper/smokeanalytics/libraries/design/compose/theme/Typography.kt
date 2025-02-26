package com.feragusper.smokeanalytics.libraries.design.compose.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feragusper.smokeanalytics.libraries.design.compose.TypographiesPreview

/**
 * Defines the typography styles for the Smoke Analytics application using Material3's [Typography].
 * Each style is configured with specific font weight, size, and line height to ensure consistency.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)

/**
 * Previews the typography scale of the application, demonstrating the appearance of different text styles
 * defined in the theme across various font scales and UI modes.
 */
@TypographiesPreview
@Composable
private fun TypographyPreview() {
    SmokeAnalyticsTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            listOf(
                "Display Large" to MaterialTheme.typography.displayLarge,
                "Display Medium" to MaterialTheme.typography.displayMedium,
                "Display Small" to MaterialTheme.typography.displaySmall,
                "Headline Large" to MaterialTheme.typography.headlineLarge,
                "Headline Medium" to MaterialTheme.typography.headlineMedium,
                "Headline Small" to MaterialTheme.typography.headlineSmall,
                "Title Large" to MaterialTheme.typography.titleLarge,
                "Title Medium" to MaterialTheme.typography.titleMedium,
                "Title Small" to MaterialTheme.typography.titleSmall,
                "Label Medium" to MaterialTheme.typography.labelMedium,
                "Label Large" to MaterialTheme.typography.labelLarge,
                "Label Small" to MaterialTheme.typography.labelSmall,
                "Body Small" to MaterialTheme.typography.bodySmall,
                "Body Medium" to MaterialTheme.typography.bodyMedium,
                "Body Large" to MaterialTheme.typography.bodyLarge,
            ).forEach { (name, style) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = name,
                        style = style,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
