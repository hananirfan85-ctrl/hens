package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FarmUiState(
    val user: UserEntity? = null,
    val packages: List<PackageEntity> = emptyList(),
    val userLivestock: List<UserLivestockEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val depositProofs: List<DepositProofEntity> = emptyList(),
    val withdrawalRequests: List<WithdrawalRequestEntity> = emptyList(),
    val referrals: List<ReferralEntity> = emptyList(),
    val tickets: List<SupportTicketEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val announcements: List<AnnouncementEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val selectedCategory: LivestockCategory = LivestockCategory.ALL,
    val searchQuery: String = "",
    val activeTicketId: String? = null,
    val ticketMessages: List<SupportMessageEntity> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null
)

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FarmRepository
    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FarmRepository(db)

        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.userFlow.collect { user -> _uiState.update { it.copy(user = user) } }
        }
        viewModelScope.launch {
            repository.packagesFlow.collect { pkgs -> _uiState.update { it.copy(packages = pkgs) } }
        }
        viewModelScope.launch {
            repository.userLivestockFlow.collect { livestock -> _uiState.update { it.copy(userLivestock = livestock) } }
        }
        viewModelScope.launch {
            repository.transactionsFlow.collect { txs -> _uiState.update { it.copy(transactions = txs) } }
        }
        viewModelScope.launch {
            repository.depositProofsFlow.collect { deposits -> _uiState.update { it.copy(depositProofs = deposits) } }
        }
        viewModelScope.launch {
            repository.referralsFlow.collect { refs -> _uiState.update { it.copy(referrals = refs) } }
        }
        viewModelScope.launch {
            repository.userTicketsFlow.collect { tickets -> _uiState.update { it.copy(tickets = tickets) } }
        }
        viewModelScope.launch {
            repository.notificationsFlow.collect { notifications -> _uiState.update { it.copy(notifications = notifications) } }
        }
        viewModelScope.launch {
            repository.announcementsFlow.collect { announcements -> _uiState.update { it.copy(announcements = announcements) } }
        }
        viewModelScope.launch {
            repository.withdrawalRequestsFlow.collect { wds -> _uiState.update { it.copy(withdrawalRequests = wds) } }
        }
        viewModelScope.launch {
            repository.auditLogsFlow.collect { logs -> _uiState.update { it.copy(auditLogs = logs) } }
        }
    }

    fun setCategoryFilter(category: LivestockCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectTicket(ticketId: String?) {
        _uiState.update { it.copy(activeTicketId = ticketId) }
        if (ticketId != null) {
            viewModelScope.launch {
                repository.getMessagesForTicketFlow(ticketId).collect { msgs ->
                    _uiState.update { it.copy(ticketMessages = msgs) }
                }
            }
        } else {
            _uiState.update { it.copy(ticketMessages = emptyList()) }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun toggleRole() {
        viewModelScope.launch {
            repository.toggleUserRole()
            _uiState.update { it.copy(snackbarMessage = "Switched User Role") }
        }
    }

    fun purchasePackage(pkg: PackageEntity, units: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.purchasePackage(pkg, units)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "Purchase failed" }
                )
            }
        }
    }

    fun submitDeposit(amountStr: String, bankName: String, refNo: String, proofNote: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.submitDepositProof(amount, bankName, refNo, proofNote)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "Deposit failed" }
                )
            }
        }
    }

    fun submitWithdrawal(amountStr: String, bankName: String, accNumber: String, accHolder: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.submitWithdrawalRequest(amount, bankName, accNumber, accHolder)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "Withdrawal failed" }
                )
            }
        }
    }

    fun triggerDailyYieldPayout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.triggerDailyYieldPayout()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "Payout trigger failed" }
                )
            }
        }
    }

    fun approveDeposit(proofId: String) {
        viewModelScope.launch {
            val res = repository.approveDepositProof(proofId)
            _uiState.update { it.copy(snackbarMessage = res.getOrNull()) }
        }
    }

    fun submitKyc(fullName: String, idType: String, idNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.submitKycVerification(fullName, idType, idNumber)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "KYC failed" }
                )
            }
        }
    }

    fun sendSupportMessage(message: String) {
        val ticketId = _uiState.value.activeTicketId ?: return
        viewModelScope.launch {
            val res = repository.submitSupportMessage(ticketId, message)
            if (res.isFailure) {
                _uiState.update { it.copy(snackbarMessage = res.exceptionOrNull()?.message) }
            }
        }
    }

    fun createSupportTicket(subject: String, category: String, message: String) {
        viewModelScope.launch {
            val res = repository.createSupportTicket(subject, category, message)
            _uiState.update { it.copy(snackbarMessage = res.getOrElse { err -> err.message ?: "Failed" }) }
        }
    }

    fun addNewPackage(
        title: String,
        category: LivestockCategory,
        unitPriceStr: String,
        durationDaysStr: String,
        dailyYieldPercentStr: String,
        totalRoiPercentStr: String,
        totalUnitsStr: String,
        farmLocation: String,
        description: String,
        feedType: String
    ) {
        val unitPrice = unitPriceStr.toDoubleOrNull() ?: 0.0
        val durationDays = durationDaysStr.toIntOrNull() ?: 90
        val dailyYieldPercent = dailyYieldPercentStr.toDoubleOrNull() ?: 0.5
        val totalRoiPercent = totalRoiPercentStr.toDoubleOrNull() ?: 20.0
        val totalUnits = totalUnitsStr.toIntOrNull() ?: 50

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.addNewPackage(
                title, category, unitPrice, durationDays, dailyYieldPercent,
                totalRoiPercent, totalUnits, farmLocation, description, feedType
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    snackbarMessage = res.getOrElse { err -> err.message ?: "Error adding package" }
                )
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }
}
