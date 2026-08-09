package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.FinancialFormatter
import com.example.data.model.TradeEntity
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedContainer
import com.example.ui.theme.WinGreen
import com.example.ui.theme.WinGreenContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeRow(
    trade: TradeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val netPnL = trade.realizedPnL - trade.fee
    val isWin = netPnL >= 0
    val pnlColor = if (isWin) WinGreen else LossRed
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    val sideIsLong = trade.side.equals("LONG", ignoreCase = true) || trade.side.equals("BUY", ignoreCase = true)
    val sideBg = if (sideIsLong) WinGreenContainer else LossRedContainer
    val sideTextColor = if (sideIsLong) WinGreen else LossRed

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("trade_row_${trade.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Side Badge (LONG / SHORT)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(sideBg)
                .border(0.5.dp, sideTextColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (sideIsLong) "LONG" else "SHORT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = sideTextColor,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Symbol & Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trade.symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
            Text(
                text = dateFormat.format(Date(trade.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // Prices & Size
        Column(horizontalAlignment = Alignment.End) {
            if (trade.entryPrice > 0) {
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f → $%.2f", trade.entryPrice, trade.exitPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Text(
                text = FinancialFormatter.formatCurrency(netPnL, showPlusSign = true),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = pnlColor,
                fontSize = 15.sp
            )
        }
    }
}

