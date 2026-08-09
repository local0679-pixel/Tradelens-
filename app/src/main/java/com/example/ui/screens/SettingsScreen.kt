package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity
import com.example.ui.theme.LossRed
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    accounts: List<AccountEntity>,
    onThemeSelected: (ThemeMode) -> Unit,
    onAddAccount: (name: String, currency: String, initialBalance: Double) -> Unit,
    onResetToSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showClearDataConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customize appearance, accounts, data persistence, and privacy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Appearance
        SettingsSectionCard(title = "Appearance", icon = Icons.Default.Palette) {
            Text("Select App Theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = (themeMode == ThemeMode.DARK),
                    onClick = { onThemeSelected(ThemeMode.DARK) },
                    label = { Text("Dark Theme") },
                    modifier = Modifier.testTag("theme_dark_chip")
                )
                FilterChip(
                    selected = (themeMode == ThemeMode.LIGHT),
                    onClick = { onThemeSelected(ThemeMode.LIGHT) },
                    label = { Text("Light Theme") },
                    modifier = Modifier.testTag("theme_light_chip")
                )
                FilterChip(
                    selected = (themeMode == ThemeMode.SYSTEM),
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                    label = { Text("System Default") },
                    modifier = Modifier.testTag("theme_system_chip")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Accounts Management
        SettingsSectionCard(title = "Trading Accounts", icon = Icons.Default.Wallet) {
            accounts.forEach { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(acc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${acc.currency} • Starting Bal: $${acc.initialBalance}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (acc.isDefault) {
                        Badge { Text("Default") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showAddAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_account_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Trading Account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Data & Storage
        SettingsSectionCard(title = "Data Management", icon = Icons.Default.DeleteSweep) {
            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_sample_data_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore Demo Sample Data")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showClearDataConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clear_all_data_button")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onError)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear All Local Trade Records", color = MaterialTheme.colorScheme.onError)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Privacy & Security
        SettingsSectionCard(title = "Your Privacy", icon = Icons.Default.PrivacyTip) {
            Text(
                text = "100% Local-First Engine",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.colorScheme.primary.let { MaterialTheme.typography.bodyMedium }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your trading executions are parsed and calculated strictly on your mobile device. TradeLens never requests API secret keys, passwords, or seed phrases.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 5: About
        SettingsSectionCard(title = "About TradeLens", icon = Icons.Default.Info) {
            Text("TradeLens Mobile v2.4.0", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text("Tagline: Know Your Trading.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Engineered for modern multi-asset traders.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        var accName by remember { mutableStateOf("") }
        var accCurr by remember { mutableStateOf("USD") }
        var accBal by remember { mutableStateOf("10000") }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add New Account", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = accName, onValueChange = { accName = it }, label = { Text("Account Name") })
                    OutlinedTextField(value = accCurr, onValueChange = { accCurr = it }, label = { Text("Currency") })
                    OutlinedTextField(value = accBal, onValueChange = { accBal = it }, label = { Text("Initial Balance") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (accName.isNotBlank()) {
                            onAddAccount(accName, accCurr, accBal.toDoubleOrNull() ?: 10000.0)
                            showAddAccountDialog = false
                        }
                    }
                ) {
                    Text("Add Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Demo Sample Trades?", fontWeight = FontWeight.Bold) },
            text = { Text("This will replace current trade history with demo records for testing.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToSampleData()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Restore Demo Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data Confirm Dialog
    if (showClearDataConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirmDialog = false },
            title = { Text("Clear All Trade Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all trades from local storage. You can test the empty dashboard state or import new CSV files.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
