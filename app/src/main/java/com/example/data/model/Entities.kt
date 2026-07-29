package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    INVESTOR,
    FARM_MANAGER_ADMIN
}

enum class KycStatus {
    NOT_VERIFIED,
    PENDING,
    VERIFIED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "usr_001",
    val fullName: String = "Hanan Irfan",
    val email: String = "hanan.irfan@eggvest.com",
    val phone: String = "+1 (555) 234-5678",
    val walletBalance: Double = 2850.00,
    val totalEarnings: Double = 412.50,
    val referralCode: String = "EGGVEST-HANAN85",
    val kycStatus: KycStatus = KycStatus.VERIFIED,
    val isPinEnabled: Boolean = true,
    val isBiometricsEnabled: Boolean = true,
    val role: UserRole = UserRole.INVESTOR
)

enum class LivestockCategory {
    ALL,
    LAYER_HENS,
    FREE_RANGE_LAYERS,
    BROILER_BREEDERS,
    HATCHERY_FLOCKS,
    ORGANIC_HERITAGE
}

@Entity(tableName = "livestock_packages")
data class PackageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: LivestockCategory,
    val unitPrice: Double,
    val durationDays: Int,
    val dailyYieldPercent: Double,
    val totalRoiPercent: Double,
    val availableUnits: Int,
    val totalUnits: Int,
    val farmLocation: String,
    val insuranceCovered: Boolean = true,
    val description: String,
    val feedType: String,
    val expectedCycleOutput: String,
    val imageUrl: String = ""
)

enum class LivestockStatus {
    ACTIVE,
    MATURED,
    COMPLETED
}

@Entity(tableName = "user_livestock")
data class UserLivestockEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val packageId: String,
    val packageTitle: String,
    val category: LivestockCategory,
    val units: Int,
    val totalInvested: Double,
    val dailyYieldAmount: Double,
    val totalEarnedSoFar: Double,
    val purchaseDateTimestamp: Long,
    val expiryDateTimestamp: Long,
    val daysRemaining: Int,
    val totalDurationDays: Int,
    val healthScorePercent: Int = 98,
    val status: LivestockStatus = LivestockStatus.ACTIVE,
    val lastFeedingNote: String = "Feed ration: Layer mash with calcium shell grit & organic supplements"
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    PACKAGE_PURCHASE,
    DAILY_YIELD,
    REFERRAL_COMMISSION
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    REJECTED
}

@Entity(tableName = "wallet_transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: TransactionType,
    val amount: Double,
    val status: TransactionStatus,
    val referenceNo: String,
    val method: String,
    val timestamp: Long,
    val description: String
)

@Entity(tableName = "deposit_proofs")
data class DepositProofEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val bankName: String,
    val referenceNo: String,
    val proofNote: String,
    val timestamp: Long,
    val status: TransactionStatus = TransactionStatus.PENDING
)

@Entity(tableName = "withdrawal_requests")
data class WithdrawalRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val timestamp: Long,
    val status: TransactionStatus = TransactionStatus.PENDING
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey val id: String,
    val referrerUserId: String,
    val refereeName: String,
    val refereeEmail: String,
    val commissionAmount: Double,
    val dateJoined: String,
    val isInvested: Boolean
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subject: String,
    val category: String,
    val status: String, // OPEN, RESOLVED
    val priority: String, // NORMAL, HIGH
    val createdAt: String,
    val lastMessage: String
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticketId: String,
    val senderRole: String, // USER, SUPPORT
    val message: String,
    val timestamp: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String = "SYSTEM"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val actor: String,
    val details: String,
    val timestamp: Long
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val date: String,
    val author: String = "EggVest Poultry Ops"
)
