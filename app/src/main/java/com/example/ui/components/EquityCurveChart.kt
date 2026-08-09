package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
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
import androidx.compose.ui.geometry.Size
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class ChartTimeRange(val label: String, val daysCutoff: Int) {
    WEEK_1("1W", 7),
    MONTH_1("1M", 30),
    MONTH_3("3M", 90),
    MONTH_6("6M", 180),
    YEAR_1("1Y", 365),
    ALL("ALL", 0)
}

@Composable
fun EquityCurveChart(
    points: List<EquityPoint>,
    modifier: Modifier = Modifier,
    initialRange: ChartTimeRange = ChartTimeRange.ALL
) {
    var selectedRange by remember { mutableStateOf(initialRange) }

    // Filter points based on selected time range
    val filteredPoints = remember(points, selectedRange) {
        if (points.isEmpty()) emptyList()
        else if (selectedRange == ChartTimeRange.ALL) points
        else {
            val lastTimestamp = points.last().timestamp
            val cutoffMillis = lastTimestamp - (selectedRange.daysCutoff * 24L * 60L * 60L * 1000L)
            val subList = points.filter { it.timestamp >= cutoffMillis }
            if (subList.isEmpty() && points.isNotEmpty()) points.takeLast(2) else subList
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("equity_curve_chart_container")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "EQUITY CURVE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cumulative performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Summary P&L value badge
            if (filteredPoints.isNotEmpty()) {
                val latestPoint = filteredPoints.last()
                val startingPoint = filteredPoints.first()
                val pnlDelta = latestPoint.cumulativePnL - startingPoint.cumulativePnL
                val startingBal = startingPoint.balance - startingPoint.tradePnL
                val pctReturn = if (startingBal > 0) (pnlDelta / startingBal) * 100.0 else 0.0

                val isPositive = pnlDelta >= 0
                val color = if (isPositive) WinGreen else LossRed

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FinancialFormatter.formatCurrency(latestPoint.cumulativePnL, showPlusSign = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = FinancialFormatter.formatPercent(pctReturn, showPlusSign = true),
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Range Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChartTimeRange.entries.forEach { range ->
                val isSelected = range == selectedRange
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedRange = range }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Canvas or Empty State
        if (filteredPoints.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
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
            EquityChartCanvas(points = filteredPoints)

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Balance Bar (Matching Chart Design Prompt)
            val latestPt = filteredPoints.lastOrNull()
            val startingBal = filteredPoints.firstOrNull()?.balance?.minus(filteredPoints.firstOrNull()?.tradePnL ?: 0.0) ?: 10000.0
            val currentBal = latestPt?.balance ?: startingBal
            val peakBal = filteredPoints.maxOfOrNull { it.peakBalance } ?: currentBal
            val maxDd = filteredPoints.maxOfOrNull { it.drawdownAmount } ?: 0.0

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EquityStatTile(
                        label = "Starting Balance",
                        value = FinancialFormatter.formatCurrency(startingBal, showPlusSign = false),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    EquityStatTile(
                        label = "Current Balance",
                        value = FinancialFormatter.formatCurrency(currentBal, showPlusSign = false),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    EquityStatTile(
                        label = "Peak Balance",
                        value = FinancialFormatter.formatCurrency(peakBal, showPlusSign = false),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    EquityStatTile(
                        label = "Max Drawdown",
                        value = "-${FinancialFormatter.formatCurrency(maxDd, showPlusSign = false)}",
                        valueColor = LossRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EquityStatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun EquityChartCanvas(
    points: List<EquityPoint>
) {
    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val activePoint = selectedIndex?.let { points.getOrNull(it) } ?: points.last()
    val prevPoint = selectedIndex?.let { idx -> if (idx > 0) points[idx - 1] else null }
    val changeFromPrev = prevPoint?.let { activePoint.cumulativePnL - it.cumulativePnL } ?: activePoint.tradePnL

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }

    Column {
        // Floating Active Point Tooltip Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
                        text = if (activePoint.timestamp > 0) dateFormat.format(Date(activePoint.timestamp)) else activePoint.dateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Cum. P&L: ${FinancialFormatter.formatCurrency(activePoint.cumulativePnL, showPlusSign = true)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activePoint.cumulativePnL >= 0) WinGreen else LossRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val changeColor = if (changeFromPrev >= 0) WinGreen else LossRed
                    Text(
                        text = "Change: ${FinancialFormatter.formatCurrency(changeFromPrev, showPlusSign = true)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Text(
                        text = "Balance: ${FinancialFormatter.formatCurrency(activePoint.balance, showPlusSign = false)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        val winColor = WinGreen
        val lossColor = LossRed

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val paddingLeft = 45.dp.toPx()
                            val paddingRight = 10.dp.toPx()
                            val chartWidth = width - paddingLeft - paddingRight

                            if (chartWidth > 0 && points.size > 1) {
                                val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                                val ratio = touchX / chartWidth
                                val idx = (ratio * (points.size - 1) + 0.5f).toInt().coerceIn(0, points.size - 1)
                                selectedIndex = idx
                            }
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val width = size.width
                            val paddingLeft = 45.dp.toPx()
                            val paddingRight = 10.dp.toPx()
                            val chartWidth = width - paddingLeft - paddingRight

                            if (chartWidth > 0 && points.size > 1) {
                                val touchX = (change.position.x - paddingLeft).coerceIn(0f, chartWidth)
                                val ratio = touchX / chartWidth
                                val idx = (ratio * (points.size - 1) + 0.5f).toInt().coerceIn(0, points.size - 1)
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

                if (chartWidth <= 0 || chartHeight <= 0 || points.isEmpty()) return@Canvas

                val minVal = points.minOf { it.cumulativePnL }
                val maxVal = points.maxOf { it.cumulativePnL }
                val rawRange = maxVal - minVal
                val paddedRange = if (rawRange < 10.0) 100.0 else rawRange * 1.15
                val midVal = (maxVal + minVal) / 2.0
                val yMin = midVal - (paddedRange / 2.0)
                val yMax = midVal + (paddedRange / 2.0)
                val yRange = yMax - yMin

                fun getX(index: Int): Float {
                    return if (points.size <= 1) paddingLeft else paddingLeft + (index.toFloat() / (points.size - 1)) * chartWidth
                }

                fun getY(value: Double): Float {
                    val norm = (value - yMin) / yRange
                    return paddingTop + ((1.0 - norm) * chartHeight).toFloat()
                }

                // Grid lines & Y-axis labels
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val gridY = paddingTop + (i * (chartHeight / gridSteps))
                    val gridVal = yMax - (i * (yRange / gridSteps))

                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, gridY),
                        end = Offset(width - paddingRight, gridY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Draw Y axis label
                    drawContext.canvas.nativeCanvas.apply {
                        val textStr = FinancialFormatter.formatCompactCurrency(gridVal, showPlusSign = false)
                        val paint = android.graphics.Paint().apply {
                            color = axisTextColor.hashCode()
                            textSize = 10.dp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                        drawText(textStr, paddingLeft - 8.dp.toPx(), gridY + 3.dp.toPx(), paint)
                    }
                }

                // Smooth Path Construction
                val curvePath = Path()
                val fillPath = Path()

                val firstX = getX(0)
                val firstY = getY(points.first().cumulativePnL)
                val zeroY = getY(0.0).coerceIn(paddingTop, paddingTop + chartHeight)

                curvePath.moveTo(firstX, firstY)
                fillPath.moveTo(firstX, zeroY)
                fillPath.lineTo(firstX, firstY)

                for (i in 0 until points.size - 1) {
                    val x0 = getX(i)
                    val y0 = getY(points[i].cumulativePnL)
                    val x1 = getX(i + 1)
                    val y1 = getY(points[i + 1].cumulativePnL)

                    val controlX1 = x0 + (x1 - x0) / 2f
                    val controlY1 = y0
                    val controlX2 = x0 + (x1 - x0) / 2f
                    val controlY2 = y1

                    curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                }

                val lastX = getX(points.size - 1)
                fillPath.lineTo(lastX, zeroY)
                fillPath.close()

                val overallNet = points.last().cumulativePnL - points.first().cumulativePnL
                val themeColor = if (overallNet >= 0) winColor else lossColor

                // Subtle gradient fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.18f),
                            themeColor.copy(alpha = 0.02f)
                        ),
                        startY = paddingTop,
                        endY = paddingTop + chartHeight
                    )
                )

                // Main Line Stroke
                drawPath(
                    path = curvePath,
                    color = themeColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // X-Axis Date Labels
                val labelCount = if (points.size <= 4) points.size else 4
                val stepIdx = ((points.size - 1).toFloat() / (labelCount - 1)).toInt().coerceAtLeast(1)

                val shortDateFmt = SimpleDateFormat("MMM dd", Locale.US)
                for (k in 0 until labelCount) {
                    val idx = (k * stepIdx).coerceAtMost(points.size - 1)
                    val pt = points[idx]
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

                // Selected Crosshair & Marker
                selectedIndex?.let { idx ->
                    val selPt = points[idx]
                    val selX = getX(idx)
                    val selY = getY(selPt.cumulativePnL)

                    // Vertical crosshair line
                    drawLine(
                        color = themeColor.copy(alpha = 0.7f),
                        start = Offset(selX, paddingTop),
                        end = Offset(selX, paddingTop + chartHeight),
                        strokeWidth = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Outer halo
                    drawCircle(
                        color = themeColor.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = Offset(selX, selY)
                    )
                    // Inner dot
                    drawCircle(
                        color = themeColor,
                        radius = 4.dp.toPx(),
                        center = Offset(selX, selY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(selX, selY)
                    )
                }
            }
        }
    }
}
