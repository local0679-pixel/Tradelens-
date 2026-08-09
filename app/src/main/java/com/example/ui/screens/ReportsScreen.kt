package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analytics.TradingReportResult
import com.example.export.PdfReportExporter
import com.example.ui.theme.WinGreen
import java.io.File

@Composable
fun ReportsScreen(
    report: TradingReportResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfGeneratedMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("reports_screen_root")
    ) {
        Text(
            text = "Performance Reports",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Export publication-grade PDF summaries for accounting or personal review.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Executive Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Executive Trading Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text("PDF Available", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Includes Net P&L, Win Rate, Profit Factor, Equity Curve points, Drawdown stats, and top symbol performances.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isGeneratingPdf = true
                        val file = PdfReportExporter.generatePdfReport(context, "All Accounts", report)
                        isGeneratingPdf = false

                        pdfGeneratedMessage = "PDF Generated: ${file.name}"
                        sharePdfFile(context, file)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_pdf_report_button"),
                    enabled = !isGeneratingPdf
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating PDF...")
                    } else {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate & Share PDF Report")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pre-configured Report Templates
        Text(
            text = "Report Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        ReportTemplateCard(
            title = "Monthly P&L Audit",
            subtitle = "Breakdown of execution consistency month-by-month.",
            icon = Icons.Default.CalendarMonth,
            onClick = {
                val file = PdfReportExporter.generatePdfReport(context, "All Accounts", report)
                sharePdfFile(context, file)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ReportTemplateCard(
            title = "Asset & Symbol Breakdown",
            subtitle = "Detailed analysis of Win Rates and Net P&L per ticker symbol.",
            icon = Icons.Default.PieChart,
            onClick = {
                val file = PdfReportExporter.generatePdfReport(context, "All Accounts", report)
                sharePdfFile(context, file)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ReportTemplateCard(
            title = "Risk & Drawdown Report",
            subtitle = "Audit maximum portfolio drops and recovery durations.",
            icon = Icons.Default.Assessment,
            onClick = {
                val file = PdfReportExporter.generatePdfReport(context, "All Accounts", report)
                sharePdfFile(context, file)
            }
        )

        pdfGeneratedMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = WinGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReportTemplateCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share TradeLens Report"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
