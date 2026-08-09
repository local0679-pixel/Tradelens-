package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradeEntity
import com.example.ui.components.TradeDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.TradeLensTheme
import com.example.ui.viewmodel.MainViewModel

enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_tab_dashboard"),
    TRADES("Trades", Icons.Default.ListAlt, "nav_tab_trades"),
    ANALYTICS("Analytics", Icons.Default.BarChart, "nav_tab_analytics"),
    REPORTS("Reports", Icons.Default.Assessment, "nav_tab_reports"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_tab_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()

    val accounts by viewModel.accounts.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val report by viewModel.analyticsReport.collectAsState()
    val filteredTrades by viewModel.filteredTradesList.collectAsState()
    val validationReport by viewModel.validationReport.collectAsState()

    var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var isAiScreenActive by remember { mutableStateOf(false) }
    var isImportScreenActive by remember { mutableStateOf(false) }

    var selectedTradeForDetail by remember { mutableStateOf<TradeEntity?>(null) }

    TradeLensTheme(themeMode = themeMode) {
        if (!onboardingCompleted) {
            OnboardingScreen(
                onFinishOnboarding = { viewModel.onboardingCompleted.value = true }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "TradeLens",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                                Text(
                                    text = "Know Your Trading.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        actions = {
                            // AI Assistant Header Quick Action Button
                            IconButton(
                                onClick = {
                                    isAiScreenActive = !isAiScreenActive
                                    isImportScreenActive = false
                                },
                                modifier = Modifier.testTag("top_app_bar_ai_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = if (isAiScreenActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // CSV Import Quick Action Button
                            IconButton(
                                onClick = {
                                    isImportScreenActive = !isImportScreenActive
                                    isAiScreenActive = false
                                },
                                modifier = Modifier.testTag("top_app_bar_import_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Import Trades",
                                    tint = if (isImportScreenActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavTab.values().forEach { tab ->
                            val isSelected = (!isAiScreenActive && !isImportScreenActive && currentTab == tab)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentTab = tab
                                    isAiScreenActive = false
                                    isImportScreenActive = false
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.example.ui.theme.BrightBlue,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = com.example.ui.theme.TextDisabledDark,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Crossfade(
                        targetState = when {
                            isAiScreenActive -> "AI"
                            isImportScreenActive -> "IMPORT"
                            else -> currentTab.name
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            "AI" -> AiAssistantScreen(report = report)
                            "IMPORT" -> ImportScreen(
                                accounts = accounts,
                                validationReport = validationReport,
                                onParseCsv = { text, map, accId -> viewModel.parseAndValidateCsv(text, map, accId) },
                                onConfirmImport = { viewModel.confirmImportValidatedTrades() },
                                onAddManualTrade = { trade -> viewModel.addManualTrade(trade) }
                            )
                            NavTab.DASHBOARD.name -> DashboardScreen(
                                report = report,
                                accounts = accounts,
                                selectedAccountId = filterState.accountId,
                                selectedDateOption = filterState.dateRangeOption,
                                recentTrades = filteredTrades,
                                onAccountSelected = { accId -> viewModel.updateAccountFilter(accId) },
                                onDateOptionSelected = { opt -> viewModel.updateDateRangeFilter(opt) },
                                onTradeSelected = { trade -> selectedTradeForDetail = trade },
                                onViewAllTrades = { currentTab = NavTab.TRADES },
                                onNavigateToImport = { isImportScreenActive = true }
                            )
                            NavTab.TRADES.name -> TradesScreen(
                                trades = filteredTrades,
                                selectedAccountId = filterState.accountId,
                                searchQuery = filterState.searchQuery,
                                sideFilter = filterState.sideFilter,
                                onSearchQueryChange = { q -> viewModel.updateSearchQuery(q) },
                                onSideFilterChange = { side -> viewModel.updateSideFilter(side) },
                                onAddTrade = { trade -> viewModel.addManualTrade(trade) },
                                onDeleteTrade = { trade -> viewModel.deleteTrade(trade) },
                                onTradeSelected = { trade -> selectedTradeForDetail = trade }
                            )
                            NavTab.ANALYTICS.name -> AnalyticsScreen(report = report)
                            NavTab.REPORTS.name -> ReportsScreen(report = report)
                            NavTab.SETTINGS.name -> SettingsScreen(
                                themeMode = themeMode,
                                accounts = accounts,
                                onThemeSelected = { mode -> viewModel.themeMode.value = mode },
                                onAddAccount = { name, curr, bal -> viewModel.addAccount(name, curr, bal) },
                                onResetToSampleData = { viewModel.resetToSampleDataset() },
                                onClearAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }
        }

        // Trade Detail Dialog
        selectedTradeForDetail?.let { trade ->
            TradeDetailDialog(
                trade = trade,
                onDismiss = { selectedTradeForDetail = null },
                onDeleteTrade = { tradeToDelete -> viewModel.deleteTrade(tradeToDelete) }
            )
        }
    }
}
