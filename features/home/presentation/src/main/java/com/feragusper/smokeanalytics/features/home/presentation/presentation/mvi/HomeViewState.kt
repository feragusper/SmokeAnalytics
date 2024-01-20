package com.feragusper.smokeanalytics.features.home.presentation.presentation.mvi

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeAfter
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeFormatted
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import java.util.Date

data class HomeViewState(
    internal val displayLoading: Boolean = false,
    internal val smokesPerDay: Int? = null,
    internal val smokesPerWeek: Int? = null,
    internal val smokesPerMonth: Int? = null,
    internal val timeSinceLastCigarette: Pair<Long, Long>? = null,
    internal val latestSmokes: List<Smoke>? = null,
    internal val displaySmokeAddedSuccess: Boolean = false,
    internal val error: HomeResult.Error? = null,
) : MVIViewState<HomeIntent> {
    interface TestTags {
        companion object {
            const val BUTTON_ADD_SMOKE = "buttonAddSmoke"
        }
    }

    @Composable
    override fun Compose(intent: (HomeIntent) -> Unit) {
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!displayLoading) {
                    FloatingActionButton(
                        onClick = { intent(HomeIntent.AddSmoke) },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_cigarette),
                                contentDescription = ""
                            )
                            Text(
                                text = stringResource(R.string.home_button_track),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        ) {
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        smokesPerDay?.let { smokesPerDay ->
                            Stat(
                                modifier = Modifier.weight(1f),
                                titleResourceId = R.string.home_label_per_day,
                                count = smokesPerDay
                            )
                        }
                        smokesPerWeek?.let { smokesPerWeek ->
                            Stat(
                                modifier = Modifier.weight(1f),
                                titleResourceId = R.string.home_label_per_week,
                                count = smokesPerWeek
                            )
                        }
                        smokesPerMonth?.let { smokesPerMonth ->
                            Stat(
                                modifier = Modifier.weight(1f),
                                titleResourceId = R.string.home_label_per_month,
                                count = smokesPerMonth
                            )
                        }
                    }
                    timeSinceLastCigarette?.let { timeSinceLastCigarette ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomEnd,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(
                                    4.dp,
                                    Alignment.CenterVertically
                                ),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.home_since_your_last_cigarette),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = timeSinceLastCigarette.let { (hours, minutes) ->
                                        listOfNotNull(
                                            stringResource(
                                                id = R.string.home_smoked_after_hours_short,
                                                hours.toInt()
                                            ).takeIf { hours > 0 },
                                            stringResource(
                                                id = R.string.home_smoked_after_minutes_short,
                                                minutes.toInt()
                                            )
                                        ).joinToString(", ")
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Image(
                                painter = painterResource(id = R.drawable.il_cigarette_background),
                                contentDescription = null
                            )
                        }
                    }
                    latestSmokes
                        .takeIf { latestSmokes -> !latestSmokes.isNullOrEmpty() }
                        ?.let { latestSmokes ->
                            Text(
                                modifier = Modifier.padding(top = 24.dp),
                                text = stringResource(id = R.string.home_smoked_today),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            LazyColumn(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                itemsIndexed(latestSmokes) { index, smoke ->
                                    SmokeItem(
                                        time = smoke.date.timeFormatted(),
                                        timeAfterPrevious = smoke.date.timeAfter(
                                            this@HomeViewState.latestSmokes?.getOrNull(
                                                index + 1
                                            )?.date
                                        ),
                                    )
                                }
                            }
                        }
                }
            }
        }

        val context = LocalContext.current
        LaunchedEffect(error) {
            error?.let {
                when (it) {
                    HomeResult.Error.Generic -> snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.error_generic
                        )
                    )

                    HomeResult.Error.NotLoggedIn -> Toast.makeText(
                        context,
                        context.getString(R.string.error_not_logged_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Composable
    private fun SmokeItem(
        time: String,
        timeAfterPrevious: Pair<Long, Long>,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyLarge,
            )
            val (hours, minutes) = timeAfterPrevious
            Text(
                text = "${stringResource(id = R.string.home_smoked_after)} ${
                    listOfNotNull(
                        pluralStringResource(
                            id = R.plurals.home_smoked_after_hours,
                            hours.toInt(),
                            hours.toInt()
                        ).takeIf { hours > 0 },
                        pluralStringResource(
                            id = R.plurals.home_smoked_after_minutes,
                            minutes.toInt(),
                            minutes.toInt()
                        )
                    ).joinToString(" and ")
                }",
                style = MaterialTheme.typography.bodyMedium,
            )
            Divider()
        }
    }

    @Composable
    private fun Stat(
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

}

@CombinedPreviews
@Composable
private fun HomeViewLoadingPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            displayLoading = true,
        ).Compose {}
    }
}

@CombinedPreviews
@Composable
private fun HomeViewSuccessPreview() {
    SmokeAnalyticsTheme {
        HomeViewState(
            smokesPerDay = 10,
            smokesPerWeek = 20,
            smokesPerMonth = 30,
            timeSinceLastCigarette = 1L to 30L,
            latestSmokes = listOf(
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
                Smoke(Date()),
            ),
        ).Compose {}
    }
}

