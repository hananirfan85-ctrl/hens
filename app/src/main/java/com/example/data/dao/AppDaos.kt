package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String = "usr_001"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String = "usr_001"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :delta WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, delta: Double)

    @Query("UPDATE users SET totalEarnings = totalEarnings + :delta WHERE id = :userId")
    suspend fun updateTotalEarnings(userId: String, delta: Double)

    @Query("UPDATE users SET kycStatus = :status WHERE id = :userId")
    suspend fun updateKycStatus(userId: String, status: KycStatus)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: UserRole)
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM livestock_packages")
    fun getAllPackagesFlow(): Flow<List<PackageEntity>>

    @Query("SELECT * FROM livestock_packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: String): PackageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<PackageEntity>)

    @Query("UPDATE livestock_packages SET availableUnits = availableUnits - :units WHERE id = :id")
    suspend fun decrementAvailableUnits(id: String, units: Int)
}

@Dao
interface UserLivestockDao {
    @Query("SELECT * FROM user_livestock WHERE userId = :userId ORDER BY purchaseDateTimestamp DESC")
    fun getUserLivestockFlow(userId: String = "usr_001"): Flow<List<UserLivestockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserLivestock(livestock: UserLivestockEntity)

    @Query("UPDATE user_livestock SET totalEarnedSoFar = totalEarnedSoFar + :yieldAmount, daysRemaining = CASE WHEN daysRemaining > 0 THEN daysRemaining - 1 ELSE 0 END WHERE id = :livestockId")
    suspend fun updateYieldForLivestock(livestockId: String, yieldAmount: Double)

    @Query("SELECT * FROM user_livestock WHERE status = 'ACTIVE'")
    suspend fun getAllActiveLivestock(): List<UserLivestockEntity>
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserTransactionsFlow(userId: String = "usr_001"): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("UPDATE wallet_transactions SET status = :status WHERE id = :txId")
    suspend fun updateTransactionStatus(txId: String, status: TransactionStatus)
}

@Dao
interface DepositProofDao {
    @Query("SELECT * FROM deposit_proofs ORDER BY timestamp DESC")
    fun getAllDepositProofsFlow(): Flow<List<DepositProofEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositProof(proof: DepositProofEntity)

    @Query("UPDATE deposit_proofs SET status = :status WHERE id = :id")
    suspend fun updateDepositStatus(id: String, status: TransactionStatus)
}

@Dao
interface WithdrawalRequestDao {
    @Query("SELECT * FROM withdrawal_requests ORDER BY timestamp DESC")
    fun getAllWithdrawalRequestsFlow(): Flow<List<WithdrawalRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawalRequest(request: WithdrawalRequestEntity)

    @Query("UPDATE withdrawal_requests SET status = :status WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: String, status: TransactionStatus)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerUserId = :userId")
    fun getUserReferralsFlow(userId: String = "usr_001"): Flow<List<ReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferrals(referrals: List<ReferralEntity>)
}

@Dao
interface SupportDao {
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTicketsFlow(userId: String = "usr_001"): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_messages WHERE ticketId = :ticketId ORDER BY timestamp ASC")
    fun getMessagesForTicketFlow(ticketId: String): Flow<List<SupportMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SupportMessageEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserNotificationsFlow(userId: String = "usr_001"): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY date DESC")
    fun getAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)
}
