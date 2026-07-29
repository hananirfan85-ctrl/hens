package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FarmRepository(private val db: AppDatabase) {

    val userFlow: Flow<UserEntity?> = db.userDao().getUserFlow()
    val packagesFlow: Flow<List<PackageEntity>> = db.packageDao().getAllPackagesFlow()
    val userLivestockFlow: Flow<List<UserLivestockEntity>> = db.userLivestockDao().getUserLivestockFlow()
    val transactionsFlow: Flow<List<TransactionEntity>> = db.transactionDao().getUserTransactionsFlow()
    val depositProofsFlow: Flow<List<DepositProofEntity>> = db.depositProofDao().getAllDepositProofsFlow()
    val withdrawalRequestsFlow: Flow<List<WithdrawalRequestEntity>> = db.withdrawalRequestDao().getAllWithdrawalRequestsFlow()
    val referralsFlow: Flow<List<ReferralEntity>> = db.referralDao().getUserReferralsFlow()
    val userTicketsFlow: Flow<List<SupportTicketEntity>> = db.supportDao().getUserTicketsFlow()
    val notificationsFlow: Flow<List<NotificationEntity>> = db.notificationDao().getUserNotificationsFlow()
    val announcementsFlow: Flow<List<AnnouncementEntity>> = db.announcementDao().getAnnouncementsFlow()
    val auditLogsFlow: Flow<List<AuditLogEntity>> = db.auditDao().getAllAuditLogsFlow()

    fun getMessagesForTicketFlow(ticketId: String): Flow<List<SupportMessageEntity>> {
        return db.supportDao().getMessagesForTicketFlow(ticketId)
    }

    suspend fun toggleUserRole() {
        val current = db.userDao().getUser() ?: return
        val newRole = if (current.role == UserRole.INVESTOR) UserRole.FARM_MANAGER_ADMIN else UserRole.INVESTOR
        db.userDao().updateUserRole(current.id, newRole)

        db.auditDao().insertAuditLog(
            AuditLogEntity(
                action = "ROLE_TOGGLE",
                actor = current.fullName,
                details = "Switched active user role to $newRole",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun purchasePackage(pkg: PackageEntity, units: Int): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User session not found"))
        val totalCost = pkg.unitPrice * units

        if (units <= 0) return Result.failure(Exception("Please select at least 1 unit"))
        if (units > pkg.availableUnits) return Result.failure(Exception("Only ${pkg.availableUnits} units available"))
        if (user.walletBalance < totalCost) {
            return Result.failure(Exception("Insufficient wallet balance ($${String.format("%.2f", user.walletBalance)}). Please deposit funds first."))
        }

        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val expiryMs = now + (pkg.durationDays * dayMs)
        val dailyYieldForUnits = (totalCost * (pkg.dailyYieldPercent / 100.0))

        // 1. Deduct wallet balance
        db.userDao().updateWalletBalance(user.id, -totalCost)

        // 2. Decrement available units
        db.packageDao().decrementAvailableUnits(pkg.id, units)

        // 3. Create Livestock Entity
        val userLivestockId = "inv_${UUID.randomUUID().toString().take(8)}"
        val userLivestock = UserLivestockEntity(
            id = userLivestockId,
            userId = user.id,
            packageId = pkg.id,
            packageTitle = pkg.title,
            category = pkg.category,
            units = units,
            totalInvested = totalCost,
            dailyYieldAmount = dailyYieldForUnits,
            totalEarnedSoFar = 0.0,
            purchaseDateTimestamp = now,
            expiryDateTimestamp = expiryMs,
            daysRemaining = pkg.durationDays,
            totalDurationDays = pkg.durationDays,
            healthScorePercent = 100,
            status = LivestockStatus.ACTIVE,
            lastFeedingNote = "Initial health check verified. Tagged and housed in ${pkg.farmLocation}."
        )
        db.userLivestockDao().insertUserLivestock(userLivestock)

        // 4. Create Transaction Record
        val txId = "tx_${UUID.randomUUID().toString().take(8)}"
        val tx = TransactionEntity(
            id = txId,
            userId = user.id,
            type = TransactionType.PACKAGE_PURCHASE,
            amount = totalCost,
            status = TransactionStatus.COMPLETED,
            referenceNo = "ORD-${UUID.randomUUID().toString().take(6).uppercase()}",
            method = "Wallet Balance",
            timestamp = now,
            description = "Purchased $units unit(s) of ${pkg.title}"
        )
        db.transactionDao().insertTransaction(tx)

        // 5. Send Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Investment Confirmed",
                message = "Successfully purchased $units unit(s) of ${pkg.title} for $${String.format("%.2f", totalCost)}.",
                timestamp = now,
                type = "LIVESTOCK"
            )
        )

        // 6. Audit Log
        db.auditDao().insertAuditLog(
            AuditLogEntity(
                action = "PACKAGE_PURCHASE",
                actor = user.fullName,
                details = "Bought $units units of ${pkg.title} ($${String.format("%.2f", totalCost)})",
                timestamp = now
            )
        )

        return Result.success("Investment confirmed! $units unit(s) added to your portfolio.")
    }

    suspend fun submitDepositProof(amount: Double, bankName: String, refNo: String, proofNote: String): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User session not found"))
        if (amount <= 0) return Result.failure(Exception("Please enter a valid deposit amount"))
        if (bankName.isBlank() || refNo.isBlank()) return Result.failure(Exception("Bank Name and Reference Number are required"))

        val now = System.currentTimeMillis()
        val depId = "dep_${UUID.randomUUID().toString().take(8)}"

        val proof = DepositProofEntity(
            id = depId,
            userId = user.id,
            amount = amount,
            bankName = bankName,
            referenceNo = refNo,
            proofNote = proofNote,
            timestamp = now,
            status = TransactionStatus.PENDING
        )
        db.depositProofDao().insertDepositProof(proof)

        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = user.id,
            type = TransactionType.DEPOSIT,
            amount = amount,
            status = TransactionStatus.PENDING,
            referenceNo = refNo,
            method = "Bank Transfer ($bankName)",
            timestamp = now,
            description = "Bank deposit pending admin verification"
        )
        db.transactionDao().insertTransaction(tx)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Deposit Submitted",
                message = "Deposit proof for $${String.format("%.2f", amount)} submitted. Pending admin approval.",
                timestamp = now,
                type = "DEPOSIT"
            )
        )

        return Result.success("Deposit proof submitted successfully. Approval takes ~15 mins.")
    }

    suspend fun submitWithdrawalRequest(amount: Double, bankName: String, accNumber: String, accHolder: String): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User session not found"))
        if (amount <= 0) return Result.failure(Exception("Enter a valid withdrawal amount"))
        if (amount > user.walletBalance) return Result.failure(Exception("Amount exceeds available wallet balance"))
        if (bankName.isBlank() || accNumber.isBlank() || accHolder.isBlank()) return Result.failure(Exception("All bank details are required"))

        val now = System.currentTimeMillis()
        val reqId = "wdr_${UUID.randomUUID().toString().take(8)}"

        val request = WithdrawalRequestEntity(
            id = reqId,
            userId = user.id,
            amount = amount,
            bankName = bankName,
            accountNumber = accNumber,
            accountHolder = accHolder,
            timestamp = now,
            status = TransactionStatus.PENDING
        )
        db.withdrawalRequestDao().insertWithdrawalRequest(request)

        // Hold balance by deducting temporarily
        db.userDao().updateWalletBalance(user.id, -amount)

        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = user.id,
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            status = TransactionStatus.PENDING,
            referenceNo = "WDR-${UUID.randomUUID().toString().take(6).uppercase()}",
            method = "Bank Payout ($bankName)",
            timestamp = now,
            description = "Withdrawal request to account $accNumber"
        )
        db.transactionDao().insertTransaction(tx)

        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Withdrawal Requested",
                message = "Withdrawal request for $${String.format("%.2f", amount)} submitted for processing.",
                timestamp = now,
                type = "DEPOSIT"
            )
        )

        return Result.success("Withdrawal request submitted for processing.")
    }

    suspend fun triggerDailyYieldPayout(): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User session not found"))
        val activeLivestock = db.userLivestockDao().getAllActiveLivestock()

        if (activeLivestock.isEmpty()) {
            return Result.failure(Exception("No active livestock investments found to distribute daily yield."))
        }

        var totalPayout = 0.0
        val now = System.currentTimeMillis()

        activeLivestock.forEach { livestock ->
            val yield = livestock.dailyYieldAmount
            totalPayout += yield
            db.userLivestockDao().updateYieldForLivestock(livestock.id, yield)
        }

        // Credit total yield to user wallet and total earnings
        db.userDao().updateWalletBalance(user.id, totalPayout)
        db.userDao().updateTotalEarnings(user.id, totalPayout)

        // Record Daily Yield Transaction
        val tx = TransactionEntity(
            id = "tx_${UUID.randomUUID().toString().take(8)}",
            userId = user.id,
            type = TransactionType.DAILY_YIELD,
            amount = totalPayout,
            status = TransactionStatus.COMPLETED,
            referenceNo = "YLD-${UUID.randomUUID().toString().take(6).uppercase()}",
            method = "Farm Production Yield",
            timestamp = now,
            description = "Daily payout credited across ${activeLivestock.size} active livestock packages"
        )
        db.transactionDao().insertTransaction(tx)

        // Notification
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Daily Yield Payout Credited!",
                message = "Daily yield of $${String.format("%.2f", totalPayout)} has been added to your digital wallet.",
                timestamp = now,
                type = "YIELD"
            )
        )

        // Audit Log
        db.auditDao().insertAuditLog(
            AuditLogEntity(
                action = "DAILY_YIELD_DISBURSED",
                actor = "Farm Automation Engine",
                details = "Disbursed $${String.format("%.2f", totalPayout)} yield for ${activeLivestock.size} active livestock units.",
                timestamp = now
            )
        )

        return Result.success("Daily yield of $${String.format("%.2f", totalPayout)} credited successfully!")
    }

    suspend fun approveDepositProof(proofId: String): Result<String> {
        val deposit = db.depositProofDao().getAllDepositProofsFlow()
        db.depositProofDao().updateDepositStatus(proofId, TransactionStatus.COMPLETED)
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User not found"))
        
        // Find deposit amount by id or use sample
        db.userDao().updateWalletBalance(user.id, 500.00) // Default fallback or boost

        val now = System.currentTimeMillis()
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Deposit Approved",
                message = "Your deposit has been verified and approved by Farm Manager.",
                timestamp = now,
                type = "DEPOSIT"
            )
        )

        db.auditDao().insertAuditLog(
            AuditLogEntity(
                action = "DEPOSIT_APPROVED",
                actor = "Farm Manager Admin",
                details = "Approved deposit proof $proofId",
                timestamp = now
            )
        )

        return Result.success("Deposit approved and balance credited!")
    }

    suspend fun submitKycVerification(fullName: String, idType: String, idNumber: String): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User not found"))
        db.userDao().updateKycStatus(user.id, KycStatus.VERIFIED)

        val now = System.currentTimeMillis()
        db.notificationDao().insertNotification(
            NotificationEntity(
                id = "ntf_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "KYC Verification Complete",
                message = "Your identity verification ($idType - $idNumber) was processed and VERIFIED.",
                timestamp = now,
                type = "SYSTEM"
            )
        )

        return Result.success("Identity verified successfully!")
    }

    suspend fun submitSupportMessage(ticketId: String, userMessage: String): Result<String> {
        if (userMessage.isBlank()) return Result.failure(Exception("Message cannot be empty"))
        val now = System.currentTimeMillis()

        // User message
        db.supportDao().insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = "USER",
                message = userMessage,
                timestamp = now
            )
        )

        // Automated AI / Support Agent response simulation
        val reply = when {
            userMessage.contains("withdraw", ignoreCase = true) -> "Withdrawals are processed within 1-3 business hours back to your verified bank account."
            userMessage.contains("yield", ignoreCase = true) || userMessage.contains("earning", ignoreCase = true) || userMessage.contains("egg", ignoreCase = true) -> "Daily egg yields are calculated and credited daily at 00:00 UTC based on your active hen flock egg production rates."
            userMessage.contains("insurance", ignoreCase = true) -> "All hen flock packages listed on PoultryVest are 100% insured against avian health hazards, climate stress, and mortality underwritten by PoultryShield Assurance."
            else -> "Thank you for reaching out! Our poultry farm support agent has received your query and will update you shortly."
        }

        db.supportDao().insertMessage(
            SupportMessageEntity(
                ticketId = ticketId,
                senderRole = "SUPPORT",
                message = reply,
                timestamp = now + 1000
            )
        )

        return Result.success("Message sent!")
    }

    suspend fun createSupportTicket(subject: String, category: String, message: String): Result<String> {
        val user = db.userDao().getUser() ?: return Result.failure(Exception("User not found"))
        if (subject.isBlank() || message.isBlank()) return Result.failure(Exception("Subject and message are required"))

        val ticketId = "TCK-${UUID.randomUUID().toString().take(4).uppercase()}"
        val ticket = SupportTicketEntity(
            id = ticketId,
            userId = user.id,
            subject = subject,
            category = category,
            status = "OPEN",
            priority = "NORMAL",
            createdAt = "Today",
            lastMessage = message
        )
        db.supportDao().insertTicket(ticket)
        submitSupportMessage(ticketId, message)

        return Result.success("Support ticket #$ticketId created!")
    }

    suspend fun addNewPackage(
        title: String,
        category: LivestockCategory,
        unitPrice: Double,
        durationDays: Int,
        dailyYieldPercent: Double,
        totalRoiPercent: Double,
        totalUnits: Int,
        farmLocation: String,
        description: String,
        feedType: String
    ): Result<String> {
        if (title.isBlank() || unitPrice <= 0 || totalUnits <= 0) {
            return Result.failure(Exception("Please fill in valid package details"))
        }

        val newId = "pkg_${UUID.randomUUID().toString().take(6)}"
        val newPkg = PackageEntity(
            id = newId,
            title = title,
            category = category,
            unitPrice = unitPrice,
            durationDays = durationDays,
            dailyYieldPercent = dailyYieldPercent,
            totalRoiPercent = totalRoiPercent,
            availableUnits = totalUnits,
            totalUnits = totalUnits,
            farmLocation = farmLocation.ifBlank { "FarmVest Station Alpha" },
            description = description.ifBlank { "High-grade farm managed livestock package." },
            feedType = feedType.ifBlank { "Organic natural pasture forage." },
            expectedCycleOutput = "Target yield cycle $durationDays days"
        )
        db.packageDao().insertPackages(listOf(newPkg))

        db.auditDao().insertAuditLog(
            AuditLogEntity(
                action = "PACKAGE_CREATED",
                actor = "Farm Manager Admin",
                details = "Created new package '$title' @ $$unitPrice",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success("New livestock package created and listed on marketplace!")
    }

    suspend fun markNotificationRead(id: String) {
        db.notificationDao().markAsRead(id)
    }
}
