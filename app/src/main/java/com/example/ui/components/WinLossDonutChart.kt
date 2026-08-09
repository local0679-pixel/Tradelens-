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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.FinancialFormatter
import com.example.ui.theme.BreakevenGray
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.util.Locale

@Composable
fun WinLossDonutChart(
    winningCount: Int,
    losingCount: Int,
    breakevenCount: Int,
    winRate: Double,
    modifier: Modifier = Modifier
) {
    val total = winningCount + losingCount + breakevenCount
    var selectedSegment by remember { mutableStateOf<String?>(null) }

    val winPct = if (total > 0) (winningCount.toDouble() / total) * 100.0 else 0.0
    val lossPct = if (total > 0) (losingCount.toDouble() / total) * 100.0 else 0.0
    val bePct = if (total > 0) (breakevenCount.toDouble() / total) * 100.0 else 0.0

    val winSweep = if (total > 0) (winningCount.toFloat() / total) * 360f else 0f
    val lossSweep = if (total > 0) (losingCount.toFloat() / total) * 360f else 0f
    val beSweep = if (total > 0) (breakevenCount.toFloat() / total) * 360f else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("win_loss_donut_chart_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WIN / LOSS DISTRIBUTION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Execution outcome breakdown ($total trades)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (total == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No trades in selected date range.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(125.dp)
                            .pointerInput(total) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                                    if (angle < 0) angle += 360f

                                    selectedSegment = when {
                                        angle <= winSweep -> "WIN"
                                        angle <= (winSweep + lossSweep) -> "LOSS"
                                        else -> "BREAKEVEN"
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = 18.dp.toPx()
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        var startAngle = -90f
                        if (winSweep > 0) {
                            val isSel = selectedSegment == "WIN"
                            drawArc(
                                color = WinGreen,
                                startAngle = startAngle,
                                sweepAngle = winSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = if (isSel) strokeWidth + 4f else strokeWidth)
                            )
                            startAngle += winSweep
                        }
                        if (lossSweep > 0) {
                            val isSel = selectedSegment == "LOSS"
                            drawArc(
                                color = LossRed,
                                startAngle = startAngle,
                                sweepAngle = lossSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = if (isSel) strokeWidth + 4f else strokeWidth)
                            )
                            startAngle += lossSweep
                        }
                        if (beSweep > 0) {
                            val isSel = selectedSegment == "BREAKEVEN"
                            drawArc(
                                color = BreakevenGray,
                                startAngle = startAngle,
                                sweepAngle = beSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = if (isSel) strokeWidth + 4f else strokeWidth)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = FinancialFormatter.formatPercent(winRate, showPlusSign = false),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Win Rate",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    LegendRow(
                        label = "Winning Trades",
                        count = winningCount,
                        pct = winPct,
                        color = WinGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LegendRow(
                        label = "Losing Trades",
                        count = losingCount,
                        pct = lossPct,
                        color = LossRed
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LegendRow(
                        label = "Breakeven",
                        count = breakevenCount,
                        pct = bePct,
                        color = BreakevenGray
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(label: String, count: Int, pct: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "$count (${FinancialFormatter.formatPercent(pct, showPlusSign = false)})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
