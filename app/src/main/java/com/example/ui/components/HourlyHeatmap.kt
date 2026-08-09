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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.FinancialFormatter
import com.example.analytics.HourlyPerformance
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen

@Composable
fun HourlyHeatmapChart(
    hourlyList: List<HourlyPerformance>,
    modifier: Modifier = Modifier
) {
    var selectedHourIndex by remember(hourlyList) { mutableStateOf<Int?>(null) }
    val activeItem = selectedHourIndex?.let { hourlyList.getOrNull(it) } ?: hourlyList.maxByOrNull { it.count }

    val totalTradesInDay = hourlyList.sumOf { it.count }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("hourly_heatmap_chart_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Performance by Hour",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Win rate percentage distribution across 24h",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (totalTradesInDay == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hourly trading activity found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Selected Hour Floating Tooltip Banner
            activeItem?.let { h ->
                val hourRange = String.format("%02d:00", h.hour)
                val pnlColor = if (h.netPnL >= 0) WinGreen else LossRed

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
                                text = "$hourRange Win Rate: ${FinancialFormatter.formatPercent(h.winRate, showPlusSign = false)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = WinGreen
                            )
                            Text(
                                text = "Trades: ${h.count}  |  Wins: ${h.winCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net P&L: ${FinancialFormatter.formatCurrency(h.netPnL, showPlusSign = true)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
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
                        .pointerInput(hourlyList) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 35.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && hourlyList.size > 1) {
                                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val ratio = touchX / chartWidth
                                    val idx = (ratio * 23 + 0.5f).toInt().coerceIn(0, 23)
                                    selectedHourIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 35.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 15.dp.toPx()
                    val paddingBottom = 25.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                    // Y Axis steps (80%, 60%, 40%, 20%, 0%)
                    val ySteps = listOf(80, 60, 40, 20, 0)
                    ySteps.forEach { pct ->
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

                    // Calculate X & Y for each hour
                    fun getPointX(hour: Int): Float {
                        return paddingLeft + (hour / 23f) * chartWidth
                    }

                    fun getPointY(winRatePct: Double): Float {
                        val norm = (winRatePct / 100.0).coerceIn(0.0, 1.0)
                        return paddingTop + ((1.0 - norm) * chartHeight).toFloat()
                    }

                    // Build smooth curve path
                    val path = Path()
                    hourlyList.forEachIndexed { hour, item ->
                        val px = getPointX(hour)
                        val py = getPointY(item.winRate)

                        if (hour == 0) {
                            path.moveTo(px, py)
                        } else {
                            val prevX = getPointX(hour - 1)
                            val prevY = getPointY(hourlyList[hour - 1].winRate)
                            val cx1 = prevX + (px - prevX) / 2f
                            val cx2 = prevX + (px - prevX) / 2f
                            path.cubicTo(cx1, prevY, cx2, py, px, py)
                        }
                    }

                    // Draw line
                    drawPath(
                        path = path,
                        color = WinGreen,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points
                    hourlyList.forEachIndexed { hour, item ->
                        val px = getPointX(hour)
                        val py = getPointY(item.winRate)
                        val isSelected = selectedHourIndex == hour

                        if (isSelected) {
                            drawCircle(color = WinGreen.copy(alpha = 0.3f), radius = 7.dp.toPx(), center = Offset(px, py))
                            drawCircle(color = WinGreen, radius = 4.dp.toPx(), center = Offset(px, py))
                            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(px, py))
                        } else if (item.count > 0) {
                            drawCircle(color = WinGreen, radius = 2.5.dp.toPx(), center = Offset(px, py))
                        }
                    }

                    // X-Axis Hour Labels (00:00, 04:00, 08:00, 12:00, 16:00, 20:00)
                    val labelHours = listOf(0, 4, 8, 12, 16, 20, 23)
                    labelHours.forEach { h ->
                        val lx = getPointX(h)
                        val labelStr = String.format("%02d:00", h)

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = axisTextColor.hashCode()
                                textSize = 9.dp.toPx()
                                isAntiAlias = true
                                textAlign = when (h) {
                                    0 -> android.graphics.Paint.Align.LEFT
                                    23 -> android.graphics.Paint.Align.RIGHT
                                    else -> android.graphics.Paint.Align.CENTER
                                }
                            }
                            drawText(labelStr, lx, height - 4.dp.toPx(), paint)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(WinGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Win Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

