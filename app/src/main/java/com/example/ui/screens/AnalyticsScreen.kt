package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.FinancialFormatter
import com.example.analytics.TradingReportResult
import com.example.ui.components.DayOfWeekBarChart
import com.example.ui.components.DrawdownChart
import com.example.ui.components.HourlyHeatmapChart
import com.example.ui.components.MonthlyPnlBarChart
import com.example.ui.components.PnlDistributionChart
import com.example.ui.components.SymbolBarChart
import com.example.ui.components.WinLossDonutChart
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    report: TradingReportResult,
    modifier: Modifier = Modifier
) {
    var selectedMetricExplanation by remember { mutableStateOf<Pair<String, String>?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("analytics_screen_root")
    ) {
        Text(
            text = "Analytics & Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Deep dive into trading efficiency, time patterns, and drawdown risk.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Win/Loss/BE Distribution Donut Chart
        WinLossDonutChart(
            winningCount = report.winningTrades,
            losingCount = report.losingTrades,
            breakevenCount = report.breakevenTrades,
            winRate = report.winRate
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Portfolio Drawdown Chart
        DrawdownChart(
            equityPoints = report.equityCurve,
            maxDrawdown = report.maxDrawdown,
            maxDrawdownPct = report.maxDrawdownPercent,
            currentDrawdown = report.currentDrawdown
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Directional Bias: Long vs Short Comparison
        Text(
            text = "Directional Bias (Long vs Short)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val longColor = if (report.longStats.netPnL >= 0) WinGreen else LossRed
            val shortColor = if (report.shortStats.netPnL >= 0) WinGreen else LossRed

            // Long Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .testTag("analytics_long_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("LONG", fontWeight = FontWeight.Bold, color = WinGreen, fontSize = 14.sp)
                        Icon(Icons.Default.TrendingUp, contentDescription = "Long", tint = WinGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = FinancialFormatter.formatCurrency(report.longStats.netPnL, showPlusSign = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = longColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${report.longStats.count} Trades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Win Rate: ${FinancialFormatter.formatPercent(report.longStats.winRate, showPlusSign = false)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Short Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .testTag("analytics_short_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SHORT", fontWeight = FontWeight.Bold, color = LossRed, fontSize = 14.sp)
                        Icon(Icons.Default.TrendingDown, contentDescription = "Short", tint = LossRed, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = FinancialFormatter.formatCurrency(report.shortStats.netPnL, showPlusSign = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = shortColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${report.shortStats.count} Trades", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Win Rate: ${FinancialFormatter.formatPercent(report.shortStats.winRate, showPlusSign = false)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Monthly Performance Chart
        MonthlyPnlBarChart(monthlyList = report.monthlyPerformanceList)

        Spacer(modifier = Modifier.height(16.dp))

        // 5. P&L Distribution Histogram Chart
        PnlDistributionChart(
            buckets = report.pnlDistribution,
            totalTrades = report.totalTrades,
            winningTrades = report.winningTrades,
            losingTrades = report.losingTrades
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Day of Week Performance Chart
        DayOfWeekBarChart(dayList = report.dayOfWeekPerformanceList)

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Hourly Execution Heatmap
        HourlyHeatmapChart(hourlyList = report.hourlyPerformanceList)

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Symbol Performance Breakdown Chart
        SymbolBarChart(symbolList = report.symbolPerformanceList)

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Educational Insights / Glossary Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Key Trading Metrics Explained",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                MetricHelpItem(
                    title = "Profit Factor",
                    value = String.format(Locale.getDefault(), "%.2f", report.profitFactor),
                    description = "Gross Profits divided by Gross Losses. A value above 1.5 indicates a strong statistical edge."
                ) {
                    selectedMetricExplanation = "Profit Factor" to "Profit Factor is calculated as Gross Profit ÷ Gross Loss. Values above 1.0 mean you are profitable. Professional traders target a Profit Factor between 1.5 and 2.5."
                }

                MetricHelpItem(
                    title = "Expectancy",
                    value = "${FinancialFormatter.formatCurrency(report.expectancy, showPlusSign = true)} / trade",
                    description = "The average expected return per trade executed based on your win rate and reward-to-risk ratio."
                ) {
                    selectedMetricExplanation = "Expectancy" to "Expectancy = (Win Rate × Avg Win) - (Loss Rate × Avg Loss). It represents how much money you can expect to gain or lose on average with every trade."
                }

                MetricHelpItem(
                    title = "Max Drawdown",
                    value = FinancialFormatter.formatPercent(-report.maxDrawdownPercent, showPlusSign = false),
                    description = "The maximum peak-to-trough decline in portfolio equity. Key indicator for risk management."
                ) {
                    selectedMetricExplanation = "Max Drawdown" to "Max Drawdown measures the maximum percentage loss your account experienced from a peak equity value before reaching a new peak. Managing drawdown is vital for capital preservation."
                }
            }
        }
    }

    // Metric Explanation Dialog
    selectedMetricExplanation?.let { (title, explanation) ->
        AlertDialog(
            onDismissRequest = { selectedMetricExplanation = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = { Text(explanation, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { selectedMetricExplanation = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun MetricHelpItem(
    title: String,
    value: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
    }
}
