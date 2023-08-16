package com.feragusper.smokeanalytics.design.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
) {
    Button(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
        )
    }
}
