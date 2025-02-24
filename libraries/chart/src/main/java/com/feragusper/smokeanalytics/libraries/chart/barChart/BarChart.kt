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

package com.feragusper.smokeanalytics.libraries.chart.barChart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.feragusper.smokeanalytics.libraries.chart.barChart.components.BarChartContent
import com.feragusper.smokeanalytics.libraries.chart.barChart.model.BarParameters
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.ChartDescription
import com.feragusper.smokeanalytics.libraries.chart.baseComponents.model.LegendPosition
import com.feragusper.smokeanalytics.libraries.chart.utils.ChartDefaultValues

/**
 * Composable function to render a bar chart with an optional legend.
 *
 * @param chartParameters List of BarParameters describing the data for the bar chart.
 * @param gridColor Color of the grid lines (default is Gray).
 * @param xAxisData List of labels for the X-axis.
 * @param isShowGrid Flag to determine whether to display grid lines (default is true).
 * @param animateChart Flag to enable chart animations (default is true).
 * @param showGridWithSpacer Flag to add background spacing when showing grid lines (default is true).
 * @param descriptionStyle TextStyle for configuring the appearance of chart description (legend) text.
 * @param yAxisStyle TextStyle for configuring the appearance of the Y-axis labels.
 * @param xAxisStyle TextStyle for configuring the appearance of the X-axis labels.
 * @param horizontalArrangement Horizontal arrangement for legend items (default is [Arrangement.Center]).
 * @param backgroundLineWidth Width of the background grid lines (default is 1.0).
 * @param yAxisRange Range of values for the Y-axis (default is 0 to 100).
 * @param showXAxis Flag to determine whether to display the X-axis (default is true).
 * @param showYAxis Flag to determine whether to display the Y-axis (default is true).
 * @param barWidth Width of the bars in the chart (default is automatic).
 * @param spaceBetweenBars Space between bars within the same group (default is automatic).
 * @param spaceBetweenGroups Space between groups of bars (default is automatic).
 * @param legendPosition Position of the legend within the chart (default is [LegendPosition.TOP]).
 * @param barCornerRadius radius of the bar corner in the chart (default is zero).
 *
 * @see BarParameters
 * @see LegendPosition
 */
@Composable
fun BarChart(
    chartParameters: List<BarParameters> = ChartDefaultValues.barParameters,
    gridColor: Color = ChartDefaultValues.gridColor,
    xAxisData: List<String> = emptyList(),
    isShowGrid: Boolean = ChartDefaultValues.IS_SHOW_GRID,
    animateChart: Boolean = ChartDefaultValues.ANIMATED_CHART,
    showGridWithSpacer: Boolean = ChartDefaultValues.SHOW_BACKGROUND_WITH_SPACER,
    descriptionStyle: TextStyle = ChartDefaultValues.descriptionDefaultStyle,
    yAxisStyle: TextStyle = ChartDefaultValues.axesStyle,
    xAxisStyle: TextStyle = ChartDefaultValues.axesStyle,
    horizontalArrangement: Arrangement.Horizontal = ChartDefaultValues.headerArrangement,
    backgroundLineWidth: Float = ChartDefaultValues.backgroundLineWidth.value,
    yAxisRange: Int = ChartDefaultValues.yAxisRange,
    showXAxis: Boolean = ChartDefaultValues.showXAxis,
    showYAxis: Boolean = ChartDefaultValues.showyAxis,
    barWidth: Dp = ChartDefaultValues.barWidth,
    spaceBetweenBars: Dp = ChartDefaultValues.spaceBetweenBars,
    spaceBetweenGroups: Dp = ChartDefaultValues.spaceBetweenGroups,
    legendPosition: LegendPosition = ChartDefaultValues.legendPosition,
    barCornerRadius: Dp = ChartDefaultValues.barCornerRadius
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (legendPosition) {
            LegendPosition.TOP -> {
                LazyRow(
                    horizontalArrangement = horizontalArrangement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    items(chartParameters) { details ->
                        details.dataName?.let {
                            ChartDescription(
                                chartColor = details.barColor,
                                chartName = it,
                                descriptionStyle = descriptionStyle,
                            )
                        }
                    }
                }

                BarChartContent(
                    barsParameters = chartParameters,
                    gridColor = gridColor,
                    xAxisData = xAxisData,
                    isShowGrid = isShowGrid,
                    animateChart = animateChart,
                    showGridWithSpacer = showGridWithSpacer,
                    yAxisStyle = yAxisStyle,
                    xAxisStyle = xAxisStyle,
                    backgroundLineWidth = backgroundLineWidth,
                    yAxisRange = yAxisRange,
                    showXAxis = showXAxis,
                    showYAxis = showYAxis,
                    barWidth = barWidth,
                    spaceBetweenBars = spaceBetweenBars,
                    spaceBetweenGroups = spaceBetweenGroups,
                    barCornerRadius = barCornerRadius
                )
            }

            LegendPosition.BOTTOM -> {
                BarChartContent(
                    barsParameters = chartParameters,
                    gridColor = gridColor,
                    xAxisData = xAxisData,
                    isShowGrid = isShowGrid,
                    animateChart = animateChart,
                    showGridWithSpacer = showGridWithSpacer,
                    yAxisStyle = yAxisStyle,
                    xAxisStyle = xAxisStyle,
                    backgroundLineWidth = backgroundLineWidth,
                    yAxisRange = yAxisRange,
                    showXAxis = showXAxis,
                    showYAxis = showYAxis,
                    barWidth = barWidth,
                    spaceBetweenBars = spaceBetweenBars,
                    spaceBetweenGroups = spaceBetweenGroups,
                    modifier = Modifier.weight(1f),
                    barCornerRadius = barCornerRadius
                )

                LazyRow(
                    horizontalArrangement = horizontalArrangement,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {

                    items(chartParameters) { details ->
                        details.dataName?.let {
                            ChartDescription(
                                chartColor = details.barColor,
                                chartName = it,
                                descriptionStyle = descriptionStyle,
                            )
                        }
                    }
                }
            }

            LegendPosition.DISAPPEAR -> {
                BarChartContent(
                    barsParameters = chartParameters,
                    gridColor = gridColor,
                    xAxisData = xAxisData,
                    isShowGrid = isShowGrid,
                    animateChart = animateChart,
                    showGridWithSpacer = showGridWithSpacer,
                    yAxisStyle = yAxisStyle,
                    xAxisStyle = xAxisStyle,
                    backgroundLineWidth = backgroundLineWidth,
                    yAxisRange = yAxisRange,
                    showXAxis = showXAxis,
                    showYAxis = showYAxis,
                    barWidth = barWidth,
                    spaceBetweenBars = spaceBetweenBars,
                    spaceBetweenGroups = spaceBetweenGroups,
                    barCornerRadius = barCornerRadius
                )

            }
        }

    }
}

@Composable
@Preview(showBackground = true)
fun BarChartPreview() {
    // Datos de ejemplo para el gráfico de barras
    val sampleData = listOf(
        BarParameters(
            dataName = "Consumption",
            data = listOf(
                3.0,
                1.5,
                2.5,
                0.5,
                4.0,
                3.5,
                2.0
            ), // Debe coincidir con el tamaño de xAxisLabels
            barColor = Color.Blue
        )
    )

    // Labels del eje X
    val xAxisLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Estilos de texto para los ejes
    val yAxisStyle = TextStyle(
        color = Color.DarkGray,
    )
    val xAxisStyle = TextStyle(
        color = Color.DarkGray,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        BarChart(
            chartParameters = sampleData,
            gridColor = Color.LightGray,
            xAxisData = xAxisLabels,
            isShowGrid = true,
            animateChart = true,
            showGridWithSpacer = true,
            yAxisStyle = yAxisStyle,
            xAxisStyle = xAxisStyle,
            showXAxis = true,
            showYAxis = true,
            legendPosition = LegendPosition.TOP
        )
    }
}
