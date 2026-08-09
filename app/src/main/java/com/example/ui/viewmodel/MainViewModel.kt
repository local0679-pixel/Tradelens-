package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAssistant
import com.example.analytics.TradeAnalyticsEngine
import com.example.analytics.TradingReportResult
import com.example.data.local.AppDatabase
import com.example.data.local.SampleDataProvider
import com.example.data.model.AccountEntity
import com.example.data.model.DateRangeOption
import com.example.data.model.TradeEntity
import com.example.data.model.TradeFilterState
import com.example.data.repository.AccountRepository
import com.example.data.repository.TradeRepository
import com.example.importer.ColumnMapping
import com.example.importer.CsvImporter
import com.example.importer.ValidationReport
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val tradeRepository = TradeRepository(db.tradeDao())
    private val accountRepository = AccountRepository(db.accountDao())

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawTrades: StateFlow<List<TradeEntity>> = tradeRepository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterState = MutableStateFlow(TradeFilterState())
    val themeMode = MutableStateFlow(ThemeMode.DARK)
    val onboardingCompleted = MutableStateFlow(false)

    val validationReport = MutableStateFlow<ValidationReport?>(null)
    val aiSummaryText = MutableStateFlow<String?>(null)
    val isAiLoading = MutableStateFlow(false)

    init {
        // Seed default accounts & sample data if database is empty on first launch
        viewModelScope.launch(Dispatchers.IO) {
            tradeRepository.allTrades.collect { list ->
                if (list.isEmpty()) {
                    accountRepository.insertAccounts(SampleDataProvider.defaultAccounts)
                    tradeRepository.insertTrades(SampleDataProvider.generateSampleTrades())
                }
            }
        }
    }

    // Combined Reactive Analytics Result
    val analyticsReport: StateFlow<TradingReportResult> = combine(
        rawTrades,
        accounts,
        filterState
    ) { trades, accList, filter ->
        val filtered = filterTrades(trades, filter)
        val selectedAccount = accList.find { it.accountId == filter.accountId }
        val startBal = selectedAccount?.initialBalance ?: 10000.0
        TradeAnalyticsEngine.analyze(filtered, startingBalance = startBal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TradingReportResult())

    val filteredTradesList: StateFlow<List<TradeEntity>> = combine(
        rawTrades,
        filterState
    ) { trades, filter ->
        filterTrades(trades, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterTrades(trades: List<TradeEntity>, filter: TradeFilterState): List<TradeEntity> {
        var list = trades

        // Account Filter
        if (filter.accountId != "ALL") {
            list = list.filter { it.accountId == filter.accountId }
        }

        // Date Range Filter
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val startMillis = when (filter.dateRangeOption) {
            DateRangeOption.TODAY -> {
                cal.timeInMillis = now
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            DateRangeOption.DAYS_7 -> now - (7L * 86400000L)
            DateRangeOption.DAYS_30 -> now - (30L * 86400000L)
            DateRangeOption.DAYS_90 -> now - (90L * 86400000L)
            DateRangeOption.THIS_YEAR -> {
                cal.timeInMillis = now
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.timeInMillis
            }
            DateRangeOption.ALL_TIME -> 0L
            DateRangeOption.CUSTOM -> filter.customStartDateMillis ?: 0L
        }

        if (startMillis > 0) {
            list = list.filter { it.timestamp >= startMillis }
        }
        if (filter.dateRangeOption == DateRangeOption.CUSTOM && filter.customEndDateMillis != null) {
            list = list.filter { it.timestamp <= filter.customEndDateMillis }
        }

        // Symbol Filter
        if (filter.symbolFilter != "ALL") {
            list = list.filter { it.symbol.equals(filter.symbolFilter, ignoreCase = true) }
        }

        // Side Filter
        if (filter.sideFilter != "ALL") {
            list = list.filter { it.side.equals(filter.sideFilter, ignoreCase = true) }
        }

        // Search Query
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.lowercase().trim()
            list = list.filter {
                it.symbol.lowercase().contains(q) ||
                (it.orderId != null && it.orderId.lowercase().contains(q)) ||
                (it.notes != null && it.notes.lowercase().contains(q))
            }
        }

        return list
    }

    fun updateAccountFilter(accId: String) {
        filterState.value = filterState.value.copy(accountId = accId)
    }

    fun updateDateRangeFilter(option: DateRangeOption) {
        filterState.value = filterState.value.copy(dateRangeOption = option)
    }

    fun updateSymbolFilter(sym: String) {
        filterState.value = filterState.value.copy(symbolFilter = sym)
    }

    fun updateSideFilter(side: String) {
        filterState.value = filterState.value.copy(sideFilter = side)
    }

    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(searchQuery = query)
    }

    fun parseAndValidateCsv(csvText: String, mapping: ColumnMapping, targetAccountId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val report = CsvImporter.parseCsvContent(
                csvText = csvText,
                mapping = mapping,
                targetAccountId = targetAccountId,
                existingTrades = rawTrades.value
            )
            validationReport.value = report
        }
    }

    fun confirmImportValidatedTrades() {
        viewModelScope.launch(Dispatchers.IO) {
            val report = validationReport.value
            if (report != null && report.parsedTrades.isNotEmpty()) {
                tradeRepository.insertTrades(report.parsedTrades)
                validationReport.value = null
            }
        }
    }

    fun addManualTrade(trade: TradeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            tradeRepository.insertTrade(trade)
        }
    }

    fun deleteTrade(trade: TradeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            tradeRepository.deleteTrade(trade)
        }
    }

    fun addAccount(name: String, currency: String, initialBalance: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAcc = AccountEntity(
                accountId = "acc_${System.currentTimeMillis()}",
                name = name,
                currency = currency,
                initialBalance = initialBalance,
                isDefault = false
            )
            accountRepository.insertAccount(newAcc)
        }
    }

    fun resetToSampleDataset() {
        viewModelScope.launch(Dispatchers.IO) {
            tradeRepository.clearAllTrades()
            accountRepository.clearAllAccounts()
            accountRepository.insertAccounts(SampleDataProvider.defaultAccounts)
            tradeRepository.insertTrades(SampleDataProvider.generateSampleTrades())
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            tradeRepository.clearAllTrades()
        }
    }

    fun generateAiSummary() {
        viewModelScope.launch {
            isAiLoading.value = true
            val currentReport = analyticsReport.value
            val summary = GeminiAssistant.generatePerformanceSummary(currentReport)
            aiSummaryText.value = summary
            isAiLoading.value = false
        }
    }
}
