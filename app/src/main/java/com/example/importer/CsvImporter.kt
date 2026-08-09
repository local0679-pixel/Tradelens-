package com.example.importer

import com.example.data.model.TradeEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class ColumnMapping(
    val dateColIndex: Int = -1,
    val symbolColIndex: Int = -1,
    val sideColIndex: Int = -1,
    val pnlColIndex: Int = -1,
    val entryPriceColIndex: Int = -1,
    val exitPriceColIndex: Int = -1,
    val quantityColIndex: Int = -1,
    val feeColIndex: Int = -1,
    val leverageColIndex: Int = -1,
    val orderIdColIndex: Int = -1
)

data class ValidationReport(
    val totalRowsParsed: Int,
    val validTradesCount: Int,
    val duplicateCount: Int,
    val invalidRowsCount: Int,
    val errorMessages: List<String>,
    val parsedTrades: List<TradeEntity>
)

object CsvImporter {

    fun detectColumnIndices(headers: List<String>): ColumnMapping {
        var dateIdx = -1
        var symbolIdx = -1
        var sideIdx = -1
        var pnlIdx = -1
        var entryIdx = -1
        var exitIdx = -1
        var qtyIdx = -1
        var feeIdx = -1
        var levIdx = -1
        var orderIdIdx = -1

        headers.forEachIndexed { index, header ->
            val h = header.lowercase(Locale.getDefault()).trim().replace("_", "").replace(" ", "")

            when {
                dateIdx == -1 && (h.contains("date") || h.contains("time") || h.contains("timestamp") || h.contains("opened")) -> dateIdx = index
                symbolIdx == -1 && (h.contains("symbol") || h.contains("pair") || h.contains("ticker") || h.contains("instrument") || h.contains("asset")) -> symbolIdx = index
                sideIdx == -1 && (h.contains("side") || h.contains("direction") || h.contains("type") || h.contains("action")) -> sideIdx = index
                pnlIdx == -1 && (h.contains("realizedpnl") || h.contains("pnl") || h.contains("profit") || h.contains("realizedprofit") || h.contains("netpnl")) -> pnlIdx = index
                entryIdx == -1 && (h.contains("entry") || h.contains("openprice") || h.contains("buyprice")) -> entryIdx = index
                exitIdx == -1 && (h.contains("exit") || h.contains("closeprice") || h.contains("sellprice")) -> exitIdx = index
                qtyIdx == -1 && (h.contains("qty") || h.contains("quantity") || h.contains("size") || h.contains("amount") || h.contains("volume")) -> qtyIdx = index
                feeIdx == -1 && (h.contains("fee") || h.contains("commission")) -> feeIdx = index
                levIdx == -1 && (h.contains("leverage") || h.contains("margin")) -> levIdx = index
                orderIdIdx == -1 && (h.contains("orderid") || h.contains("tradeid") || h.contains("id")) -> orderIdIdx = index
            }
        }

        return ColumnMapping(
            dateColIndex = dateIdx,
            symbolColIndex = symbolIdx,
            sideColIndex = sideIdx,
            pnlColIndex = pnlIdx,
            entryPriceColIndex = entryIdx,
            exitPriceColIndex = exitIdx,
            quantityColIndex = qtyIdx,
            feeColIndex = feeIdx,
            leverageColIndex = levIdx,
            orderIdColIndex = orderIdIdx
        )
    }

    fun parseCsvContent(
        csvText: String,
        mapping: ColumnMapping,
        targetAccountId: String = "main_futures",
        existingTrades: List<TradeEntity> = emptyList()
    ): ValidationReport {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return ValidationReport(0, 0, 0, 0, listOf("CSV file is empty."), emptyList())
        }

        val rows = lines.drop(1).map { line ->
            parseCsvLine(line)
        }

        val parsedTrades = mutableListOf<TradeEntity>()
        val errorMessages = mutableListOf<String>()
        var invalidCount = 0
        var duplicateCount = 0

        val dateParsers = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        )

        rows.forEachIndexed { rowIndex, row ->
            val rowNum = rowIndex + 2
            if (row.size <= maxOf(mapping.dateColIndex, mapping.symbolColIndex, mapping.pnlColIndex)) {
                invalidCount++
                errorMessages.add("Row $rowNum: Insufficient columns.")
                return@forEachIndexed
            }

            // Extract Date
            val dateStr = if (mapping.dateColIndex in row.indices) row[mapping.dateColIndex].trim() else ""
            var timestamp: Long? = dateStr.toLongOrNull()
            if (timestamp == null && dateStr.isNotEmpty()) {
                for (fmt in dateParsers) {
                    try {
                        val parsed = fmt.parse(dateStr)
                        if (parsed != null) {
                            timestamp = parsed.time
                            break
                        }
                    } catch (_: Exception) {}
                }
            }
            if (timestamp == null || timestamp <= 0) {
                timestamp = System.currentTimeMillis() - (rowIndex * 3600000L)
            }

            // Extract Symbol
            val symbolRaw = if (mapping.symbolColIndex in row.indices) row[mapping.symbolColIndex].trim() else "UNKNOWN"
            val symbol = if (symbolRaw.isEmpty()) "UNKNOWN" else symbolRaw.uppercase(Locale.getDefault())

            // Extract Side
            val sideRaw = if (mapping.sideColIndex in row.indices) row[mapping.sideColIndex].trim().uppercase(Locale.getDefault()) else "LONG"
            val side = when {
                sideRaw.contains("SHORT") || sideRaw.contains("SELL") -> "SHORT"
                else -> "LONG"
            }

            // Extract PnL
            val pnlStr = if (mapping.pnlColIndex in row.indices) row[mapping.pnlColIndex].replace("$", "").replace(",", "").trim() else "0"
            val pnl = pnlStr.toDoubleOrNull()
            if (pnl == null) {
                invalidCount++
                errorMessages.add("Row $rowNum: Invalid PnL value ($pnlStr).")
                return@forEachIndexed
            }

            // Extract Entry / Exit / Qty / Fee
            val entryPrice = if (mapping.entryPriceColIndex in row.indices) row[mapping.entryPriceColIndex].replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0 else 0.0
            val exitPrice = if (mapping.exitPriceColIndex in row.indices) row[mapping.exitPriceColIndex].replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0 else 0.0
            val qty = if (mapping.quantityColIndex in row.indices) row[mapping.quantityColIndex].replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0 else 0.0
            val fee = if (mapping.feeColIndex in row.indices) row[mapping.feeColIndex].replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0 else 0.0
            val leverage = if (mapping.leverageColIndex in row.indices) row[mapping.leverageColIndex].toDoubleOrNull() ?: 1.0 else 1.0
            val orderId = if (mapping.orderIdColIndex in row.indices) row[mapping.orderIdColIndex].trim() else "IMP-${System.currentTimeMillis()}-$rowIndex"

            val volume = if (qty > 0 && entryPrice > 0) qty * entryPrice else abs(pnl) * 10

            // Check Duplicate against existing or newly parsed
            val isDuplicate = existingTrades.any { ex ->
                (ex.orderId != null && orderId.isNotEmpty() && ex.orderId == orderId) ||
                (abs(ex.timestamp - timestamp) < 1000 && ex.symbol == symbol && abs(ex.realizedPnL - pnl) < 0.001)
            } || parsedTrades.any { p ->
                (p.orderId != null && orderId.isNotEmpty() && p.orderId == orderId) ||
                (abs(p.timestamp - timestamp) < 1000 && p.symbol == symbol && abs(p.realizedPnL - pnl) < 0.001)
            }

            if (isDuplicate) {
                duplicateCount++
            } else {
                parsedTrades.add(
                    TradeEntity(
                        accountId = targetAccountId,
                        orderId = orderId,
                        timestamp = timestamp,
                        symbol = symbol,
                        side = side,
                        entryPrice = entryPrice,
                        exitPrice = exitPrice,
                        quantity = qty,
                        volume = volume,
                        realizedPnL = pnl,
                        fee = fee,
                        leverage = leverage,
                        holdingDurationMillis = 3600000L,
                        source = "CSV"
                    )
                )
            }
        }

        return ValidationReport(
            totalRowsParsed = rows.size,
            validTradesCount = parsedTrades.size,
            duplicateCount = duplicateCount,
            invalidRowsCount = invalidCount,
            errorMessages = errorMessages,
            parsedTrades = parsedTrades
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(ch)
                    } else {
                        result.add(sb.toString().trim())
                        sb.clear()
                    }
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
