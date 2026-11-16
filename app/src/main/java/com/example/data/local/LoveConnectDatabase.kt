package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        UserProfileEntity::class,
        SwipeRecordEntity::class,
        MatchEntity::class,
        ChatMessageEntity::class,
        AppNotificationEntity::class,
        ModerationReportEntity::class,
        SubscriptionTransactionEntity::class,
        DiscoveryFilterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LoveConnectDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun profileDao(): ProfileDao
    abstract fun swipeDao(): SwipeDao
    abstract fun matchDao(): MatchDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun moderationDao(): ModerationDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun discoveryFilterDao(): DiscoveryFilterDao

    companion object {
        @Volatile
        private var INSTANCE: LoveConnectDatabase? = null

        fun getInstance(context: Context): LoveConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LoveConnectDatabase::class.java,
                    "loveconnect_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
