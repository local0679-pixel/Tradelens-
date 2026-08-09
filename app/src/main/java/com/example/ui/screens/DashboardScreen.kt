package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analytics.FinancialFormatter
import com.example.analytics.TradingReportResult
import com.example.data.model.AccountEntity
import com.example.data.model.DateRangeOption
import com.example.data.model.TradeEntity
import com.example.ui.components.AccountSelectorDropdown
import com.example.ui.components.DateRangeFilterChipGroup
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.KpiCard
import com.example.ui.components.MonthlyPnlBarChart
import com.example.ui.components.TradeRow
import com.example.ui.theme.LossRed
import com.example.ui.theme.TerminalGold
import com.example.ui.theme.WinGreen
import java.util.Locale

@Composable
fun DashboardScreen(
    report: TradingReportResult,
    accounts: List<AccountEntity>,
    selectedAccountId: String,
    selectedDateOption: DateRangeOption,
    recentTrades: List<TradeEntity>,
    onAccountSelected: (String) -> Unit,
    onDateOptionSelected: (DateRangeOption) -> Unit,
    onTradeSelected: (TradeEntity) -> Unit,
    onViewAllTrades: () -> Unit,
    onNavigateToImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("dashboard_screen_root")
    ) {
        // Account & Date Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccountSelectorDropdown(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = onAccountSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DateRangeFilterChipGroup(
            selectedOption = selectedDateOption,
            onOptionSelected = onDateOptionSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Empty State Banner if no trades exist for selected criteria
        if (report.totalTrades == 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_empty_state_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "No Data",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Trading Data Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Import your execution history to unlock real performance analytics and equity curve tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToImport,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("empty_state_import_button")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Import", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Trades")
                        }

                        OutlinedButton(
                            onClick = onNavigateToImport,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Manually")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Primary KPI Grid
        val pnlColor = if (report.netPnL >= 0) WinGreen else LossRed
        val pnlValStr = FinancialFormatter.formatCurrency(report.netPnL, showPlusSign = true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Net Realized P&L",
                value = pnlValStr,
                subtext = "Gross Profit: ${FinancialFormatter.formatCurrency(report.grossProfit, showPlusSign = false)}",
                valueColor = pnlColor,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Win Rate",
                value = FinancialFormatter.formatPercent(report.winRate, showPlusSign = false),
                subtext = "${report.winningTrades}W / ${report.losingTrades}L / ${report.breakevenTrades}BE",
                valueColor = WinGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Trades",
                value = "${report.totalTrades}",
                subtext = "Avg: ${FinancialFormatter.formatCurrency(report.avgTrade, showPlusSign = true)}",
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Profit Factor",
                value = String.format(Locale.getDefault(), "%.2f", report.profitFactor),
                subtext = "Expectancy: ${FinancialFormatter.formatCurrency(report.expectancy, showPlusSign = true)}",
                valueColor = TerminalGold,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Max Drawdown",
                value = "-${FinancialFormatter.formatCurrency(report.maxDrawdown, showPlusSign = false)}",
                subtext = FinancialFormatter.formatPercent(-report.maxDrawdownPercent, showPlusSign = false),
                valueColor = LossRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Equity Curve Chart
        EquityCurveChart(points = report.equityCurve)

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly P&L Chart
        MonthlyPnlBarChart(monthlyList = report.monthlyPerformanceList)

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Stats Grid Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Secondary Performance Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            StatRow("Average Winning Trade", FinancialFormatter.formatCurrency(report.avgWin, showPlusSign = true), WinGreen)
            StatRow("Average Losing Trade", FinancialFormatter.formatCurrency(-report.avgLoss, showPlusSign = false), LossRed)
            StatRow("Largest Winning Trade", FinancialFormatter.formatCurrency(report.largestWin, showPlusSign = true), WinGreen)
            StatRow("Largest Losing Trade", FinancialFormatter.formatCurrency(-report.largestLoss, showPlusSign = false), LossRed)
            StatRow("Longest Winning Streak", "${report.maxWinStreak} Trades", WinGreen)
            StatRow("Longest Losing Streak", "${report.maxLossStreak} Trades", LossRed)
            StatRow("Total Commissions & Fees", FinancialFormatter.formatCurrency(report.totalFees, showPlusSign = false), MaterialTheme.colorScheme.onSurfaceVariant)
            StatRow("Best Performing Symbol", report.bestSymbol, WinGreen)
            StatRow("Weakest Performing Symbol", report.worstSymbol, LossRed)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Trades Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Recent Trades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(
                onClick = onViewAllTrades,
                modifier = Modifier.testTag("dashboard_view_all_trades_button")
            ) {
                Text("View All (${report.totalTrades})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (recentTrades.isEmpty()) {
            Text(
                text = "No trades recorded for selected filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            recentTrades.take(5).forEach { trade ->
                TradeRow(
                    trade = trade,
                    onClick = { onTradeSelected(trade) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
