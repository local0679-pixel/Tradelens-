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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.analytics.PnlBucket
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.util.Locale

@Composable
fun PnlDistributionChart(
    buckets: List<PnlBucket>,
    totalTrades: Int,
    winningTrades: Int,
    losingTrades: Int,
    modifier: Modifier = Modifier
) {
    var selectedBucketIndex by remember(buckets) { mutableStateOf<Int?>(null) }
    val activeBucket = selectedBucketIndex?.let { buckets.getOrNull(it) }

    val losingPct = if (totalTrades > 0) (losingTrades.toDouble() / totalTrades) * 100.0 else 0.0
    val winningPct = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("pnl_distribution_chart_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "P&L Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (buckets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No trade distribution data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Selected Bucket Info Header
            activeBucket?.let { b ->
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
                        Text(
                            text = "Range: ${b.rangeLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${b.count} Trades",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (b.maxPnL <= 0) LossRed else WinGreen
                        )
                    }
                }
            }

            val maxCount = buckets.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 10
            val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(buckets) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 30.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && buckets.isNotEmpty()) {
                                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val colWidth = chartWidth / buckets.size
                                    val idx = (touchX / colWidth).toInt().coerceIn(0, buckets.size - 1)
                                    selectedBucketIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 30.dp.toPx()
                    val paddingRight = 10.dp.toPx()
                    val paddingTop = 15.dp.toPx()
                    val paddingBottom = 25.dp.toPx()

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                    val numBuckets = buckets.size
                    val colWidth = chartWidth / numBuckets
                    val barWidth = (colWidth * 0.75f).coerceIn(8.dp.toPx(), 24.dp.toPx())

                    // Horizontal Grid Lines & Y-axis labels
                    val ySteps = 3
                    for (step in 0..ySteps) {
                        val yVal = maxCount * (step.toFloat() / ySteps)
                        val yPos = paddingTop + chartHeight - (step.toFloat() / ySteps) * chartHeight

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
                            drawText(yVal.toInt().toString(), paddingLeft - 5.dp.toPx(), yPos + 3.dp.toPx(), paint)
                        }
                    }

                    // Zero Divider vertical dashed line (separating loss and win buckets)
                    val zeroBucketIndex = buckets.indexOfFirst { it.minPnL >= 0 }.let { if (it == -1) numBuckets / 2 else it }
                    val zeroX = paddingLeft + (zeroBucketIndex * colWidth)

                    drawLine(
                        color = axisTextColor.copy(alpha = 0.5f),
                        start = Offset(zeroX, paddingTop),
                        end = Offset(zeroX, paddingTop + chartHeight),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Draw Histogram Bars
                    buckets.forEachIndexed { i, b ->
                        val centerX = paddingLeft + (i * colWidth) + (colWidth / 2f)
                        val barLeft = centerX - (barWidth / 2f)
                        val isSelected = selectedBucketIndex == i

                        val isLossBucket = b.maxPnL <= 0
                        val barColor = if (isLossBucket) LossRed else WinGreen
                        val drawColor = if (isSelected) barColor else barColor.copy(alpha = 0.85f)

                        val barHeight = (b.count.toFloat() / maxCount) * chartHeight
                        val topY = paddingTop + chartHeight - barHeight

                        if (b.count > 0) {
                            drawRoundRect(
                                color = drawColor,
                                topLeft = Offset(barLeft, topY),
                                size = Size(barWidth, barHeight.coerceAtLeast(3.dp.toPx())),
                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                            )

                            if (isSelected) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(barLeft - 1.5f, topY - 1.5f),
                                    size = Size(barWidth + 3f, barHeight + 3f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
                                )
                            }
                        }

                        // Label
                        if (i % 2 == 0 || i == numBuckets - 1) {
                            val labelText = FinancialFormatter.formatCompactCurrency(b.minPnL, showPlusSign = false)
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = axisTextColor.hashCode()
                                    textSize = 9.dp.toPx()
                                    isAntiAlias = true
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawText(labelText, centerX, height - 4.dp.toPx(), paint)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend Footer (Matching Mockup)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(LossRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Losing Trades: $losingTrades (${FinancialFormatter.formatPercent(losingPct, showPlusSign = false)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(WinGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Winning Trades: $winningTrades (${FinancialFormatter.formatPercent(winningPct, showPlusSign = false)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
