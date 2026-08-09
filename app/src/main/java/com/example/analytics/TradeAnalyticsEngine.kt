package com.example.analytics

import com.example.data.model.TradeEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class EquityPoint(
    val timestamp: Long,
    val dateLabel: String,
    val tradeId: Long,
    val tradePnL: Double,
    val cumulativePnL: Double,
    val balance: Double,
    val peakBalance: Double,
    val drawdownAmount: Double,
    val drawdownPercent: Double
)

data class PnlBucket(
    val rangeLabel: String,
    val minPnL: Double,
    val maxPnL: Double,
    val count: Int
)

data class LongShortPerformance(
    val side: String, // "LONG" or "SHORT"
    val count: Int,
    val winCount: Int,
    val lossCount: Int,
    val winRate: Double,
    val netPnL: Double,
    val avgPnL: Double,
    val profitFactor: Double
)

data class SymbolPerformance(
    val symbol: String,
    val count: Int,
    val winCount: Int,
    val lossCount: Int,
    val winRate: Double,
    val netPnL: Double,
    val avgPnL: Double,
    val profitFactor: Double,
    val bestTradePnL: Double,
    val worstTradePnL: Double
)

data class DayOfWeekPerformance(
    val dayName: String, // Monday - Sunday
    val dayIndex: Int, // 1 = Sun, 2 = Mon ... 7 = Sat
    val count: Int,
    val winCount: Int,
    val winRate: Double,
    val netPnL: Double,
    val avgPnL: Double
)

data class HourlyPerformance(
    val hour: Int, // 0..23
    val hourLabel: String, // "00:00", "01:00"...
    val count: Int,
    val winCount: Int,
    val winRate: Double,
    val netPnL: Double
)

data class MonthlyPerformance(
    val yearMonth: String, // e.g. "2026-05"
    val displayLabel: String, // e.g. "May 2026"
    val count: Int,
    val winCount: Int,
    val winRate: Double,
    val netPnL: Double,
    val grossProfit: Double,
    val grossLoss: Double,
    val profitFactor: Double
)

data class DailyPerformance(
    val dateString: String, // YYYY-MM-DD
    val count: Int,
    val winCount: Int,
    val lossCount: Int,
    val netPnL: Double,
    val trades: List<TradeEntity>
)

data class BehavioralInsight(
    val title: String,
    val description: String,
    val metricHighlight: String,
    val isPositive: Boolean
)

data class TradingReportResult(
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val breakevenTrades: Int = 0,
    val winRate: Double = 0.0,
    val lossRate: Double = 0.0,
    val breakevenRate: Double = 0.0,
    val netPnL: Double = 0.0,
    val grossProfit: Double = 0.0,
    val grossLoss: Double = 0.0,
    val avgTrade: Double = 0.0,
    val avgWin: Double = 0.0,
    val avgLoss: Double = 0.0,
    val largestWin: Double = 0.0,
    val smallestWin: Double = 0.0,
    val largestLoss: Double = 0.0,
    val smallestLoss: Double = 0.0,
    val medianWin: Double = 0.0,
    val medianLoss: Double = 0.0,
    val profitFactor: Double = 0.0,
    val expectancy: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val maxDrawdownPercent: Double = 0.0,
    val avgDrawdown: Double = 0.0,
    val currentDrawdown: Double = 0.0,
    val peakBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val startingBalance: Double = 10000.0,
    val maxWinStreak: Int = 0,
    val maxLossStreak: Int = 0,
    val currentWinStreak: Int = 0,
    val currentLossStreak: Int = 0,
    val avgHoldingTimeMillis: Long = 0L,
    val longestHoldingTimeMillis: Long = 0L,
    val shortestHoldingTimeMillis: Long = 0L,
    val totalFees: Double = 0.0,
    val totalVolume: Double = 0.0,
    val bestSymbol: String = "N/A",
    val worstSymbol: String = "N/A",
    val bestDayOfWeek: String = "N/A",
    val worstDayOfWeek: String = "N/A",
    val bestHourOfDay: String = "N/A",
    val worstHourOfDay: String = "N/A",
    val longStats: LongShortPerformance = LongShortPerformance("LONG", 0, 0, 0, 0.0, 0.0, 0.0, 0.0),
    val shortStats: LongShortPerformance = LongShortPerformance("SHORT", 0, 0, 0, 0.0, 0.0, 0.0, 0.0),
    val equityCurve: List<EquityPoint> = emptyList(),
    val pnlDistribution: List<PnlBucket> = emptyList(),
    val symbolPerformanceList: List<SymbolPerformance> = emptyList(),
    val dayOfWeekPerformanceList: List<DayOfWeekPerformance> = emptyList(),
    val hourlyPerformanceList: List<HourlyPerformance> = emptyList(),
    val monthlyPerformanceList: List<MonthlyPerformance> = emptyList(),
    val dailyPerformanceMap: Map<String, DailyPerformance> = emptyMap(),
    val bestTrades: List<TradeEntity> = emptyList(),
    val worstTrades: List<TradeEntity> = emptyList(),
    val behavioralInsights: List<BehavioralInsight> = emptyList()
)

object TradeAnalyticsEngine {

    fun analyze(trades: List<TradeEntity>, startingBalance: Double = 10000.0): TradingReportResult {
        if (trades.isEmpty()) {
            return TradingReportResult(startingBalance = startingBalance, currentBalance = startingBalance)
        }

        val sortedTrades = trades.sortedBy { it.timestamp }
        val totalTrades = sortedTrades.size

        val wins = sortedTrades.filter { (it.realizedPnL - it.fee) > 0.0001 }
        val losses = sortedTrades.filter { (it.realizedPnL - it.fee) < -0.0001 }
        val breakevens = sortedTrades.filter { abs(it.realizedPnL - it.fee) <= 0.0001 }

        val winningCount = wins.size
        val losingCount = losses.size
        val breakevenCount = breakevens.size

        val winRate = if (totalTrades > 0) (winningCount.toDouble() / totalTrades) * 100.0 else 0.0
        val lossRate = if (totalTrades > 0) (losingCount.toDouble() / totalTrades) * 100.0 else 0.0
        val breakevenRate = if (totalTrades > 0) (breakevenCount.toDouble() / totalTrades) * 100.0 else 0.0

        var grossProfit = 0.0
        var grossLoss = 0.0
        var totalFees = 0.0
        var totalVolume = 0.0

        sortedTrades.forEach { trade ->
            val netTradePnL = trade.realizedPnL - trade.fee
            if (netTradePnL > 0) {
                grossProfit += netTradePnL
            } else if (netTradePnL < 0) {
                grossLoss += abs(netTradePnL)
            }
            totalFees += trade.fee
            totalVolume += trade.volume
        }

        val netPnL = grossProfit - grossLoss
        val avgTrade = if (totalTrades > 0) netPnL / totalTrades else 0.0
        val avgWin = if (winningCount > 0) grossProfit / winningCount else 0.0
        val avgLoss = if (losingCount > 0) grossLoss / losingCount else 0.0

        val winPnLs = wins.map { it.realizedPnL - it.fee }.sorted()
        val lossPnLs = losses.map { abs(it.realizedPnL - it.fee) }.sorted()

        val largestWin = winPnLs.lastOrNull() ?: 0.0
        val smallestWin = winPnLs.firstOrNull() ?: 0.0
        val largestLoss = lossPnLs.lastOrNull() ?: 0.0
        val smallestLoss = lossPnLs.firstOrNull() ?: 0.0

        val medianWin = if (winPnLs.isNotEmpty()) {
            if (winPnLs.size % 2 == 1) winPnLs[winPnLs.size / 2]
            else (winPnLs[winPnLs.size / 2 - 1] + winPnLs[winPnLs.size / 2]) / 2.0
        } else 0.0

        val medianLoss = if (lossPnLs.isNotEmpty()) {
            if (lossPnLs.size % 2 == 1) lossPnLs[lossPnLs.size / 2]
            else (lossPnLs[lossPnLs.size / 2 - 1] + lossPnLs[lossPnLs.size / 2]) / 2.0
        } else 0.0

        val profitFactor = if (grossLoss > 0.0001) grossProfit / grossLoss else if (grossProfit > 0) 99.99 else 0.0
        val expectancy = (winRate / 100.0 * avgWin) - (lossRate / 100.0 * avgLoss)

        // Equity Curve & Drawdown Analysis
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val equityCurve = mutableListOf<EquityPoint>()

        var runningPnL = 0.0
        var runningBalance = startingBalance
        var peakBalance = startingBalance
        var maxDdAmount = 0.0
        var maxDdPercent = 0.0
        val drawdownsList = mutableListOf<Double>()

        // Starting point
        equityCurve.add(
            EquityPoint(
                timestamp = if (sortedTrades.isNotEmpty()) sortedTrades.first().timestamp - 3600000L else System.currentTimeMillis(),
                dateLabel = "Start",
                tradeId = 0L,
                tradePnL = 0.0,
                cumulativePnL = 0.0,
                balance = startingBalance,
                peakBalance = startingBalance,
                drawdownAmount = 0.0,
                drawdownPercent = 0.0
            )
        )

        sortedTrades.forEach { trade ->
            val tradeNet = trade.realizedPnL - trade.fee
            runningPnL += tradeNet
            runningBalance += tradeNet
            if (runningBalance > peakBalance) {
                peakBalance = runningBalance
            }
            val ddAmount = peakBalance - runningBalance
            val ddPct = if (peakBalance > 0) (ddAmount / peakBalance) * 100.0 else 0.0

            if (ddAmount > maxDdAmount) {
                maxDdAmount = ddAmount
            }
            if (ddPct > maxDdPercent) {
                maxDdPercent = ddPct
            }
            drawdownsList.add(ddAmount)

            equityCurve.add(
                EquityPoint(
                    timestamp = trade.timestamp,
                    dateLabel = dateFormat.format(Date(trade.timestamp)),
                    tradeId = trade.id,
                    tradePnL = tradeNet,
                    cumulativePnL = runningPnL,
                    balance = runningBalance,
                    peakBalance = peakBalance,
                    drawdownAmount = ddAmount,
                    drawdownPercent = ddPct
                )
            )
        }

        val avgDrawdown = if (drawdownsList.isNotEmpty()) drawdownsList.average() else 0.0
        val currentDrawdown = peakBalance - runningBalance

        // Streaks Calculation
        var maxWinStreak = 0
        var maxLossStreak = 0
        var currentWinStreak = 0
        var currentLossStreak = 0

        var tempWinStreak = 0
        var tempLossStreak = 0

        sortedTrades.forEach { trade ->
            val net = trade.realizedPnL - trade.fee
            if (net > 0.0001) {
                tempWinStreak++
                tempLossStreak = 0
                maxWinStreak = max(maxWinStreak, tempWinStreak)
            } else if (net < -0.0001) {
                tempLossStreak++
                tempWinStreak = 0
                maxLossStreak = max(maxLossStreak, tempLossStreak)
            }
        }
        currentWinStreak = tempWinStreak
        currentLossStreak = tempLossStreak

        // Holding Duration Analysis
        val validDurations = sortedTrades.map { it.holdingDurationMillis }.filter { it > 0 }
        val avgHoldingTime = if (validDurations.isNotEmpty()) validDurations.average().toLong() else 0L
        val longestHoldingTime = validDurations.maxOrNull() ?: 0L
        val shortestHoldingTime = validDurations.minOrNull() ?: 0L

        // Long vs Short
        val longTrades = sortedTrades.filter { it.side.equals("LONG", ignoreCase = true) || it.side.equals("BUY", ignoreCase = true) }
        val shortTrades = sortedTrades.filter { it.side.equals("SHORT", ignoreCase = true) || it.side.equals("SELL", ignoreCase = true) }

        val longPerformance = calculateSideStats("LONG", longTrades)
        val shortPerformance = calculateSideStats("SHORT", shortTrades)

        // Symbol Performance
        val symbolGroups = sortedTrades.groupBy { it.symbol }
        val symbolPerfList = symbolGroups.map { (sym, tradeList) ->
            val symWins = tradeList.count { (it.realizedPnL - it.fee) > 0.0001 }
            val symLosses = tradeList.count { (it.realizedPnL - it.fee) < -0.0001 }
            val symNet = tradeList.sumOf { it.realizedPnL - it.fee }
            val symGrossProfit = tradeList.filter { (it.realizedPnL - it.fee) > 0 }.sumOf { it.realizedPnL - it.fee }
            val symGrossLoss = tradeList.filter { (it.realizedPnL - it.fee) < 0 }.sumOf { abs(it.realizedPnL - it.fee) }
            val symWinRate = if (tradeList.isNotEmpty()) (symWins.toDouble() / tradeList.size) * 100.0 else 0.0
            val symPF = if (symGrossLoss > 0.0001) symGrossProfit / symGrossLoss else if (symGrossProfit > 0) 99.99 else 0.0
            val bestTrade = tradeList.maxOfOrNull { it.realizedPnL - it.fee } ?: 0.0
            val worstTrade = tradeList.minOfOrNull { it.realizedPnL - it.fee } ?: 0.0

            SymbolPerformance(
                symbol = sym,
                count = tradeList.size,
                winCount = symWins,
                lossCount = symLosses,
                winRate = symWinRate,
                netPnL = symNet,
                avgPnL = if (tradeList.isNotEmpty()) symNet / tradeList.size else 0.0,
                profitFactor = symPF,
                bestTradePnL = bestTrade,
                worstTradePnL = worstTrade
            )
        }.sortedByDescending { it.netPnL }

        val bestSymbol = symbolPerfList.firstOrNull()?.symbol ?: "N/A"
        val worstSymbol = symbolPerfList.lastOrNull()?.symbol ?: "N/A"

        // Day of Week Analysis (Mon-Sun)
        val cal = Calendar.getInstance()
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayGroups = sortedTrades.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon ... 7 = Sat
        }

        val dayOfWeekPerfList = (1..7).map { dayIdx ->
            val dayTrades = dayGroups[dayIdx] ?: emptyList()
            val dayWins = dayTrades.count { (it.realizedPnL - it.fee) > 0.0001 }
            val dayNet = dayTrades.sumOf { it.realizedPnL - it.fee }
            val dayWinRate = if (dayTrades.isNotEmpty()) (dayWins.toDouble() / dayTrades.size) * 100.0 else 0.0

            DayOfWeekPerformance(
                dayName = dayNames[dayIdx - 1],
                dayIndex = dayIdx,
                count = dayTrades.size,
                winCount = dayWins,
                winRate = dayWinRate,
                netPnL = dayNet,
                avgPnL = if (dayTrades.isNotEmpty()) dayNet / dayTrades.size else 0.0
            )
        }

        val bestDay = dayOfWeekPerfList.filter { it.count > 0 }.maxByOrNull { it.netPnL }?.dayName ?: "N/A"
        val worstDay = dayOfWeekPerfList.filter { it.count > 0 }.minByOrNull { it.netPnL }?.dayName ?: "N/A"

        // Time of Day (Hour) Analysis
        val hourGroups = sortedTrades.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.HOUR_OF_DAY) // 0..23
        }

        val hourlyPerfList = (0..23).map { hour ->
            val hTrades = hourGroups[hour] ?: emptyList()
            val hWins = hTrades.count { (it.realizedPnL - it.fee) > 0.0001 }
            val hNet = hTrades.sumOf { it.realizedPnL - it.fee }
            val hWinRate = if (hTrades.isNotEmpty()) (hWins.toDouble() / hTrades.size) * 100.0 else 0.0

            HourlyPerformance(
                hour = hour,
                hourLabel = String.format(Locale.getDefault(), "%02d:00", hour),
                count = hTrades.size,
                winCount = hWins,
                winRate = hWinRate,
                netPnL = hNet
            )
        }

        val bestHourObj = hourlyPerfList.filter { it.count > 0 }.maxByOrNull { it.netPnL }
        val worstHourObj = hourlyPerfList.filter { it.count > 0 }.minByOrNull { it.netPnL }

        val bestHourStr = bestHourObj?.hourLabel ?: "N/A"
        val worstHourStr = worstHourObj?.hourLabel ?: "N/A"

        // Monthly Performance
        val ymFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val ymDisplayFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val monthGroups = sortedTrades.groupBy { ymFormat.format(Date(it.timestamp)) }
        val monthlyPerfList = monthGroups.map { (ym, mTrades) ->
            val mWins = mTrades.count { (it.realizedPnL - it.fee) > 0.0001 }
            val mNet = mTrades.sumOf { it.realizedPnL - it.fee }
            val mGrossProfit = mTrades.filter { (it.realizedPnL - it.fee) > 0 }.sumOf { it.realizedPnL - it.fee }
            val mGrossLoss = mTrades.filter { (it.realizedPnL - it.fee) < 0 }.sumOf { abs(it.realizedPnL - it.fee) }
            val mWinRate = if (mTrades.isNotEmpty()) (mWins.toDouble() / mTrades.size) * 100.0 else 0.0
            val mPF = if (mGrossLoss > 0.0001) mGrossProfit / mGrossLoss else if (mGrossProfit > 0) 99.99 else 0.0
            val sampleDate = Date(mTrades.first().timestamp)

            MonthlyPerformance(
                yearMonth = ym,
                displayLabel = ymDisplayFormat.format(sampleDate),
                count = mTrades.size,
                winCount = mWins,
                winRate = mWinRate,
                netPnL = mNet,
                grossProfit = mGrossProfit,
                grossLoss = mGrossLoss,
                profitFactor = mPF
            )
        }.sortedBy { it.yearMonth }

        // Daily Performance Map for Calendar
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dailyMap = sortedTrades.groupBy { dayFormat.format(Date(it.timestamp)) }.mapValues { (dateStr, dTrades) ->
            val dWins = dTrades.count { (it.realizedPnL - it.fee) > 0.0001 }
            val dLosses = dTrades.count { (it.realizedPnL - it.fee) < -0.0001 }
            val dNet = dTrades.sumOf { it.realizedPnL - it.fee }
            DailyPerformance(
                dateString = dateStr,
                count = dTrades.size,
                winCount = dWins,
                lossCount = dLosses,
                netPnL = dNet,
                trades = dTrades
            )
        }

        // PnL Distribution (Histogram buckets)
        val pnlBucketList = buildPnlDistribution(sortedTrades)

        // Best and Worst Trades
        val bestTrades = sortedTrades.sortedByDescending { it.realizedPnL - it.fee }.take(20)
        val worstTrades = sortedTrades.sortedBy { it.realizedPnL - it.fee }.take(20)

        // Statistical Behavioral Insights
        val behavioralInsights = generateBehavioralInsights(sortedTrades, dayOfWeekPerfList, longPerformance, shortPerformance)

        return TradingReportResult(
            totalTrades = totalTrades,
            winningTrades = winningCount,
            losingTrades = losingCount,
            breakevenTrades = breakevenCount,
            winRate = winRate,
            lossRate = lossRate,
            breakevenRate = breakevenRate,
            netPnL = netPnL,
            grossProfit = grossProfit,
            grossLoss = grossLoss,
            avgTrade = avgTrade,
            avgWin = avgWin,
            avgLoss = avgLoss,
            largestWin = largestWin,
            smallestWin = smallestWin,
            largestLoss = largestLoss,
            smallestLoss = smallestLoss,
            medianWin = medianWin,
            medianLoss = medianLoss,
            profitFactor = profitFactor,
            expectancy = expectancy,
            maxDrawdown = maxDdAmount,
            maxDrawdownPercent = maxDdPercent,
            avgDrawdown = avgDrawdown,
            currentDrawdown = currentDrawdown,
            peakBalance = peakBalance,
            currentBalance = runningBalance,
            startingBalance = startingBalance,
            maxWinStreak = maxWinStreak,
            maxLossStreak = maxLossStreak,
            currentWinStreak = currentWinStreak,
            currentLossStreak = currentLossStreak,
            avgHoldingTimeMillis = avgHoldingTime,
            longestHoldingTimeMillis = longestHoldingTime,
            shortestHoldingTimeMillis = shortestHoldingTime,
            totalFees = totalFees,
            totalVolume = totalVolume,
            bestSymbol = bestSymbol,
            worstSymbol = worstSymbol,
            bestDayOfWeek = bestDay,
            worstDayOfWeek = worstDay,
            bestHourOfDay = bestHourStr,
            worstHourOfDay = worstHourStr,
            longStats = longPerformance,
            shortStats = shortPerformance,
            equityCurve = equityCurve,
            pnlDistribution = pnlBucketList,
            symbolPerformanceList = symbolPerfList,
            dayOfWeekPerformanceList = dayOfWeekPerfList,
            hourlyPerformanceList = hourlyPerfList,
            monthlyPerformanceList = monthlyPerfList,
            dailyPerformanceMap = dailyMap,
            bestTrades = bestTrades,
            worstTrades = worstTrades,
            behavioralInsights = behavioralInsights
        )
    }

    private fun calculateSideStats(side: String, trades: List<TradeEntity>): LongShortPerformance {
        if (trades.isEmpty()) {
            return LongShortPerformance(side, 0, 0, 0, 0.0, 0.0, 0.0, 0.0)
        }
        val wins = trades.count { (it.realizedPnL - it.fee) > 0.0001 }
        val losses = trades.count { (it.realizedPnL - it.fee) < -0.0001 }
        val netPnL = trades.sumOf { it.realizedPnL - it.fee }
        val grossProfit = trades.filter { (it.realizedPnL - it.fee) > 0 }.sumOf { it.realizedPnL - it.fee }
        val grossLoss = trades.filter { (it.realizedPnL - it.fee) < 0 }.sumOf { abs(it.realizedPnL - it.fee) }
        val winRate = (wins.toDouble() / trades.size) * 100.0
        val pf = if (grossLoss > 0.0001) grossProfit / grossLoss else if (grossProfit > 0) 99.99 else 0.0

        return LongShortPerformance(
            side = side,
            count = trades.size,
            winCount = wins,
            lossCount = losses,
            winRate = winRate,
            netPnL = netPnL,
            avgPnL = netPnL / trades.size,
            profitFactor = pf
        )
    }

    private fun buildPnlDistribution(trades: List<TradeEntity>): List<PnlBucket> {
        if (trades.isEmpty()) return emptyList()
        val netPnLs = trades.map { it.realizedPnL - it.fee }
        val minP = netPnLs.minOrNull() ?: 0.0
        val maxP = netPnLs.maxOrNull() ?: 0.0

        if (abs(maxP - minP) < 0.01) {
            return listOf(PnlBucket("All Trades", minP - 10, maxP + 10, trades.size))
        }

        val bucketCount = 6
        val step = (maxP - minP) / bucketCount
        val buckets = mutableListOf<PnlBucket>()

        for (i in 0 until bucketCount) {
            val start = minP + (i * step)
            val end = if (i == bucketCount - 1) maxP + 0.001 else minP + ((i + 1) * step)
            val count = netPnLs.count { p -> p >= start && p < end }
            val label = String.format(Locale.getDefault(), "$%.0f to $%.0f", start, end)
            buckets.add(PnlBucket(label, start, end, count))
        }
        return buckets
    }

    private fun generateBehavioralInsights(
        trades: List<TradeEntity>,
        days: List<DayOfWeekPerformance>,
        longs: LongShortPerformance,
        shorts: LongShortPerformance
    ): List<BehavioralInsight> {
        val insights = mutableListOf<BehavioralInsight>()

        if (trades.size < 5) return insights

        // 1. Post-Loss behavior check
        var afterLossWinCount = 0
        var afterLossTotalCount = 0
        for (i in 1 until trades.size) {
            val prevNet = trades[i - 1].realizedPnL - trades[i - 1].fee
            if (prevNet < -0.0001) {
                afterLossTotalCount++
                if ((trades[i].realizedPnL - trades[i].fee) > 0.0001) {
                    afterLossWinCount++
                }
            }
        }

        if (afterLossTotalCount >= 3) {
            val postLossWinRate = (afterLossWinCount.toDouble() / afterLossTotalCount) * 100.0
            val overallWinRate = (trades.count { (it.realizedPnL - it.fee) > 0 }.toDouble() / trades.size) * 100.0
            val isPos = postLossWinRate >= overallWinRate
            insights.add(
                BehavioralInsight(
                    title = "Post-Loss Performance Pattern",
                    description = "After a losing trade, your historical win rate on the subsequent trade was ${String.format(Locale.getDefault(), "%.1f", postLossWinRate)}% across $afterLossTotalCount sample trades.",
                    metricHighlight = "${String.format(Locale.getDefault(), "%.1f", postLossWinRate)}% Win Rate",
                    isPositive = isPos
                )
            )
        }

        // 2. Day-of-week insight
        val activeDays = days.filter { it.count >= 2 }
        if (activeDays.isNotEmpty()) {
            val maxDay = activeDays.maxByOrNull { it.winRate }
            val minDay = activeDays.minByOrNull { it.winRate }

            if (maxDay != null && minDay != null && maxDay.dayName != minDay.dayName) {
                insights.add(
                    BehavioralInsight(
                        title = "Weekday Consistency Variance",
                        description = "Your highest win rate occurred on ${maxDay.dayName} (${String.format(Locale.getDefault(), "%.1f", maxDay.winRate)}%), while ${minDay.dayName} experienced lower efficiency (${String.format(Locale.getDefault(), "%.1f", minDay.winRate)}%).",
                        metricHighlight = "Best: ${maxDay.dayName}",
                        isPositive = true
                    )
                )
            }
        }

        // 3. Directional asymmetry insight
        if (longs.count >= 3 && shorts.count >= 3) {
            val isLongBetter = longs.winRate > shorts.winRate
            val diff = abs(longs.winRate - shorts.winRate)
            insights.add(
                BehavioralInsight(
                    title = "Directional Bias Breakdown",
                    description = "Long trades achieved ${String.format(Locale.getDefault(), "%.1f", longs.winRate)}% win rate while Short trades achieved ${String.format(Locale.getDefault(), "%.1f", shorts.winRate)}% win rate.",
                    metricHighlight = "${String.format(Locale.getDefault(), "%.1f", diff)}% Delta",
                    isPositive = isLongBetter
                )
            )
        }

        return insights
    }
}
