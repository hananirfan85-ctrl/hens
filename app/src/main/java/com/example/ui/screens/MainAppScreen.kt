package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.components.FarmVestBottomNavigation
import com.example.ui.components.PoultryVestTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.viewmodel.FarmUiState
import com.example.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: FarmViewModel) {
    val state by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showNotificationSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.activeTicketId == null) {
                PoultryVestTopBar(
                    title = "PoultryVest",
                    userRole = state.user?.role ?: UserRole.INVESTOR,
                    unreadNotificationCount = state.notifications.count { !it.isRead },
                    onToggleRole = { viewModel.toggleRole() },
                    onNotificationClick = { showNotificationSheet = true }
                )
            }
        },
        bottomBar = {
            if (state.activeTicketId == null) {
                FarmVestBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.activeTicketId != null) {
                SupportChatScreen(
                    state = state,
                    onBackClick = { viewModel.selectTicket(null) },
                    onSendMessage = { viewModel.sendSupportMessage(it) }
                )
            } else {
                when {
                    state.user?.role == UserRole.FARM_MANAGER_ADMIN -> {
                        AdminScreen(
                            state = state,
                            onApproveDeposit = { viewModel.approveDeposit(it) },
                            onTriggerDailyYield = { viewModel.triggerDailyYieldPayout() },
                            onAddNewPackage = { title, cat, price, duration, yieldPct, roiPct, units, location, desc, feed ->
                                viewModel.addNewPackage(title, cat, price, duration, yieldPct, roiPct, units, location, desc, feed)
                            }
                        )
                    }
                    else -> {
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> DashboardScreen(
                                state = state,
                                onDepositClick = { currentTab = NavigationTab.WALLET },
                                onWithdrawClick = { currentTab = NavigationTab.WALLET },
                                onTriggerPayoutClick = { viewModel.triggerDailyYieldPayout() },
                                onBrowseMarketClick = { currentTab = NavigationTab.MARKETPLACE },
                                onLivestockClick = { currentTab = NavigationTab.MY_LIVESTOCK }
                            )

                            NavigationTab.MARKETPLACE -> MarketplaceScreen(
                                state = state,
                                onCategorySelected = { viewModel.setCategoryFilter(it) },
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onPurchaseConfirm = { pkg, units -> viewModel.purchasePackage(pkg, units) }
                            )

                            NavigationTab.MY_LIVESTOCK -> LivestockScreen(
                                state = state,
                                onBrowseMarketClick = { currentTab = NavigationTab.MARKETPLACE }
                            )

                            NavigationTab.WALLET -> WalletScreen(
                                state = state,
                                onDepositSubmit = { amount, bank, ref, note ->
                                    viewModel.submitDeposit(amount, bank, ref, note)
                                },
                                onWithdrawSubmit = { amount, bank, accNo, holder ->
                                    viewModel.submitWithdrawal(amount, bank, accNo, holder)
                                }
                            )

                            NavigationTab.PROFILE -> ProfileScreen(
                                state = state,
                                onToggleRole = { viewModel.toggleRole() },
                                onSubmitKyc = { name, type, num -> viewModel.submitKyc(name, type, num) },
                                onOpenTicket = { ticketId -> viewModel.selectTicket(ticketId) },
                                onCreateTicket = { subj, cat, msg -> viewModel.createSupportTicket(subj, cat, msg) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Notifications Bottom Sheet
    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications & Alerts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(onClick = { showNotificationSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (state.notifications.isEmpty()) {
                    Text("No notifications yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(state.notifications) { ntf ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!ntf.isRead) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = ntf.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ntf.message,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
