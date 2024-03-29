package com.feragusper.smokeanalytics.libraries.smokes.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.feragusper.smokeanalytics.libraries.smokes.presentation.R

/**
 * Displays a message indicating there are no smoke records available.
 * This is typically used in views where smoke records are expected but none exist.
 */
@Composable
fun EmptySmokes() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.smokes_no_smokes),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}