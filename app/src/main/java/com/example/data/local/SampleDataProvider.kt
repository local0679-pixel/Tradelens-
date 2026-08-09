package com.example.data.local

import com.example.data.model.AccountEntity
import com.example.data.model.TradeEntity
import java.util.Calendar
import kotlin.random.Random

object SampleDataProvider {

    val defaultAccounts = listOf(
        AccountEntity("main_futures", "Main Futures (Crypto)", "USDT", 10000.0, true),
        AccountEntity("prop_forex", "Prop Firm (Forex)", "USD", 50000.0, false),
        AccountEntity("spot_stocks", "Swing Stocks (US)", "USD", 25000.0, false)
    )

    fun generateSampleTrades(): List<TradeEntity> {
        val trades = mutableListOf<TradeEntity>()
        val random = Random(42) // Fixed seed for reproducible realistic trades
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L
        val oneHourMillis = 3_600_000L

        // Generate 120 historical trades over the last 90 days
        var currentOffset = 90L * oneDayMillis

        val cryptoSymbols = listOf(
            Triple("BTCUSDT", 62000.0, 1.5),
            Triple("ETHUSDT", 3400.0, 3.0),
            Triple("SOLUSDT", 145.0, 5.0),
            Triple("EURUSD", 1.0850, 0.5),
            Triple("XAUUSD", 2380.0, 1.0),
            Triple("AAPL", 220.0, 10.0),
            Triple("NVDA", 125.0, 15.0)
        )

        var orderCounter = 1000

        while (currentOffset > 0) {
            val numTradesToday = random.nextInt(0, 4)
            for (i in 0 until numTradesToday) {
                orderCounter++
                val tradeTime = now - currentOffset + (random.nextInt(1, 22) * oneHourMillis) + (random.nextInt(0, 59) * 60_000L)
                val symbolData = cryptoSymbols[random.nextInt(cryptoSymbols.size)]
                val symbol = symbolData.first
                val basePrice = symbolData.second
                val volatilityPct = symbolData.third

                val isLong = random.nextBoolean()
                val side = if (isLong) "LONG" else "SHORT"
                val accountId = when {
                    symbol.contains("USDT") -> "main_futures"
                    symbol.contains("USD") -> "prop_forex"
                    else -> "spot_stocks"
                }

                val priceDeltaPct = (random.nextDouble(-0.03, 0.04) * volatilityPct)
                val entryPrice = basePrice * (1.0 + (random.nextDouble(-0.02, 0.02)))
                val exitPrice = if (isLong) entryPrice * (1.0 + priceDeltaPct) else entryPrice * (1.0 - priceDeltaPct)

                val quantity = when {
                    symbol == "BTCUSDT" -> random.nextDouble(0.1, 1.5)
                    symbol == "ETHUSDT" -> random.nextDouble(1.0, 10.0)
                    symbol == "SOLUSDT" -> random.nextDouble(20.0, 150.0)
                    symbol.contains("USD") -> random.nextDouble(10000.0, 100000.0)
                    else -> random.nextDouble(10.0, 200.0)
                }

                val leverage = if (symbol.contains("USDT")) random.nextDouble(2.0, 20.0).toInt().toDouble() else 1.0
                val volume = entryPrice * quantity

                // Calculate PnL (62% win rate bias to simulate a disciplined trader)
                val rawPnL = if (isLong) (exitPrice - entryPrice) * quantity else (entryPrice - exitPrice) * quantity
                // Add slight positive bias to make equity curve realistic & interesting
                val pnlAdjusted = rawPnL + (volume * 0.001)

                val fee = volume * 0.0005
                val holdingHours = random.nextDouble(0.2, 18.0)
                val holdingDuration = (holdingHours * oneHourMillis).toLong()

                val stopLoss = if (isLong) entryPrice * 0.985 else entryPrice * 1.015
                val takeProfit = if (isLong) entryPrice * 1.03 else entryPrice * 0.97

                trades.add(
                    TradeEntity(
                        accountId = accountId,
                        orderId = "ORD-$orderCounter",
                        positionId = "POS-$orderCounter",
                        timestamp = tradeTime,
                        symbol = symbol,
                        side = side,
                        entryPrice = (entryPrice * 10000).toLong() / 10000.0,
                        exitPrice = (exitPrice * 10000).toLong() / 10000.0,
                        quantity = (quantity * 100).toLong() / 100.0,
                        volume = (volume * 100).toLong() / 100.0,
                        realizedPnL = (pnlAdjusted * 100).toLong() / 100.0,
                        fee = (fee * 100).toLong() / 100.0,
                        leverage = leverage,
                        stopLoss = (stopLoss * 10000).toLong() / 10000.0,
                        takeProfit = (takeProfit * 10000).toLong() / 10000.0,
                        holdingDurationMillis = holdingDuration,
                        source = "Sample",
                        notes = if (random.nextDouble() < 0.25) "Target hit on key support/resistance zone" else null
                    )
                )
            }
            currentOffset -= oneDayMillis
        }

        return trades.sortedBy { it.timestamp }
    }
}
