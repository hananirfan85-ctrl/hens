package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DepositProofEntity
import com.example.data.model.LivestockCategory
import com.example.data.model.TransactionStatus
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.FarmUiState

@Composable
fun AdminScreen(
    state: FarmUiState,
    onApproveDeposit: (proofId: String) -> Unit,
    onTriggerDailyYield: () -> Unit,
    onAddNewPackage: (
        title: String,
        category: LivestockCategory,
        unitPrice: String,
        durationDays: String,
        dailyYieldPercent: String,
        totalRoiPercent: String,
        totalUnits: String,
        farmLocation: String,
        description: String,
        feedType: String
    ) -> Unit
) {
    var showAddPackageDialog by remember { mutableStateOf(false) }

    val depositProofs = state.depositProofs
    val pendingDeposits = depositProofs.filter { it.status == TransactionStatus.PENDING }
    val auditLogs = state.auditLogs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Admin Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_header_banner")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FARM MANAGER PORTAL",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Surface(color = ForestPrimary, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "SYSTEM ADMIN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pending Deposits", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${pendingDeposits.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column {
                            Text("Active Packages", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${state.packages.size}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column {
                            Text("Active Investors", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "142",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = ForestPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTriggerDailyYield,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_disburse_yield_button")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Disburse Yields", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddPackageDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_add_package_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Package", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Pending Deposit Approvals Section
        item {
            Text(
                text = "Pending Deposit Approvals (${pendingDeposits.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (pendingDeposits.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No pending deposits requiring approval.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            items(pendingDeposits) { proof ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ref: ${proof.referenceNo}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Bank: ${proof.bankName} • ${proof.proofNote}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(
                                text = "Amount: $${String.format("%.2f", proof.amount)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestPrimary)
                            )
                        }

                        Button(
                            onClick = { onApproveDeposit(proof.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("approve_deposit_button_${proof.id}")
                        ) {
                            Text("Approve")
                        }
                    }
                }
            }
        }

        // 3. System Audit Logs
        item {
            Text(
                text = "System Audit Logs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(auditLogs) { log ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Text(log.actor, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }
        }
    }

    if (showAddPackageDialog) {
        CreatePackageDialog(
            onDismiss = { showAddPackageDialog = false },
            onSubmit = { title, cat, price, duration, yieldPct, roiPct, units, location, desc, feed ->
                onAddNewPackage(title, cat, price, duration, yieldPct, roiPct, units, location, desc, feed)
                showAddPackageDialog = false
            }
        )
    }
}

@Composable
fun CreatePackageDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        category: LivestockCategory,
        unitPrice: String,
        durationDays: String,
        dailyYieldPercent: String,
        totalRoiPercent: String,
        totalUnits: String,
        farmLocation: String,
        description: String,
        feedType: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LivestockCategory.LAYER_HENS) }
    var unitPrice by remember { mutableStateOf("180.00") }
    var durationDays by remember { mutableStateOf("90") }
    var dailyYieldPercent by remember { mutableStateOf("0.55") }
    var totalRoiPercent by remember { mutableStateOf("20.0") }
    var totalUnits by remember { mutableStateOf("50") }
    var farmLocation by remember { mutableStateOf("Poultry Coop Sector 2") }
    var description by remember { mutableStateOf("High egg laying efficiency hen flock managed by certified poultry specialists.") }
    var feedType by remember { mutableStateOf("Layer mash with calcium shell grit") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List New Livestock Package", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(360.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Package Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Unit Price ($)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { durationDays = it },
                        label = { Text("Duration (Days)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = dailyYieldPercent,
                        onValueChange = { dailyYieldPercent = it },
                        label = { Text("Daily Yield Rate (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = totalUnits,
                        onValueChange = { totalUnits = it },
                        label = { Text("Total Available Units") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = farmLocation,
                        onValueChange = { farmLocation = it },
                        label = { Text("Farm Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        title, selectedCategory, unitPrice, durationDays,
                        dailyYieldPercent, totalRoiPercent, totalUnits, farmLocation, description, feedType
                    )
                },
                enabled = title.isNotBlank() && unitPrice.isNotBlank(),
                modifier = Modifier.testTag("submit_new_package_button")
            ) {
                Text("List Package")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
