package com.feragusper.smokeanalytics.features.settings.presentation.mvi.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import com.feragusper.smokeanalytics.features.settings.presentation.R
import com.feragusper.smokeanalytics.libraries.design.compose.theme.ThemeMode
import com.feragusper.smokeanalytics.libraries.design.compose.theme.ThemeModeHolder

/**
 * Theme + language controls. Both are device-local (theme via [ThemeModeHolder], language via
 * [AppCompatDelegate.setApplicationLocales]) so this section works without an account and applies
 * instantly — it never touches the Firestore-backed user preferences.
 */
@Composable
internal fun AppearanceSection() {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        ThemeModeGroup()
        LanguageGroup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModeGroup() {
    val context = LocalContext.current
    val selected = ThemeModeHolder.current
    val options = listOf(
        ThemeMode.System to R.string.settings_theme_system,
        ThemeMode.Light to R.string.settings_theme_light,
        ThemeMode.Dark to R.string.settings_theme_dark,
    )
    SettingsChoiceGroup(title = stringResource(R.string.settings_theme)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (mode, labelRes) ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { ThemeModeHolder.set(context, mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    Text(stringResource(labelRes))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageGroup() {
    val context = LocalContext.current
    // "" = follow system; otherwise a BCP-47 tag. Tracked locally so the selection reflects
    // immediately, before AppCompat recreates the activity.
    var selectedTag by remember { mutableStateOf(currentAppLanguageTag(context)) }
    val options = listOf(
        "" to R.string.settings_language_system,
        "en" to R.string.settings_language_english,
        "es" to R.string.settings_language_spanish,
    )
    SettingsChoiceGroup(title = stringResource(R.string.settings_language)) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (tag, labelRes) ->
                SegmentedButton(
                    selected = selectedTag == tag,
                    onClick = {
                        selectedTag = tag
                        setAppLanguage(context, tag)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                ) {
                    Text(stringResource(labelRes))
                }
            }
        }
    }
}

/**
 * Applies the per-app language. On API 33+ we use the framework [LocaleManager] directly — it
 * applies and recreates the whole app at the OS level without requiring `AppCompatActivity`. Below
 * that, AppCompat's backport (auto-store service in the manifest) handles persistence.
 */
private fun setAppLanguage(context: android.content.Context, tag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeList = if (tag.isEmpty()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(tag)
        }
        context.getSystemService(LocaleManager::class.java).applicationLocales = localeList
    } else {
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }
}

/** The current app language as a base tag ("en"/"es"), or "" when following the system locale. */
private fun currentAppLanguageTag(context: android.content.Context): String {
    val locales = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales
            .let { if (it.isEmpty) return "" else it[0] }
    } else {
        val compat = AppCompatDelegate.getApplicationLocales()
        if (compat.isEmpty) return "" else compat[0]
    }
    return locales?.language ?: ""
}

@Composable
private fun SettingsChoiceGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}
