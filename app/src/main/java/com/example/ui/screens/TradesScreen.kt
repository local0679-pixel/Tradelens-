package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.TradeEntity
import com.example.ui.components.TradeRow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
    trades: List<TradeEntity>,
    selectedAccountId: String,
    searchQuery: String,
    sideFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onSideFilterChange: (String) -> Unit,
    onAddTrade: (TradeEntity) -> Unit,
    onDeleteTrade: (TradeEntity) -> Unit,
    onTradeSelected: (TradeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTradeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("trades_screen_root"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTradeDialog = true },
                containerColor = ElectricCyan,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_manual_trade_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trade")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text("Trade History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trade_search_input"),
                placeholder = { Text("Search symbol, order ID or notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Side Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All Trades", "LONG" to "Longs Only", "SHORT" to "Shorts Only").forEach { (sideKey, label) ->
                    val selected = sideFilter == sideKey
                    FilterChip(
                        selected = selected,
                        onClick = { onSideFilterChange(sideKey) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("side_filter_chip_$sideKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trades List
            if (trades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trades match search or filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trades, key = { it.id }) { trade ->
                        TradeRow(
                            trade = trade,
                            onClick = { onTradeSelected(trade) }
                        )
                    }
                }
            }
        }
    }

    // Add Trade Dialog
    if (showAddTradeDialog) {
        AddManualTradeDialog(
            selectedAccountId = selectedAccountId,
            onDismiss = { showAddTradeDialog = false },
            onSave = { newTrade ->
                onAddTrade(newTrade)
                showAddTradeDialog = false
            }
        )
    }
}

@Composable
fun AddManualTradeDialog(
    selectedAccountId: String,
    onDismiss: () -> Unit,
    onSave: (TradeEntity) -> Unit
) {
    var symbol by remember { mutableStateOf("BTCUSDT") }
    var side by remember { mutableStateOf("LONG") }
    var entryPrice by remember { mutableStateOf("62000") }
    var exitPrice by remember { mutableStateOf("63500") }
    var quantity by remember { mutableStateOf("0.5") }
    var pnl by remember { mutableStateOf("750") }
    var fee by remember { mutableStateOf("3.5") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Trade Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Symbol / Pair") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_trade_symbol_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { side = "LONG" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (side == "LONG") WinGreen else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("LONG", color = if (side == "LONG") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) }
                    Button(
                        onClick = { side = "SHORT" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (side == "SHORT") LossRed else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("SHORT", color = if (side == "SHORT") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) }
                }
                OutlinedTextField(
                    value = entryPrice,
                    onValueChange = { entryPrice = it },
                    label = { Text("Entry Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = exitPrice,
                    onValueChange = { exitPrice = it },
                    label = { Text("Exit Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pnl,
                    onValueChange = { pnl = it },
                    label = { Text("Realized P&L ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_trade_pnl_input")
                )
                OutlinedTextField(
                    value = fee,
                    onValueChange = { fee = it },
                    label = { Text("Fee ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entryVal = entryPrice.toDoubleOrNull() ?: 0.0
                    val exitVal = exitPrice.toDoubleOrNull() ?: 0.0
                    val qtyVal = quantity.toDoubleOrNull() ?: 1.0
                    val pnlVal = pnl.toDoubleOrNull() ?: 0.0
                    val feeVal = fee.toDoubleOrNull() ?: 0.0

                    val newTrade = TradeEntity(
                        accountId = if (selectedAccountId == "ALL") "main_futures" else selectedAccountId,
                        orderId = "MANUAL-${System.currentTimeMillis()}",
                        timestamp = System.currentTimeMillis(),
                        symbol = symbol.trim().uppercase(),
                        side = side,
                        entryPrice = entryVal,
                        exitPrice = exitVal,
                        quantity = qtyVal,
                        volume = if (entryVal > 0) entryVal * qtyVal else abs(pnlVal) * 10,
                        realizedPnL = pnlVal,
                        fee = feeVal,
                        source = "Manual",
                        notes = notes.ifBlank { null }
                    )
                    onSave(newTrade)
                },
                modifier = Modifier.testTag("save_manual_trade_button")
            ) {
                Text("Save Trade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
