package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trades")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: String = "default",
    val orderId: String? = null,
    val positionId: String? = null,
    val timestamp: Long, // Epoch millis
    val symbol: String, // e.g. BTCUSDT, EURUSD, AAPL
    val side: String, // "LONG", "SHORT", "BUY", "SELL"
    val entryPrice: Double = 0.0,
    val exitPrice: Double = 0.0,
    val quantity: Double = 0.0,
    val volume: Double = 0.0,
    val realizedPnL: Double,
    val fee: Double = 0.0,
    val leverage: Double = 1.0,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val holdingDurationMillis: Long = 0L,
    val source: String = "CSV", // "CSV", "Excel", "Manual", "Sample"
    val notes: String? = null
)
