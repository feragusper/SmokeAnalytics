/**
 * This file is part of the AAY-Chart library, originally developed by The Chance.
 * Modifications have been made to better integrate with Smoke Analytics.
 *
 * MIT License
 * Copyright (c) 2023 The Chance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package com.feragusper.smokeanalytics.libraries.chart.lineChart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.ChartDescription
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.model.GridOrientation
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.model.LegendPosition
import com.feragusper.smokeanalytics.libraries.chart.lineChart.model.LineParameters
import com.feragusper.smokeanalytics.libraries.chart.lineChart.model.LineType
import com.feragusper.smokeanalytics.libraries.chart.utils.ChartDefaultValues

/**
 * Composable function to render a line chart with optional legends.
 *
 * @param modifier Modifier for configuring the layout and appearance of the line chart.
 * @param linesParameters List of LineParameters describing the data and style for each line in the chart.
 * @param gridColor Color of the grid lines (default is Gray).
 * @param xAxisData List of labels for the X-axis.
 * @param isGrid Flag to determine whether to display grid lines (default is true).
 * @param barWidthPx Width of the grid lines (default is 1.0).
 * @param animateChart Flag to enable chart animations (default is true).
 * @param showGridWithSpacer Flag to add background spacing when showing grid lines (default is true).
 * @param descriptionStyle TextStyle for configuring the appearance of chart description (legend) text.
 * @param yAxisStyle TextStyle for configuring the appearance of the Y-axis labels.
 * @param xAxisStyle TextStyle for configuring the appearance of the X-axis labels.
 * @param chartRatio Aspect ratio of the chart (default is 0 for automatic sizing).
 * @param horizontalArrangement Horizontal arrangement for legend items (default is [Arrangement.Center]).
 * @param yAxisRange Range of values for the Y-axis (default is 0 to 100).
 * @param showXAxis Flag to determine whether to display the X-axis (default is true).
 * @param showYAxis Flag to determine whether to display the Y-axis (default is true).
 * @param oneLineChart Flag to specify if the chart should display only one line (default is false).
 * @param gridOrientation Orientation of grid lines (default is [GridOrientation.HORIZONTAL]).
 * @param legendPosition Position of the legend within the chart (default is [LegendPosition.TOP]).
 *
 * @see LineParameters
 * @see LegendPosition
 * @see GridOrientation
 */
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    linesParameters: List<LineParameters> = ChartDefaultValues.lineParameters,
    gridColor: Color = ChartDefaultValues.gridColor,
    xAxisData: List<String> = emptyList(),
    isGrid: Boolean = ChartDefaultValues.IS_SHOW_GRID,
    barWidthPx: Dp = ChartDefaultValues.backgroundLineWidth,
    animateChart: Boolean = ChartDefaultValues.ANIMATED_CHART,
    showGridWithSpacer: Boolean = ChartDefaultValues.SHOW_BACKGROUND_WITH_SPACER,
    descriptionStyle: TextStyle = ChartDefaultValues.descriptionDefaultStyle,
    yAxisStyle: TextStyle = ChartDefaultValues.axesStyle,
    xAxisStyle: TextStyle = ChartDefaultValues.axesStyle,
    chartRatio: Float = ChartDefaultValues.chartRatio,
    horizontalArrangement: Arrangement.Horizontal = ChartDefaultValues.headerArrangement,
    yAxisRange: Int = ChartDefaultValues.yAxisRange,
    showXAxis: Boolean = ChartDefaultValues.showXAxis,
    showYAxis: Boolean = ChartDefaultValues.showyAxis,
    oneLineChart: Boolean = ChartDefaultValues.specialChart,
    gridOrientation: GridOrientation = ChartDefaultValues.gridOrientation,
    legendPosition: LegendPosition = ChartDefaultValues.legendPosition
) {
    val clickedPoints = remember { mutableStateListOf<Pair<Float, Float>>() }

    Box(modifier.wrapContentHeight()) {
        Column() {
            when (legendPosition) {
                LegendPosition.TOP -> {
                    LazyRow(
                        horizontalArrangement = horizontalArrangement,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        items(linesParameters) { details ->
                            details.label?.let {
                                ChartDescription(
                                    chartColor = details.lineColor,
                                    chartName = it,
                                    descriptionStyle = descriptionStyle,
                                )
                            }
                        }
                    }

                    ChartContent(
                        modifier = if (chartRatio == 0f) Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize()
                        else Modifier
                            .padding(top = 16.dp)
                            .aspectRatio(chartRatio)
                            .fillMaxSize(),
                        linesParameters = linesParameters,
                        gridColor = gridColor,
                        xAxisData = xAxisData,
                        isShowGrid = isGrid,
                        barWidthPx = barWidthPx,
                        animateChart = animateChart,
                        showGridWithSpacer = showGridWithSpacer,
                        yAxisStyle = yAxisStyle,
                        xAxisStyle = xAxisStyle,
                        yAxisRange = yAxisRange,
                        showXAxis = showXAxis,
                        showYAxis = showYAxis,
                        specialChart = oneLineChart,
                        onChartClick = { x, y ->
                            clickedPoints.add(x to y)
                        },
                        clickedPoints = clickedPoints,
                        gridOrientation = gridOrientation
                    )
                }

                LegendPosition.BOTTOM -> {

                    ChartContent(
                        modifier = if (chartRatio == 0f) Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize()
                            .weight(1f)
                        else Modifier
                            .padding(top = 16.dp)
                            .aspectRatio(chartRatio)
                            .fillMaxSize()
                            .weight(1f),
                        linesParameters = linesParameters,
                        gridColor = gridColor,
                        xAxisData = xAxisData,
                        isShowGrid = isGrid,
                        barWidthPx = barWidthPx,
                        animateChart = animateChart,
                        showGridWithSpacer = showGridWithSpacer,
                        yAxisStyle = yAxisStyle,
                        xAxisStyle = xAxisStyle,
                        yAxisRange = yAxisRange,
                        showXAxis = showXAxis,
                        showYAxis = showYAxis,
                        specialChart = oneLineChart,
                        onChartClick = { x, y ->
                            clickedPoints.add(x to y)
                        },
                        clickedPoints = clickedPoints,
                        gridOrientation = gridOrientation
                    )
                    LazyRow(
                        horizontalArrangement = horizontalArrangement,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        items(linesParameters) { details ->
                            details.label?.let {
                                ChartDescription(
                                    chartColor = details.lineColor,
                                    chartName = it,
                                    descriptionStyle = descriptionStyle,
                                )
                            }
                        }
                    }

                }

                LegendPosition.DISAPPEAR -> {
                    ChartContent(
                        modifier = if (chartRatio == 0f) Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize()
                        else Modifier
                            .padding(top = 16.dp)
                            .aspectRatio(chartRatio)
                            .fillMaxSize(),
                        linesParameters = linesParameters,
                        gridColor = gridColor,
                        xAxisData = xAxisData,
                        isShowGrid = isGrid,
                        barWidthPx = barWidthPx,
                        animateChart = animateChart,
                        showGridWithSpacer = showGridWithSpacer,
                        yAxisStyle = yAxisStyle,
                        xAxisStyle = xAxisStyle,
                        yAxisRange = yAxisRange,
                        showXAxis = showXAxis,
                        showYAxis = showYAxis,
                        specialChart = oneLineChart,
                        onChartClick = { x, y ->
                            clickedPoints.add(x to y)
                        },
                        clickedPoints = clickedPoints,
                        gridOrientation = gridOrientation
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun LineChartPreview() {
    val sampleStats = mapOf(
        "00:00" to 0,
        "06:00" to 2,
        "12:00" to 5,
        "18:00" to 7,
        "24:00" to 10
    )

    val lineParameters: List<LineParameters> = listOf(
        LineParameters(
            label = "Sample Data",
            data = sampleStats.values.map { it.toDouble() },
            lineColor = Color.Blue,
            lineType = LineType.CURVED_LINE,
            lineShadow = true,
        )
    )

    LineChart(
        linesParameters = lineParameters,
        gridColor = Color.Gray,
        xAxisData = sampleStats.keys.toList(),
        isGrid = true,
        animateChart = true,
        showGridWithSpacer = true,
        yAxisStyle = TextStyle(
            color = Color.DarkGray
        ),
        xAxisStyle = TextStyle(
            color = Color.DarkGray
        ),
        yAxisRange = sampleStats.values.maxOrNull()?.let { it + (it * 0.2).toInt() } ?: 10,
        gridOrientation = GridOrientation.VERTICAL
    )
}
