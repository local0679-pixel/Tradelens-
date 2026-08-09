package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DayOfWeekPerformance
import com.example.analytics.FinancialFormatter
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.util.Locale

@Composable
fun DayOfWeekBarChart(
    dayList: List<DayOfWeekPerformance>,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember(dayList) { mutableStateOf<Int?>(null) }
    val activeDay = selectedDayIndex?.let { dayList.getOrNull(it) } ?: dayList.maxByOrNull { it.winRate }

    val totalDayTrades = dayList.sumOf { it.count }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("day_of_week_bar_chart_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Win Rate by Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Historical trade accuracy per weekday",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (totalDayTrades == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No weekday performance data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Selected Day Callout Bar
            activeDay?.let { d ->
                val pnlColor = if (d.netPnL >= 0) WinGreen else LossRed

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = d.dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Trades: ${d.count}  |  Wins: ${d.winCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${FinancialFormatter.formatPercent(d.winRate, showPlusSign = false)} Win Rate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = WinGreen
                            )
                            Text(
                                text = "Net P&L: ${FinancialFormatter.formatCurrency(d.netPnL, showPlusSign = true)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = pnlColor
                            )
                        }
                    }
                }
            }

            val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dayList) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 35.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && dayList.isNotEmpty()) {
                                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val colWidth = chartWidth / dayList.size
                                    val idx = (touchX / colWidth).toInt().coerceIn(0, dayList.size - 1)
                                    selectedDayIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 35.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 22.dp.toPx()
                    val paddingBottom = 25.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                    // Y Grid Lines (0%, 25%, 50%, 75%, 100%)
                    val steps = listOf(100, 75, 50, 25, 0)
                    steps.forEach { pct ->
                        val yPos = paddingTop + (1.0f - (pct / 100f)) * chartHeight

                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, yPos),
                            end = Offset(width - paddingRight, yPos),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = axisTextColor.hashCode()
                                textSize = 9.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                            drawText("$pct%", paddingLeft - 5.dp.toPx(), yPos + 3.dp.toPx(), paint)
                        }
                    }

                    // Render Day Bars
                    val count = dayList.size
                    val colWidth = chartWidth / count
                    val barWidth = (colWidth * 0.52f).coerceIn(12.dp.toPx(), 32.dp.toPx())

                    dayList.forEachIndexed { i, d ->
                        val centerX = paddingLeft + (i * colWidth) + (colWidth / 2f)
                        val barLeft = centerX - (barWidth / 2f)
                        val isSelected = selectedDayIndex == i

                        val winRatePct = d.winRate.coerceIn(0.0, 100.0)
                        val barH = ((winRatePct / 100.0) * chartHeight).toFloat()
                        val topY = paddingTop + chartHeight - barH

                        val barColor = WinGreen
                        val drawColor = if (isSelected) barColor else barColor.copy(alpha = 0.85f)

                        if (barH > 0) {
                            drawRoundRect(
                                color = drawColor,
                                topLeft = Offset(barLeft, topY),
                                size = Size(barWidth, barH.coerceAtLeast(4.dp.toPx())),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            if (isSelected) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(barLeft - 1.5f, topY - 1.5f),
                                    size = Size(barWidth + 3f, barH + 3f),
                                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                                )
                            }
                        }

                        // Percentage text callout directly above each bar (Matching prompt requirement)
                        val pctText = "${winRatePct.toInt()}%"
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelected) Color.White.hashCode() else WinGreen.hashCode()
                                textSize = 10.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            drawText(pctText, centerX, (topY - 4.dp.toPx()).coerceAtLeast(14.dp.toPx()), paint)
                        }

                        // Day label on X axis
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelected) Color.White.hashCode() else axisTextColor.hashCode()
                                textSize = 10.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                if (isSelected) isFakeBoldText = true
                            }
                            drawText(d.dayName, centerX, height - 4.dp.toPx(), paint)
                        }
                    }
                }
            }
        }
    }
}

