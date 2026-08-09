package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
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
import com.example.analytics.FinancialFormatter
import com.example.analytics.MonthlyPerformance
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import kotlin.math.abs
import kotlin.math.max

@Composable
fun MonthlyPnlBarChart(
    monthlyList: List<MonthlyPerformance>,
    modifier: Modifier = Modifier
) {
    var selectedMonthIndex by remember(monthlyList) { mutableStateOf<Int?>(null) }
    val selectedItem = selectedMonthIndex?.let { monthlyList.getOrNull(it) } ?: monthlyList.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("monthly_pnl_bar_chart_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MONTHLY P&L DISTRIBUTION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Net performance per calendar month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (monthlyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "No Data",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No trading data yet",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Import your trades to generate your performance chart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Interactive Tooltip Card for Selected Month
            selectedItem?.let { m ->
                val losingCount = max(0, m.count - m.winCount)
                val pnlColor = if (m.netPnL >= 0) WinGreen else LossRed

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
                                text = m.displayLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Trades: ${m.count}  |  Wins: ${m.winCount}  |  Losses: $losingCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FinancialFormatter.formatCurrency(m.netPnL, showPlusSign = true),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = pnlColor
                            )
                            Text(
                                text = "Win Rate: ${FinancialFormatter.formatPercent(m.winRate, showPlusSign = false)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(monthlyList) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 45.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && monthlyList.isNotEmpty()) {
                                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val colWidth = chartWidth / monthlyList.size
                                    val idx = (touchX / colWidth).toInt().coerceIn(0, monthlyList.size - 1)
                                    selectedMonthIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 45.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 15.dp.toPx()
                    val paddingBottom = 25.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                    val maxAbsVal = monthlyList.maxOfOrNull { abs(it.netPnL) }?.coerceAtLeast(10.0) ?: 100.0
                    val roundedMax = maxAbsVal * 1.15

                    fun getY(value: Double): Float {
                        val norm = (value / roundedMax).coerceIn(-1.0, 1.0)
                        val centerY = paddingTop + (chartHeight / 2f)
                        return centerY - (norm.toFloat() * (chartHeight / 2f))
                    }

                    val zeroY = getY(0.0)

                    // Reference Grid lines
                    listOf(roundedMax, roundedMax / 2.0, 0.0, -roundedMax / 2.0, -roundedMax).forEach { gVal ->
                        val gY = getY(gVal)
                        drawLine(
                            color = if (abs(gVal) < 0.01) axisTextColor.copy(alpha = 0.5f) else gridColor,
                            start = Offset(paddingLeft, gY),
                            end = Offset(width - paddingRight, gY),
                            strokeWidth = if (abs(gVal) < 0.01) 1.5f else 1f,
                            pathEffect = if (abs(gVal) < 0.01) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Y-axis labels
                        drawContext.canvas.nativeCanvas.apply {
                            val textStr = FinancialFormatter.formatCompactCurrency(gVal, showPlusSign = false)
                            val paint = android.graphics.Paint().apply {
                                color = axisTextColor.hashCode()
                                textSize = 9.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                            drawText(textStr, paddingLeft - 6.dp.toPx(), gY + 3.dp.toPx(), paint)
                        }
                    }

                    // Render Bars
                    val count = monthlyList.size
                    val colWidth = chartWidth / count
                    val barWidth = (colWidth * 0.55f).coerceIn(12.dp.toPx(), 36.dp.toPx())

                    monthlyList.forEachIndexed { i, m ->
                        val centerX = paddingLeft + (i * colWidth) + (colWidth / 2f)
                        val barLeft = centerX - (barWidth / 2f)
                        val pnl = m.netPnL
                        val isSelected = selectedMonthIndex == i

                        val barY = getY(pnl)
                        val barTop = minOf(zeroY, barY)
                        val barBottom = maxOf(zeroY, barY)
                        val rawBarHeight = (barBottom - barTop).coerceAtLeast(3.dp.toPx())

                        val barColor = if (pnl >= 0) WinGreen else LossRed
                        val displayColor = if (isSelected) barColor else barColor.copy(alpha = 0.85f)

                        drawRoundRect(
                            color = displayColor,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, rawBarHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Selection outline highlight
                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(barLeft - 2f, barTop - 2f),
                                size = Size(barWidth + 4f, rawBarHeight + 4f),
                                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                            )
                        }

                        // Month Label on X axis
                        val labelText = m.displayLabel.take(3)
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelected) Color.White.hashCode() else axisTextColor.hashCode()
                                textSize = 10.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                if (isSelected) isFakeBoldText = true
                            }
                            drawText(labelText, centerX, height - 4.dp.toPx(), paint)
                        }
                    }
                }
            }
        }
    }
}
