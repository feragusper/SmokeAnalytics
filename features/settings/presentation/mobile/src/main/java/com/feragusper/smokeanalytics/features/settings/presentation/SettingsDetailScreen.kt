package com.feragusper.smokeanalytics.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.SettingsIntent
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.AppearanceSection
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.ManageTriggersSection
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.PersonalizationSection
import com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose.PreferencesSection

/**
 * Full-screen host for a single [SettingsSection]. For now each section renders exactly what
 * the old collapsible card held; each screen is polished individually afterwards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsDetailScreen(
    section: SettingsSection,
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val viewState by remember(viewModel) { viewModel.states() }.collectAsState()
    val intent: (SettingsIntent) -> Unit = { viewModel.intents().trySend(it) }
    var draft by remember(viewState.preferences) { mutableStateOf(viewState.preferences) }
    val enabled = !viewState.displayLoading && viewState.currentEmail != null
    val backgroundColor = MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(section.titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.about_back_to_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (section) {
                SettingsSection.PREFERENCES -> PreferencesSection(
                    preferences = draft,
                    enabled = enabled,
                    onPreferencesChange = { draft = it },
                    onSave = { updated -> intent(SettingsIntent.UpdatePreferences(updated)) },
                )

                SettingsSection.PERSONALIZATION -> PersonalizationSection(
                    preferences = draft,
                    enabled = enabled,
                    onChange = { updated ->
                        draft = updated
                        intent(SettingsIntent.UpdatePreferences(updated))
                    },
                )

                SettingsSection.TRIGGERS -> ManageTriggersSection(
                    preferences = draft,
                    enabled = enabled,
                    onChange = { updated ->
                        draft = updated
                        intent(SettingsIntent.UpdatePreferences(updated))
                    },
                )

                SettingsSection.APPEARANCE -> AppearanceSection()

                SettingsSection.ABOUT -> AboutSection()
            }
        }
    }
}
