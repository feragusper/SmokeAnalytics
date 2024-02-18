package com.feragusper.smokeanalytics.features.home.presentation.mvi

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feragusper.smokeanalytics.features.home.domain.Smoke
import com.feragusper.smokeanalytics.features.home.presentation.R
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.timeFormatted
import com.feragusper.smokeanalytics.libraries.architecture.domain.helper.utcMillis
import com.feragusper.smokeanalytics.libraries.architecture.presentation.mvi.MVIViewState
import com.feragusper.smokeanalytics.libraries.design.CombinedPreviews
import com.feragusper.smokeanalytics.libraries.design.theme.SmokeAnalyticsTheme
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

data class HomeViewState(
    internal val displayLoading: Boolean = false,
    internal val smokesPerDay: Int? = null,
    internal val smokesPerWeek: Int? = null,
    internal val smokesPerMonth: Int? = null,
    internal val timeSinceLastCigarette: Pair<Long, Long>? = null,
    internal val latestSmokes: List<Smoke>? = null,
    internal val error: HomeResult.Error? = null,
) : MVIViewState<HomeIntent> {
    interface TestTags {
        companion object {
            const val BUTTON_ADD_SMOKE = "buttonAddSmoke"
        }
    }

    data class SwipeActionsConfig(
        val threshold: Float,
        val icon: ImageVector,
        val iconTint: Color,
        val background: Color,
        val stayDismissed: Boolean,
        val onDismiss: () -> Unit,
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        ) { contentPadding ->
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val state = rememberPullToRefreshState()
                if (state.isRefreshing) {
                    LaunchedEffect(true) {
                        intent(HomeIntent.FetchSmokes)
                    }
                }

                Box(Modifier.nestedScroll(state.nestedScrollConnection)) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(contentPadding)
                            .padding()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        stickyHeader {
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
                            Text(
                                modifier = Modifier.padding(top = 24.dp),
                                text = stringResource(id = R.string.home_smoked_today),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        latestSmokes.takeIf { !it.isNullOrEmpty() }?.let {
                            items(it) { smoke ->
                                SwipeToDismissRow(smoke, intent)
                                HorizontalDivider()
                            }
                        }
                    }
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = state,
                    )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SwipeToDismissRow(smoke: Smoke, intent: (HomeIntent) -> Unit) = BoxWithConstraints {

        var willDismissDirection: SwipeToDismissBoxValue? by remember {
            mutableStateOf(null)
        }

        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (willDismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    intent(HomeIntent.DeleteSmoke(smoke.id))
                    true
                } else {
                    false
                }
            }
        )

        val width = constraints.maxWidth.toFloat()
        val threshold = 0.4f

        LaunchedEffect(key1 = Unit, block = {
            snapshotFlow { dismissState.requireOffset() }
                .collect {
                    willDismissDirection = when {
                        it < -width * threshold -> SwipeToDismissBoxValue.EndToStart
                        else -> null
                    }
                }
        })

        val hapticFeedback = LocalHapticFeedback.current
        LaunchedEffect(key1 = willDismissDirection, block = {
            if (willDismissDirection != null) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        })

        SwipeToDismissBox(state = dismissState,
            backgroundContent =
            {
                AnimatedContent(
                    targetState = willDismissDirection != null,
                    transitionSpec = {
                        fadeIn(
                            tween(0),
                            initialAlpha = if (targetState) 1f else 0f,
                        ) togetherWith fadeOut(
                            tween(0),
                            targetAlpha = if (targetState) .7f else 0f,
                        )
                    }, label = ""
                ) { willDismiss ->
                    val revealSize = remember { Animatable(if (willDismiss) 0f else 1f) }
                    val iconSize = remember { Animatable(if (willDismiss) .8f else 1f) }
                    LaunchedEffect(key1 = Unit, block = {
                        if (willDismiss) {
                            revealSize.snapTo(0f)
                            launch {
                                revealSize.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(400)
                                )
                            }
                            iconSize.snapTo(.8f)
                            iconSize.animateTo(
                                targetValue = 1.45f,
                                animationSpec = spring(Spring.DampingRatioHighBouncy)
                            )
                            iconSize.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(Spring.DampingRatioLowBouncy)
                            )
                        }
                    })
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                            .background(color = Color.Red)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .scale(iconSize.value)
                                .offset {
                                    IntOffset(
                                        x = 0,
                                        y = (10 * (1f - iconSize.value)).roundToInt()
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberVectorPainter(Icons.Default.Delete),
                                colorFilter = ColorFilter.tint(Color.Black),
                                contentDescription = null
                            )
                        }
                    }
                }
            },
            enableDismissFromStartToEnd = false,
            content =
            {
                SmokeItem(
                    id = smoke.id,
                    date = smoke.date,
                    timeAfterPrevious = smoke.timeElapsedSincePreviousSmoke,
                    intent = intent
                )
            })
    }

    @Composable
    private fun SmokeItem(
        id: String,
        date: Date,
        timeAfterPrevious: Pair<Long, Long>,
        intent: (HomeIntent) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = date.timeFormatted(),
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
            }

            var showDatePicker by remember {
                mutableStateOf(false)
            }

            IconButton(
                modifier = Modifier.wrapContentWidth(),
                onClick = { showDatePicker = true }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_pencil),
                    contentDescription = null
                )
            }

            if (showDatePicker) {
                val selectedDateTime by lazy {
                    Calendar.getInstance().apply {
                        time = date
                    }
                }

                DateTimePickerDialog(
                    initialDateTime = date,
                    onDismiss = {
                        showDatePicker = false
                    },
                    onDateSelected = { date ->
                        selectedDateTime.time = date
                    },
                    onTimeSelected = { hour, minutes ->
                        showDatePicker = false
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hour)
                        selectedDateTime.set(Calendar.MINUTE, minutes)
                        intent(HomeIntent.EditSmoke(id, selectedDateTime.time))
                    }
                )
            }
        }

    }

    private enum class DateTimeDialogType {
        Date,
        Time
    }

    @Composable
    private fun DateTimePickerDialog(
        initialDateTime: Date,
        onDismiss: () -> Unit,
        onDateSelected: (Date) -> Unit,
        onTimeSelected: (Int, Int) -> Unit,
    ) {

        var dateTimeDialogType by remember {
            mutableStateOf(DateTimeDialogType.Date)
        }

        when (dateTimeDialogType) {
            DateTimeDialogType.Date -> {
                DatePickerDialog(
                    initialDate = initialDateTime,
                    onConfirm = {
                        onDateSelected(it)
                        dateTimeDialogType = DateTimeDialogType.Time
                    },
                    onDismiss = onDismiss,
                )
            }

            DateTimeDialogType.Time -> {
                TimePickerDialog(
                    initialDate = initialDateTime,
                    onConfirm = { hour, minute ->
                        onTimeSelected(hour, minute)
                    },
                    onDismiss = onDismiss,
                )
            }
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun DatePickerDialog(
        initialDate: Date,
        onConfirm: (Date) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = initialDate.utcMillis(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= System.currentTimeMillis()
                    }
                })

        DatePickerDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                Button(onClick = {
                    datePickerState
                        .selectedDateMillis
                        ?.let { Date(it) }
                        ?.let { onConfirm(it) }
                }) {
                    Text(text = stringResource(id = R.string.home_date_time_picker_button_ok))
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.home_date_time_picker_button_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TimePickerDialog(
        initialDate: Date,
        onDismiss: () -> Unit,
        onConfirm: (Int, Int) -> Unit,
    ) {
        val timePickerState = with(Calendar.getInstance().apply { time = initialDate }) {
            rememberTimePickerState(
                initialHour = get(Calendar.HOUR_OF_DAY),
                initialMinute = get(Calendar.MINUTE),
            )
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = stringResource(id = R.string.home_date_time_picker_title),
                        style = MaterialTheme.typography.labelMedium
                    )
                    TimePicker(
                        state = timePickerState
                    )
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = {
                            onDismiss()
                        }) {
                            Text(text = stringResource(id = R.string.home_date_time_picker_button_cancel))
                        }
                        Button(onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        }) {
                            Text(text = stringResource(id = R.string.home_date_time_picker_button_ok))
                        }
                    }
                }
            }
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
            latestSmokes = buildList {
                repeat(4) {
                    add(
                        Smoke(
                            id = "123",
                            date = Date(),
                            timeElapsedSincePreviousSmoke = 1L to 30L
                        )
                    )
                }
            },
        ).Compose {}
    }
}

