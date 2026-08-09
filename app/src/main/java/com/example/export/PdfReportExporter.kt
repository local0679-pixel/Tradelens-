package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.analytics.TradingReportResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {

    fun generatePdfReport(context: Context, accountName: String, report: TradingReportResult): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size in points
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0B0E14")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#5A6B82")
            textSize = 12f
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#00F2FE")
        }

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#151B26")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 11f
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.parseColor("#00875A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redPaint = Paint().apply {
            color = Color.parseColor("#DE350B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1f
        }

        var y = 40f

        // Top Accent Bar
        canvas.drawRect(35f, y, 560f, y + 4f, headerBgPaint)
        y += 24f

        // Header Title
        canvas.drawText("TradeLens Performance Analytics Report", 35f, y, titlePaint)
        y += 18f
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Account: $accountName  |  Generated: $nowStr  |  Scope: All Trades", 35f, y, subtitlePaint)
        y += 25f

        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // Executive KPI Table
        canvas.drawText("EXECUTIVE PERFORMANCE SUMMARY", 35f, y, sectionPaint)
        y += 20f

        drawKpiRow(canvas, "Total Closed Trades", "${report.totalTrades}", "Win Rate", String.format(Locale.getDefault(), "%.1f%%", report.winRate), 35f, y, boldTextPaint, greenPaint)
        y += 18f
        val pnlColor = if (report.netPnL >= 0) greenPaint else redPaint
        val pnlStr = String.format(Locale.getDefault(), "$%.2f", report.netPnL)
        drawKpiRow(canvas, "Net Realized P&L", pnlStr, "Profit Factor", String.format(Locale.getDefault(), "%.2f", report.profitFactor), 35f, y, pnlColor, boldTextPaint)
        y += 18f
        drawKpiRow(canvas, "Gross Profit", String.format(Locale.getDefault(), "$%.2f", report.grossProfit), "Gross Loss", String.format(Locale.getDefault(), "$%.2f", report.grossLoss), 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Max Drawdown", String.format(Locale.getDefault(), "$%.2f (%.1f%%)", report.maxDrawdown, report.maxDrawdownPercent), "Expectancy / Trade", String.format(Locale.getDefault(), "$%.2f", report.expectancy), 35f, y, redPaint, boldTextPaint)
        y += 25f

        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // Detailed Statistics
        canvas.drawText("DETAILED TRADING METRICS", 35f, y, sectionPaint)
        y += 20f

        drawKpiRow(canvas, "Winning Trades", "${report.winningTrades}", "Losing Trades", "${report.losingTrades}", 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Average Win", String.format(Locale.getDefault(), "$%.2f", report.avgWin), "Average Loss", String.format(Locale.getDefault(), "$%.2f", report.avgLoss), 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Largest Win", String.format(Locale.getDefault(), "$%.2f", report.largestWin), "Largest Loss", String.format(Locale.getDefault(), "$%.2f", report.largestLoss), 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Max Winning Streak", "${report.maxWinStreak} trades", "Max Losing Streak", "${report.maxLossStreak} trades", 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Best Trading Symbol", report.bestSymbol, "Weakest Trading Symbol", report.worstSymbol, 35f, y, greenPaint, redPaint)
        y += 18f
        drawKpiRow(canvas, "Best Day of Week", report.bestDayOfWeek, "Weakest Day of Week", report.worstDayOfWeek, 35f, y, boldTextPaint, textPaint)
        y += 25f

        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 25f

        // Symbol Breakdown Table
        canvas.drawText("TOP SYMBOL BREAKDOWN", 35f, y, sectionPaint)
        y += 20f

        canvas.drawText("Symbol", 35f, y, boldTextPaint)
        canvas.drawText("Trades", 160f, y, boldTextPaint)
        canvas.drawText("Win Rate", 260f, y, boldTextPaint)
        canvas.drawText("Profit Factor", 360f, y, boldTextPaint)
        canvas.drawText("Net P&L", 470f, y, boldTextPaint)
        y += 15f
        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 15f

        report.symbolPerformanceList.take(6).forEach { sym ->
            val pColor = if (sym.netPnL >= 0) greenPaint else redPaint
            canvas.drawText(sym.symbol, 35f, y, textPaint)
            canvas.drawText("${sym.count}", 160f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", sym.winRate), 260f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", sym.profitFactor), 360f, y, textPaint)
            canvas.drawText(String.format(Locale.getDefault(), "$%.2f", sym.netPnL), 470f, y, pColor)
            y += 18f
        }

        y = 780f
        canvas.drawLine(35f, y, 560f, y, linePaint)
        y += 15f
        canvas.drawText("DISCLAIMER: This report is generated strictly for historical journaling and performance analysis. It does not constitute investment advice.", 35f, y, subtitlePaint)

        document.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()
        val pdfFile = File(reportsDir, "TradeLens_Report_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    fun sharePdfReport(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share TradeLens Performance Report"))
    }

    private fun drawKpiRow(
        canvas: Canvas,
        label1: String, val1: String,
        label2: String, val2: String,
        x: Float, y: Float,
        valPaint1: Paint, valPaint2: Paint
    ) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText(label1, x, y, labelPaint)
        canvas.drawText(val1, x + 160f, y, valPaint1)

        canvas.drawText(label2, x + 280f, y, labelPaint)
        canvas.drawText(val2, x + 420f, y, valPaint2)
    }
}
