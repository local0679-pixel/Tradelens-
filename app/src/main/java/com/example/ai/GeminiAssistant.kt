package com.example.ai

import com.example.BuildConfig
import com.example.analytics.TradingReportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object GeminiAssistant {

    suspend fun generatePerformanceSummary(report: TradingReportResult): String {
        return askQuestion("Provide a concise 3-bullet executive summary of my trade history.", report)
    }

    suspend fun askQuestion(question: String, report: TradingReportResult): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI Assistant key not configured. Local statistical engine analyzed ${report.totalTrades} trades locally with ${String.format(Locale.getDefault(), "%.1f%%", report.winRate)} win rate and $${String.format(Locale.getDefault(), "%.2f", report.netPnL)} Net P&L."
        }

        val prompt = """
            You are a senior quantitative risk manager and trading performance auditor.
            Answer the user's question based strictly on their historical trade statistics below.
            Do NOT provide financial advice, trading signals, or profit guarantees. Do NOT tell the user what trade to take.
            Keep responses factual, concise, and helpful.

            USER QUESTION:
            "$question"

            TRADE HISTORY METRICS:
            - Total Closed Trades: ${report.totalTrades}
            - Win Rate: ${String.format(Locale.getDefault(), "%.1f%%", report.winRate)}
            - Net Realized P&L: $${String.format(Locale.getDefault(), "%.2f", report.netPnL)}
            - Profit Factor: ${String.format(Locale.getDefault(), "%.2f", report.profitFactor)}
            - Expectancy / Trade: $${String.format(Locale.getDefault(), "%.2f", report.expectancy)}
            - Max Drawdown: $${String.format(Locale.getDefault(), "%.2f", report.maxDrawdown)} (${String.format(Locale.getDefault(), "%.1f%%", report.maxDrawdownPercent)})
            - Best Symbol: ${report.bestSymbol} | Worst Symbol: ${report.worstSymbol}
            - Best Day of Week: ${report.bestDayOfWeek} | Worst Day: ${report.worstDayOfWeek}
            - Long Win Rate: ${String.format(Locale.getDefault(), "%.1f%%", report.longStats.winRate)} | Short Win Rate: ${String.format(Locale.getDefault(), "%.1f%%", report.shortStats.winRate)}
            - Max Win Streak: ${report.maxWinStreak} | Max Loss Streak: ${report.maxLossStreak}
        """.trimIndent()

        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
            }

            connection.outputStream.use { os ->
                os.write(requestJson.toString().toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "Response generated.")
                    }
                }
            }
            "AI Assistant response could not be parsed."
        } catch (e: Exception) {
            "AI Assistant unavailable: ${e.message}"
        }
    }
}
