package com.example.data.model

enum class DateRangeOption(val label: String) {
    TODAY("Today"),
    DAYS_7("7 Days"),
    DAYS_30("30 Days"),
    DAYS_90("90 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom")
}

enum class WinLossFilter(val label: String) {
    ALL("All Trades"),
    WINS("Winning Trades"),
    LOSSES("Losing Trades"),
    BREAKEVEN("Breakeven")
}

data class TradeFilterState(
    val accountId: String = "ALL", // "ALL" or specific account ID
    val dateRangeOption: DateRangeOption = DateRangeOption.ALL_TIME,
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val symbolFilter: String = "ALL",
    val sideFilter: String = "ALL", // "ALL", "LONG", "SHORT"
    val winLossFilter: WinLossFilter = WinLossFilter.ALL,
    val searchQuery: String = ""
)
