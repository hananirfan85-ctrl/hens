package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.KycStatus
import com.example.data.model.ReferralEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.UserRole
import com.example.ui.components.KycStatusBadge
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.FarmUiState

@Composable
fun ProfileScreen(
    state: FarmUiState,
    onToggleRole: () -> Unit,
    onSubmitKyc: (fullName: String, idType: String, idNumber: String) -> Unit,
    onOpenTicket: (ticketId: String) -> Unit,
    onCreateTicket: (subject: String, category: String, message: String) -> Unit
) {
    var showKycDialog by remember { mutableStateOf(false) }
    var showCreateTicketDialog by remember { mutableStateOf(false) }

    val user = state.user
    val referrals = state.referrals
    val tickets = state.tickets

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ForestPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.take(2)?.uppercase() ?: "HI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user?.fullName ?: "Hanan Irfan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = user?.email ?: "hanan.irfan@eggvest.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    user?.kycStatus?.let { kyc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KycStatusBadge(status = kyc)

                            if (kyc != KycStatus.VERIFIED) {
                                Button(
                                    onClick = { showKycDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("verify_kyc_now_button")
                                ) {
                                    Text("Verify Identity")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Role Switch Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
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
                        Text(
                            text = "Active Role: ${if (user?.role == UserRole.INVESTOR) "Investor Mode" else "Farm Manager Mode"}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Toggle to access Admin dashboard, deposit approvals, and payout engine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = onToggleRole,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("toggle_role_button")
                    ) {
                        Text("Switch Role")
                    }
                }
            }
        }

        // 3. Referral Program Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = ForestPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Referral Program", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "Code: ${user?.referralCode ?: "EGGVEST-HANAN85"}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Earn 5% commission on every livestock investment completed by your invited friends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Referred Network (${referrals.size}):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    referrals.forEach { ref ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(ref.refereeName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(ref.dateJoined, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Text(
                                text = "+$${String.format("%.2f", ref.commissionAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = ForestPrimary
                            )
                        }
                    }
                }
            }
        }

        // 4. Support Desk & Tickets
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = ForestPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Support Desk & Live Chat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        IconButton(onClick = { showCreateTicketDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New Ticket")
                        }
                    }

                    if (tickets.isEmpty()) {
                        Text("No active support tickets.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    } else {
                        tickets.forEach { ticket ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ticket.subject, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(ticket.lastMessage, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                    }

                                    Button(
                                        onClick = { onOpenTicket(ticket.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("open_chat_button_${ticket.id}")
                                    ) {
                                        Text("Chat", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // KYC Dialog
    if (showKycDialog) {
        KycScannerDialog(
            onDismiss = { showKycDialog = false },
            onSubmit = { name, type, num ->
                onSubmitKyc(name, type, num)
                showKycDialog = false
            }
        )
    }

    // Create Ticket Dialog
    if (showCreateTicketDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateTicketDialog = false },
            onSubmit = { subj, cat, msg ->
                onCreateTicket(subj, cat, msg)
                showCreateTicketDialog = false
            }
        )
    }
}

@Composable
fun KycScannerDialog(
    onDismiss: () -> Unit,
    onSubmit: (fullName: String, idType: String, idNumber: String) -> Unit
) {
    var name by remember { mutableStateOf("Hanan Irfan") }
    var docType by remember { mutableStateOf("National Identity Card") }
    var docNum by remember { mutableStateOf("NID-884192041") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Badge, contentDescription = null, tint = ForestPrimary) },
        title = { Text("KYC Identity Verification", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Legal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = docType,
                    onValueChange = { docType = it },
                    label = { Text("Document Type (ID / Passport)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = docNum,
                    onValueChange = { docNum = it },
                    label = { Text("Document Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(name, docType, docNum) },
                modifier = Modifier.testTag("submit_kyc_button")
            ) {
                Text("Submit & Verify Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onSubmit: (subject: String, category: String, message: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General Inquiry") }
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Support Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(subject, category, message) },
                enabled = subject.isNotBlank() && message.isNotBlank()
            ) {
                Text("Create Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
