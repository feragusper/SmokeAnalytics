package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

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
    count: Int?,
    isLoading: Boolean
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(titleResourceId), style = MaterialTheme.typography.bodySmall)

        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .size(40.dp, 29.dp)
                    .shimmer()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        } else {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = count?.toString() ?: "0",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
