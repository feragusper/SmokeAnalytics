package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Displays a statistic related to smoke events, encapsulating a title and a count within a stylized container.
 *
 * @param modifier A [Modifier] applied to the container for customization.
 * @param titleResourceId The resource ID for the title of the statistic.
 * @param count The numerical value of the statistic.
 */
@Composable
fun Stat(
    modifier: Modifier = Modifier,
    titleResourceId: Int,
    count: Int
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(id = titleResourceId),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.displayMedium
        )
    }
}