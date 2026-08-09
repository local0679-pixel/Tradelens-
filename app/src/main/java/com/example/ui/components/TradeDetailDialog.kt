package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TradeEntity
import com.example.ui.theme.LossRed
import com.example.ui.theme.WinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailDialog(
    trade: TradeEntity,
    onDismiss: () -> Unit,
    onDeleteTrade: (TradeEntity) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trade.timestamp))
    val isWin = trade.realizedPnL >= 0
    val pnlColor = if (isWin) WinGreen else LossRed
    val pnlText = String.format(Locale.getDefault(), "%s$%.2f", if (isWin) "+" else "", trade.realizedPnL)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(trade.symbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trade_detail_dialog_content")
            ) {
                // P&L Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(pnlColor.copy(alpha = 0.15f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (trade.side == "BUY") "LONG / BUY" else "SHORT / SELL",
                            fontWeight = FontWeight.Bold,
                            color = if (trade.side == "BUY") WinGreen else LossRed,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pnlText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = pnlColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Items Grid
                DetailRow(label = "Quantity", value = "${trade.quantity}")
                DetailRow(label = "Entry Price", value = "$${trade.entryPrice}")
                DetailRow(label = "Exit Price", value = "$${trade.exitPrice}")
                DetailRow(label = "Commissions & Fees", value = "$${trade.fee}")
                DetailRow(label = "Source", value = trade.source)
                trade.orderId?.let { DetailRow(label = "Trade ID", value = it) }
                trade.notes?.let { DetailRow(label = "Notes", value = it) }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onDeleteTrade(trade)
                    onDismiss()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LossRed),
                modifier = Modifier.testTag("delete_trade_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Trade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
