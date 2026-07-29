package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionStatusBadge
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.FarmUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    state: FarmUiState,
    onDepositSubmit: (amount: String, bankName: String, refNo: String, proofNote: String) -> Unit,
    onWithdrawSubmit: (amount: String, bankName: String, accNo: String, accHolder: String) -> Unit
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    val user = state.user
    val transactions = state.transactions

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Digital Wallet Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wallet_balance_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TOTAL WALLET BALANCE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$${String.format("%.2f", user?.walletBalance ?: 0.0)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showDepositDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wallet_deposit_modal_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit Funds", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showWithdrawDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x33FFFFFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wallet_withdraw_modal_button")
                        ) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Transaction History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History (${transactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "All Activity",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 3. Transactions List
        if (transactions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions recorded", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionRowItem(tx = tx)
            }
        }
    }

    // Deposit Modal Dialog
    if (showDepositDialog) {
        DepositModalDialog(
            onDismiss = { showDepositDialog = false },
            onSubmit = { amount, bank, ref, note ->
                onDepositSubmit(amount, bank, ref, note)
                showDepositDialog = false
            }
        )
    }

    // Withdraw Modal Dialog
    if (showWithdrawDialog) {
        WithdrawModalDialog(
            availableBalance = user?.walletBalance ?: 0.0,
            onDismiss = { showWithdrawDialog = false },
            onSubmit = { amount, bank, accNo, holder ->
                onWithdrawSubmit(amount, bank, accNo, holder)
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun TransactionRowItem(tx: TransactionEntity) {
    val isPositive = tx.type == TransactionType.DEPOSIT ||
            tx.type == TransactionType.DAILY_YIELD ||
            tx.type == TransactionType.REFERRAL_COMMISSION

    val icon = when (tx.type) {
        TransactionType.DEPOSIT -> Icons.Default.AddCircleOutline
        TransactionType.WITHDRAWAL -> Icons.Default.ArrowCircleUp
        TransactionType.PACKAGE_PURCHASE -> Icons.Default.ShoppingBag
        TransactionType.DAILY_YIELD -> Icons.Default.TrendingUp
        TransactionType.REFERRAL_COMMISSION -> Icons.Default.GroupAdd
    }

    val iconColor = if (isPositive) ForestPrimary else Color(0xFFE53935)
    val formattedDate = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(tx.timestamp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_row_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${tx.method} • $formattedDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else "-"}$${String.format("%.2f", tx.amount)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                TransactionStatusBadge(status = tx.status)
            }
        }
    }
}

@Composable
fun DepositModalDialog(
    onDismiss: () -> Unit,
    onSubmit: (amount: String, bankName: String, refNo: String, proofNote: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("First National Bank") }
    var refNo by remember { mutableStateOf("DEP-" + (100000..999999).random()) }
    var proofNote by remember { mutableStateOf("Wire transfer receipt attached") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ForestPrimary) },
        title = { Text("Deposit Funds via Bank Transfer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Company Bank Details:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Bank: First National AgriBank", style = MaterialTheme.typography.bodySmall)
                        Text("Acc Name: FarmVest Operations Ltd", style = MaterialTheme.typography.bodySmall)
                        Text("Acc Number: 8810-2947-1920", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Deposit Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input")
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Your Bank Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("Payment Reference No.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = proofNote,
                    onValueChange = { proofNote = it },
                    label = { Text("Proof Note / Attachment Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(amount, bankName, refNo, proofNote) },
                enabled = amount.isNotBlank(),
                modifier = Modifier.testTag("submit_deposit_button")
            ) {
                Text("Submit Deposit Proof")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WithdrawModalDialog(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (amount: String, bankName: String, accNo: String, accHolder: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("Global Farmers Bank") }
    var accNumber by remember { mutableStateOf("9988-124-55") }
    var accHolder by remember { mutableStateOf("Hanan Irfan") }

    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val isValid = amountDouble > 0 && amountDouble <= availableBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = HarvestGold) },
        title = { Text("Withdrawal Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Available Balance: $${String.format("%.2f", availableBalance)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ForestPrimary
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Withdrawal Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input")
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Destination Bank Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accNumber,
                    onValueChange = { accNumber = it },
                    label = { Text("Account Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accHolder,
                    onValueChange = { accHolder = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(amount, bankName, accNumber, accHolder) },
                enabled = isValid,
                modifier = Modifier.testTag("submit_withdraw_button")
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
