package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.TrendingDown
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.analytics.EquityPoint
import com.example.analytics.FinancialFormatter
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DrawdownChart(
    equityPoints: List<EquityPoint>,
    maxDrawdown: Double,
    maxDrawdownPct: Double,
    currentDrawdown: Double,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(equityPoints) { mutableStateOf<Int?>(null) }
    val activePoint = selectedIndex?.let { equityPoints.getOrNull(it) } ?: equityPoints.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("drawdown_chart_card")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PORTFOLIO DRAWDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Peak-to-trough decline analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Max DD: ${FinancialFormatter.formatPercent(-maxDrawdownPct, showPlusSign = false)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LossRed
                )
                Text(
                    text = "Amount: -${FinancialFormatter.formatCurrency(maxDrawdown, showPlusSign = false)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (equityPoints.size < 2) {
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
                        imageVector = Icons.Default.TrendingDown,
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
            // Selected Point Tooltip Header
            activePoint?.let { pt ->
                val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

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
                                text = if (pt.timestamp > 0) dateFormat.format(Date(pt.timestamp)) else pt.dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Drawdown: -${FinancialFormatter.formatCurrency(pt.drawdownAmount, showPlusSign = false)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pt.drawdownAmount > 0.01) LossRed else WinGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Decline: ${FinancialFormatter.formatPercent(-pt.drawdownPercent, showPlusSign = false)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pt.drawdownPercent > 0.01) LossRed else WinGreen
                            )
                            Text(
                                text = "Peak Bal: ${FinancialFormatter.formatCurrency(pt.peakBalance, showPlusSign = false)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
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
                        .pointerInput(equityPoints) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val paddingLeft = 45.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && equityPoints.size > 1) {
                                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val ratio = touchX / chartWidth
                                    val idx = (ratio * (equityPoints.size - 1) + 0.5f).toInt().coerceIn(0, equityPoints.size - 1)
                                    selectedIndex = idx
                                }
                            }
                        }
                        .pointerInput(equityPoints) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val width = size.width
                                val paddingLeft = 45.dp.toPx()
                                val paddingRight = 10.dp.toPx()
                                val chartWidth = width - paddingLeft - paddingRight

                                if (chartWidth > 0 && equityPoints.size > 1) {
                                    val touchX = (change.position.x - paddingLeft).coerceIn(0f, chartWidth)
                                    val ratio = touchX / chartWidth
                                    val idx = (ratio * (equityPoints.size - 1) + 0.5f).toInt().coerceIn(0, equityPoints.size - 1)
                                    selectedIndex = idx
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

                    val maxDdPct = equityPoints.maxOf { it.drawdownPercent }.coerceAtLeast(1.0)
                    val yMaxPct = maxDdPct * 1.2

                    fun getX(index: Int): Float {
                        return paddingLeft + (index.toFloat() / (equityPoints.size - 1)) * chartWidth
                    }

                    fun getY(ddPct: Double): Float {
                        val norm = (ddPct / yMaxPct).coerceIn(0.0, 1.0)
                        return paddingTop + (norm.toFloat() * chartHeight)
                    }

                    val zeroY = getY(0.0)

                    // Reference Grid lines
                    listOf(0.0, yMaxPct * 0.33, yMaxPct * 0.66, yMaxPct).forEach { gPct ->
                        val gY = getY(gPct)
                        drawLine(
                            color = if (gPct < 0.01) WinGreen.copy(alpha = 0.6f) else gridColor,
                            start = Offset(paddingLeft, gY),
                            end = Offset(width - paddingRight, gY),
                            strokeWidth = if (gPct < 0.01) 1.5f else 1f,
                            pathEffect = if (gPct < 0.01) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Y-axis labels
                        drawContext.canvas.nativeCanvas.apply {
                            val textStr = String.format(Locale.US, "-%.1f%%", gPct)
                            val paint = android.graphics.Paint().apply {
                                color = axisTextColor.hashCode()
                                textSize = 9.dp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                            drawText(textStr, paddingLeft - 6.dp.toPx(), gY + 3.dp.toPx(), paint)
                        }
                    }

                    // Underwater Drawdown Path
                    val ddLinePath = Path()
                    val ddFillPath = Path()

                    val firstX = getX(0)
                    val firstY = getY(equityPoints.first().drawdownPercent)

                    ddLinePath.moveTo(firstX, firstY)
                    ddFillPath.moveTo(firstX, zeroY)
                    ddFillPath.lineTo(firstX, firstY)

                    for (i in 0 until equityPoints.size - 1) {
                        val x0 = getX(i)
                        val y0 = getY(equityPoints[i].drawdownPercent)
                        val x1 = getX(i + 1)
                        val y1 = getY(equityPoints[i + 1].drawdownPercent)

                        val ctrlX1 = x0 + (x1 - x0) / 2f
                        val ctrlY1 = y0
                        val ctrlX2 = x0 + (x1 - x0) / 2f
                        val ctrlY2 = y1

                        ddLinePath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, x1, y1)
                        ddFillPath.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, x1, y1)
                    }

                    val lastX = getX(equityPoints.size - 1)
                    ddFillPath.lineTo(lastX, zeroY)
                    ddFillPath.close()

                    // Draw filled underwater region
                    drawPath(
                        path = ddFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LossRed.copy(alpha = 0.05f),
                                LossRed.copy(alpha = 0.35f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                    )

                    // Draw Drawdown line
                    drawPath(
                        path = ddLinePath,
                        color = LossRed,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // X-axis Date Labels
                    val labelCount = if (equityPoints.size <= 4) equityPoints.size else 4
                    val stepIdx = ((equityPoints.size - 1).toFloat() / (labelCount - 1)).toInt().coerceAtLeast(1)
                    val shortDateFmt = SimpleDateFormat("MMM dd", Locale.US)

                    for (k in 0 until labelCount) {
                        val idx = (k * stepIdx).coerceAtMost(equityPoints.size - 1)
                        val pt = equityPoints[idx]
                        val labelX = getX(idx)
                        val labelText = if (pt.timestamp > 0) shortDateFmt.format(Date(pt.timestamp)) else pt.dateLabel

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = axisTextColor.hashCode()
                                textSize = 9.dp.toPx()
                                isAntiAlias = true
                                textAlign = when (k) {
                                    0 -> android.graphics.Paint.Align.LEFT
                                    labelCount - 1 -> android.graphics.Paint.Align.RIGHT
                                    else -> android.graphics.Paint.Align.CENTER
                                }
                            }
                            drawText(labelText, labelX, height - 4.dp.toPx(), paint)
                        }
                    }

                    // Interactive Selected Marker
                    selectedIndex?.let { idx ->
                        val selPt = equityPoints[idx]
                        val selX = getX(idx)
                        val selY = getY(selPt.drawdownPercent)

                        drawLine(
                            color = LossRed.copy(alpha = 0.7f),
                            start = Offset(selX, paddingTop),
                            end = Offset(selX, paddingTop + chartHeight),
                            strokeWidth = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        drawCircle(
                            color = LossRed.copy(alpha = 0.3f),
                            radius = 8.dp.toPx(),
                            center = Offset(selX, selY)
                        )
                        drawCircle(
                            color = LossRed,
                            radius = 4.dp.toPx(),
                            center = Offset(selX, selY)
                        )
                    }
                }
            }
        }
    }
}
