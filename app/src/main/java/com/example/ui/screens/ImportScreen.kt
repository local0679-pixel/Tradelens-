package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TradeEntity
import com.example.importer.ColumnMapping
import com.example.importer.CsvImporter
import com.example.importer.ValidationReport
import com.example.ui.theme.LossRed
import com.example.ui.theme.TerminalGold
import com.example.ui.theme.WinGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    accounts: List<AccountEntity>,
    validationReport: ValidationReport?,
    onParseCsv: (csvText: String, mapping: ColumnMapping, accountId: String) -> Unit,
    onConfirmImport: () -> Unit,
    onAddManualTrade: (TradeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.accountId ?: "main_futures") }
    var rawCsvText by remember { mutableStateOf("") }
    var activeStep by remember { mutableIntStateOf(1) } // 1: Paste/Select, 2: Validate, 3: Done

    var showManualTradeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("import_screen_root")
    ) {
        Text(
            text = "Import Trading History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Turn your raw CSV execution history into clean, actionable analytics.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-Step Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepChip(stepNumber = 1, title = "Paste CSV Data", isActive = activeStep == 1, isDone = activeStep > 1)
            StepChip(stepNumber = 2, title = "Data Integrity", isActive = activeStep == 2, isDone = activeStep > 2)
            StepChip(stepNumber = 3, title = "Complete", isActive = activeStep == 3, isDone = activeStep > 3)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Target Account Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Target Account", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = (selectedAccountId == acc.accountId),
                            onClick = { selectedAccountId = acc.accountId },
                            label = { Text(acc.name) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Upload or Paste CSV
        if (activeStep == 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste CSV Trading Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Supports Binance, Bybit, Interactive Brokers, MetaTrader, and custom CSV exports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rawCsvText,
                        onValueChange = { rawCsvText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("import_csv_input_field"),
                        placeholder = { Text("Date,Symbol,Side,Realized P&L,Quantity\n2026-08-01 10:00:00,BTCUSDT,BUY,120.50,0.5\n2026-08-02 14:30:00,ETHUSDT,SELL,-45.00,2.0") },
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                rawCsvText = """
                                    Date,Symbol,Side,Realized P&L,Quantity,Entry Price,Exit Price,Fee
                                    2026-08-05 09:15:00,BTCUSDT,BUY,240.50,1.0,64000.0,64240.5,2.5
                                    2026-08-05 11:30:00,ETHUSDT,SELL,-80.00,3.0,3450.0,3476.6,1.8
                                    2026-08-06 14:00:00,SOLUSDT,BUY,115.20,25.0,140.0,144.6,1.2
                                    2026-08-07 16:45:00,BTCUSDT,SELL,310.00,0.8,65000.0,64612.5,2.0
                                """.trimIndent()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Load Sample CSV")
                        }

                        Button(
                            onClick = {
                                if (rawCsvText.isNotBlank()) {
                                    val headers = rawCsvText.lines().firstOrNull()?.split(",") ?: emptyList()
                                    val mapping = CsvImporter.detectColumnIndices(headers)
                                    onParseCsv(rawCsvText, mapping, selectedAccountId)
                                    activeStep = 2
                                }
                            },
                            enabled = rawCsvText.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_next_step_1_button")
                        ) {
                            Text("Validate CSV")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option to add single trade manually
            OutlinedButton(
                onClick = { showManualTradeDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("import_manual_trade_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Single Trade Manually")
            }
        }

        // Step 2: Validation & Confirmation
        if (activeStep == 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Integrity Check",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (validationReport == null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ValidationCountBadge("Total Rows", "${validationReport.totalRowsParsed}", MaterialTheme.colorScheme.primary)
                            ValidationCountBadge("Valid Trades", "${validationReport.validTradesCount}", WinGreen)
                            ValidationCountBadge("Duplicates", "${validationReport.duplicateCount}", TerminalGold)
                            ValidationCountBadge("Errors", "${validationReport.invalidRowsCount}", LossRed)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (validationReport.errorMessages.isNotEmpty()) {
                            Text("Validation Issues:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = LossRed)
                            validationReport.errorMessages.take(3).forEach { err ->
                                Text("• $err", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { activeStep = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = {
                                    onConfirmImport()
                                    activeStep = 3
                                },
                                enabled = validationReport.validTradesCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_confirm_button")
                            ) {
                                Text("Import ${validationReport.validTradesCount} Trades")
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Success State
        if (activeStep == 3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = WinGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Import Completed Successfully!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WinGreen
                    )
                    Text(
                        text = "Your trades have been processed and added to your analytics database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            activeStep = 1
                            rawCsvText = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Another File")
                    }
                }
            }
        }
    }

    // Manual Trade Dialog
    if (showManualTradeDialog) {
        ManualTradeDialog(
            accountId = selectedAccountId,
            onDismiss = { showManualTradeDialog = false },
            onAddTrade = { trade ->
                onAddManualTrade(trade)
                showManualTradeDialog = false
            }
        )
    }
}

@Composable
private fun StepChip(stepNumber: Int, title: String, isActive: Boolean, isDone: Boolean) {
    val bgColor = when {
        isDone -> WinGreen
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text("$stepNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ValidationCountBadge(label: String, countStr: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(countStr, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ManualTradeDialog(
    accountId: String,
    onDismiss: () -> Unit,
    onAddTrade: (TradeEntity) -> Unit
) {
    var symbol by remember { mutableStateOf("BTCUSDT") }
    var side by remember { mutableStateOf("BUY") }
    var pnl by remember { mutableStateOf("150.00") }
    var qty by remember { mutableStateOf("1.0") }
    var entry by remember { mutableStateOf("65000.0") }
    var exit by remember { mutableStateOf("65150.0") }
    var fee by remember { mutableStateOf("2.5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Single Trade", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = symbol, onValueChange = { symbol = it }, label = { Text("Symbol (e.g. BTCUSDT)") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = side == "BUY", onClick = { side = "BUY" }, label = { Text("BUY / LONG") })
                    FilterChip(selected = side == "SELL", onClick = { side = "SELL" }, label = { Text("SELL / SHORT") })
                }
                OutlinedTextField(value = pnl, onValueChange = { pnl = it }, label = { Text("Realized P&L ($)") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                OutlinedTextField(value = entry, onValueChange = { entry = it }, label = { Text("Entry Price ($)") })
                OutlinedTextField(value = exit, onValueChange = { exit = it }, label = { Text("Exit Price ($)") })
                OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("Fee ($)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTrade = TradeEntity(
                        accountId = accountId,
                        timestamp = System.currentTimeMillis(),
                        symbol = symbol.uppercase().trim(),
                        side = side,
                        quantity = qty.toDoubleOrNull() ?: 1.0,
                        entryPrice = entry.toDoubleOrNull() ?: 0.0,
                        exitPrice = exit.toDoubleOrNull() ?: 0.0,
                        realizedPnL = pnl.toDoubleOrNull() ?: 0.0,
                        fee = fee.toDoubleOrNull() ?: 0.0,
                        source = "MANUAL"
                    )
                    onAddTrade(newTrade)
                }
            ) {
                Text("Save Trade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
