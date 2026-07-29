package com.example.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = enumValueOf(value)

    @TypeConverter
    fun fromKycStatus(value: KycStatus): String = value.name

    @TypeConverter
    fun toKycStatus(value: String): KycStatus = enumValueOf(value)

    @TypeConverter
    fun fromLivestockCategory(value: LivestockCategory): String = value.name

    @TypeConverter
    fun toLivestockCategory(value: String): LivestockCategory = enumValueOf(value)

    @TypeConverter
    fun fromLivestockStatus(value: LivestockStatus): String = value.name

    @TypeConverter
    fun toLivestockStatus(value: String): LivestockStatus = enumValueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = enumValueOf(value)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = enumValueOf(value)
}

@Database(
    entities = [
        UserEntity::class,
        PackageEntity::class,
        UserLivestockEntity::class,
        TransactionEntity::class,
        DepositProofEntity::class,
        WithdrawalRequestEntity::class,
        ReferralEntity::class,
        SupportTicketEntity::class,
        SupportMessageEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        AnnouncementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun packageDao(): PackageDao
    abstract fun userLivestockDao(): UserLivestockDao
    abstract fun transactionDao(): TransactionDao
    abstract fun depositProofDao(): DepositProofDao
    abstract fun withdrawalRequestDao(): WithdrawalRequestDao
    abstract fun referralDao(): ReferralDao
    abstract fun supportDao(): SupportDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditDao(): AuditDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eggvest_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }
    }
}

private suspend fun seedDatabase(db: AppDatabase) {
    // 1. Seed User
    val initialUser = UserEntity(
        id = "usr_001",
        fullName = "Hanan Irfan",
        email = "hanan.irfan@eggvest.com",
        phone = "+1 (555) 234-5678",
        walletBalance = 3420.00,
        totalEarnings = 580.00,
        referralCode = "EGGVEST-HANAN85",
        kycStatus = KycStatus.VERIFIED,
        isPinEnabled = true,
        isBiometricsEnabled = true,
        role = UserRole.INVESTOR
    )
    db.userDao().insertOrUpdateUser(initialUser)

    // 2. Seed Hen Investment Packages
    val packages = listOf(
        PackageEntity(
            id = "pkg_rhode_layer_01",
            title = "Rhode Island Red Egg Layers",
            category = LivestockCategory.LAYER_HENS,
            unitPrice = 150.00,
            durationDays = 120,
            dailyYieldPercent = 0.55,
            totalRoiPercent = 25.0,
            availableUnits = 45,
            totalUnits = 100,
            farmLocation = "Coop Alpha, Sector 1",
            insuranceCovered = true,
            description = "High-efficiency Rhode Island Red laying hens producing grade-A brown eggs with automated feeding and climate control.",
            feedType = "Layer mash & calcium shell grit",
            expectedCycleOutput = "Target yield 320 Grade-A eggs/day per unit"
        ),
        PackageEntity(
            id = "pkg_freerange_hen_02",
            title = "Pasture-Raised Golden Laying Flock",
            category = LivestockCategory.FREE_RANGE_LAYERS,
            unitPrice = 220.00,
            durationDays = 90,
            dailyYieldPercent = 0.65,
            totalRoiPercent = 22.0,
            availableUnits = 60,
            totalUnits = 120,
            farmLocation = "Pasture Run Barn 4",
            insuranceCovered = true,
            description = "Free-range heritage hens producing premium pasture-raised eggs with rich golden yolks for organic markets.",
            feedType = "Organic pasture forage & non-GMO grains",
            expectedCycleOutput = "Premium pasture-raised eggs (95% laying rate)"
        ),
        PackageEntity(
            id = "pkg_cobb_breeder_03",
            title = "Cobb Breeder Hen Flock",
            category = LivestockCategory.BROILER_BREEDERS,
            unitPrice = 300.00,
            durationDays = 150,
            dailyYieldPercent = 0.48,
            totalRoiPercent = 28.0,
            availableUnits = 30,
            totalUnits = 80,
            farmLocation = "Breeder Facility B",
            insuranceCovered = true,
            description = "Commercial breeding hens selected for high fertility and parent stock egg production in controlled biosecure units.",
            feedType = "High-protein breeder crumble & omega-3 minerals",
            expectedCycleOutput = "Fertile hatching eggs for commercial hatcheries"
        ),
        PackageEntity(
            id = "pkg_amberlink_organic_04",
            title = "Organic Amberlink Layer Hens",
            category = LivestockCategory.ORGANIC_HERITAGE,
            unitPrice = 180.00,
            durationDays = 100,
            dailyYieldPercent = 0.60,
            totalRoiPercent = 20.0,
            availableUnits = 75,
            totalUnits = 150,
            farmLocation = "EcoCoop Bio Barn 2",
            insuranceCovered = true,
            description = "Certified organic laying hens fed 100% soy-free organic ration with open daylight roosting spaces.",
            feedType = "Certified organic corn & soy-free mash",
            expectedCycleOutput = "Farm-fresh organic certified eggs"
        ),
        PackageEntity(
            id = "pkg_commercial_pullet_05",
            title = "Commercial Pullet Hatchery Batch",
            category = LivestockCategory.HATCHERY_FLOCKS,
            unitPrice = 100.00,
            durationDays = 60,
            dailyYieldPercent = 0.75,
            totalRoiPercent = 15.0,
            availableUnits = 110,
            totalUnits = 300,
            farmLocation = "Hatchery Station 3",
            insuranceCovered = true,
            description = "Young vaccinated pullet flock transitioning into prime egg-laying age with fast capital rotation.",
            feedType = "Chick starter & grower formulation",
            expectedCycleOutput = "Day-old pullets & early cycle egg yields"
        )
    )
    db.packageDao().insertPackages(packages)

    // 3. Seed User Hen Investments
    val now = System.currentTimeMillis()
    val dayMs = 86400000L
    val investments = listOf(
        UserLivestockEntity(
            id = "inv_001",
            userId = "usr_001",
            packageId = "pkg_rhode_layer_01",
            packageTitle = "Rhode Island Red Egg Layers",
            category = LivestockCategory.LAYER_HENS,
            units = 4,
            totalInvested = 600.00,
            dailyYieldAmount = 3.30,
            totalEarnedSoFar = 132.00,
            purchaseDateTimestamp = now - (40 * dayMs),
            expiryDateTimestamp = now + (80 * dayMs),
            daysRemaining = 80,
            totalDurationDays = 120,
            healthScorePercent = 99,
            status = LivestockStatus.ACTIVE,
            lastFeedingNote = "Veterinary check passed. Egg yield rate 98.4% efficiency."
        ),
        UserLivestockEntity(
            id = "inv_002",
            userId = "usr_001",
            packageId = "pkg_freerange_hen_02",
            packageTitle = "Pasture-Raised Golden Laying Flock",
            category = LivestockCategory.FREE_RANGE_LAYERS,
            units = 3,
            totalInvested = 660.00,
            dailyYieldAmount = 4.29,
            totalEarnedSoFar = 128.70,
            purchaseDateTimestamp = now - (30 * dayMs),
            expiryDateTimestamp = now + (60 * dayMs),
            daysRemaining = 60,
            totalDurationDays = 90,
            healthScorePercent = 98,
            status = LivestockStatus.ACTIVE,
            lastFeedingNote = "Morning egg collection complete: 288 eggs collected today."
        )
    )
    investments.forEach { db.userLivestockDao().insertUserLivestock(it) }

    // 4. Seed Wallet Transactions
    val transactions = listOf(
        TransactionEntity(
            id = "tx_101",
            userId = "usr_001",
            type = TransactionType.DEPOSIT,
            amount = 2500.00,
            status = TransactionStatus.COMPLETED,
            referenceNo = "DEP-8921471",
            method = "Bank Wire Transfer",
            timestamp = now - (45 * dayMs),
            description = "Wallet deposit approval from Admin"
        ),
        TransactionEntity(
            id = "tx_102",
            userId = "usr_001",
            type = TransactionType.PACKAGE_PURCHASE,
            amount = 600.00,
            status = TransactionStatus.COMPLETED,
            referenceNo = "ORD-449102",
            method = "Wallet Balance",
            timestamp = now - (40 * dayMs),
            description = "Purchased 4 units of Rhode Island Red Egg Layers"
        ),
        TransactionEntity(
            id = "tx_103",
            userId = "usr_001",
            type = TransactionType.PACKAGE_PURCHASE,
            amount = 660.00,
            status = TransactionStatus.COMPLETED,
            referenceNo = "ORD-519203",
            method = "Wallet Balance",
            timestamp = now - (30 * dayMs),
            description = "Purchased 3 units of Pasture-Raised Golden Laying Flock"
        ),
        TransactionEntity(
            id = "tx_104",
            userId = "usr_001",
            type = TransactionType.DAILY_YIELD,
            amount = 7.59,
            status = TransactionStatus.COMPLETED,
            referenceNo = "YLD-99120",
            method = "System Payout",
            timestamp = now - (1 * dayMs),
            description = "Daily egg yield production payout credited"
        ),
        TransactionEntity(
            id = "tx_105",
            userId = "usr_001",
            type = TransactionType.REFERRAL_COMMISSION,
            amount = 50.00,
            status = TransactionStatus.COMPLETED,
            referenceNo = "REF-22910",
            method = "Referral Bonus",
            timestamp = now - (15 * dayMs),
            description = "Commission for referred user Marcus Vance"
        )
    )
    transactions.forEach { db.transactionDao().insertTransaction(it) }

    // 5. Seed Referrals
    val referrals = listOf(
        ReferralEntity(
            id = "ref_01",
            referrerUserId = "usr_001",
            refereeName = "Marcus Vance",
            refereeEmail = "marcus.v@gmail.com",
            commissionAmount = 50.00,
            dateJoined = "15 Jul 2026",
            isInvested = true
        ),
        ReferralEntity(
            id = "ref_02",
            referrerUserId = "usr_001",
            refereeName = "Sarah Jenkins",
            refereeEmail = "s.jenkins@outlook.com",
            commissionAmount = 25.00,
            dateJoined = "22 Jul 2026",
            isInvested = true
        ),
        ReferralEntity(
            id = "ref_03",
            referrerUserId = "usr_001",
            refereeName = "David Kim",
            refereeEmail = "dkim@techfarm.io",
            commissionAmount = 0.00,
            dateJoined = "27 Jul 2026",
            isInvested = false
        )
    )
    db.referralDao().insertReferrals(referrals)

    // 6. Seed Support Ticket & Messages
    val ticket = SupportTicketEntity(
        id = "TCK-8819",
        userId = "usr_001",
        subject = "Inquiry regarding Insurance Coverage for Hen Flocks",
        category = "Poultry Protection",
        status = "OPEN",
        priority = "NORMAL",
        createdAt = "28 Jul 2026",
        lastMessage = "Our poultry insurance covers 100% mortality and avian biosecurity hazards."
    )
    db.supportDao().insertTicket(ticket)

    db.supportDao().insertMessage(
        SupportMessageEntity(
            ticketId = "TCK-8819",
            senderRole = "USER",
            message = "Hi, does the Rhode Island Red layer package cover heat stress or avian biosecurity hazards?",
            timestamp = now - (12 * 3600000L)
        )
    )
    db.supportDao().insertMessage(
        SupportMessageEntity(
            ticketId = "TCK-8819",
            senderRole = "SUPPORT",
            message = "Hello Hanan! Yes, our poultry insurance covers 100% mortality, climate stress, and avian health hazards underwritten by EggShield Assurance.",
            timestamp = now - (6 * 3600000L)
        )
    )

    // 7. Seed Notifications
    val notifications = listOf(
        NotificationEntity(
            id = "ntf_01",
            userId = "usr_001",
            title = "Daily Egg Yield Credit",
            message = "You received $7.59 daily egg yield earnings from active hen flocks.",
            timestamp = now - (1 * dayMs),
            isRead = false,
            type = "YIELD"
        ),
        NotificationEntity(
            id = "ntf_02",
            userId = "usr_001",
            title = "Poultry Health Update",
            message = "Rhode Island Red Layer Flock health score updated to 99%. Automated feeder & egg collector checked.",
            timestamp = now - (2 * dayMs),
            isRead = true,
            type = "LIVESTOCK"
        ),
        NotificationEntity(
            id = "ntf_03",
            userId = "usr_001",
            title = "KYC Approved",
            message = "Your identity verification documents have been fully approved.",
            timestamp = now - (10 * dayMs),
            isRead = true,
            type = "SYSTEM"
        )
    )
    notifications.forEach { db.notificationDao().insertNotification(it) }

    // 8. Seed Announcements
    val announcements = listOf(
        AnnouncementEntity(
            id = "anc_01",
            title = "Poultry Expansion: New Smart Coop 4 Commissioned",
            content = "We have officially commissioned our state-of-the-art climate-controlled Layer Coop in Sector 1, boosting daily egg collection by 40%.",
            date = "28 Jul 2026"
        ),
        AnnouncementEntity(
            id = "anc_02",
            title = "Quarterly Yield Bonus for Pasture-Raised Layer Investors",
            content = "Due to surge in organic egg market demand, all active free-range layer hen package yields will receive a +0.05% daily bonus through August.",
            date = "20 Jul 2026"
        )
    )
    db.announcementDao().insertAnnouncements(announcements)

    // 9. Seed Audit Log
    db.auditDao().insertAuditLog(
        AuditLogEntity(
            action = "USER_LOGIN",
            actor = "Hanan Irfan",
            details = "Successful authentication via Android Biometric Session",
            timestamp = now
        )
    )
}
