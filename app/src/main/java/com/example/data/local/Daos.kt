package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM user_accounts LIMIT 1")
    fun observePrimaryAccount(): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts LIMIT 1")
    suspend fun getPrimaryAccountOnce(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: UserAccountEntity)

    @Query("SELECT COUNT(*) FROM user_accounts")
    fun observeAccountCount(): Flow<Int>

    @Query("DELETE FROM user_accounts")
    suspend fun clearAllAccounts()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserProfileOnce(): UserProfileEntity?

    @Query("SELECT * FROM profiles WHERE isCurrentUser = 0 AND isBlocked = 0")
    fun observeCandidateProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isCurrentUser = 0")
    fun observeAllNonSelfProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isCurrentUser = 0 AND isBlocked = 1")
    fun observeBlockedProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    fun observeProfileById(profileId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfiles(profiles: List<UserProfileEntity>)

    @Query("UPDATE profiles SET isBlocked = :blocked WHERE id = :profileId")
    suspend fun setBlocked(profileId: String, blocked: Boolean)

    @Query("UPDATE profiles SET isReported = 1 WHERE id = :profileId")
    suspend fun markReported(profileId: String)

    @Query("UPDATE profiles SET isVerified = :verified WHERE id = :profileId")
    suspend fun setVerified(profileId: String, verified: Boolean)

    @Query("DELETE FROM profiles WHERE isCurrentUser = 1")
    suspend fun deleteCurrentUserProfile()
}

@Dao
interface SwipeDao {
    @Query("SELECT * FROM swipes ORDER BY timestamp DESC")
    fun observeSwipes(): Flow<List<SwipeRecordEntity>>

    @Query("SELECT * FROM swipes ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSwipe(): SwipeRecordEntity?

    @Query("SELECT * FROM swipes WHERE targetProfileId = :profileId LIMIT 1")
    suspend fun getSwipeForTarget(profileId: String): SwipeRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipe(swipe: SwipeRecordEntity)

    @Query("DELETE FROM swipes WHERE id = :swipeId")
    suspend fun deleteSwipeById(swipeId: String)

    @Query("DELETE FROM swipes")
    suspend fun clearSwipes()
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY lastMessageTime DESC")
    fun observeMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE profileId = :profileId LIMIT 1")
    suspend fun getMatchByProfileId(profileId: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    fun observeMatchById(matchId: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE profileId = :profileId")
    suspend fun deleteMatchByProfileId(profileId: String)

    @Query("UPDATE matches SET unreadCount = 0 WHERE id = :matchId")
    suspend fun markMatchRead(matchId: String)

    @Query("DELETE FROM matches")
    suspend fun clearMatches()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun observeMessagesForMatch(matchId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE isDeleted = 0")
    fun observeTotalMessageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isDeleted = 1, text = 'Message deleted' WHERE id = :messageId")
    suspend fun softDeleteMessage(messageId: String)

    @Query("UPDATE chat_messages SET status = 'READ' WHERE matchId = :matchId AND senderId != 'me'")
    suspend fun markIncomingMessagesRead(matchId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<AppNotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}

@Dao
interface ModerationDao {
    @Query("SELECT * FROM moderation_reports ORDER BY timestamp DESC")
    fun observeReports(): Flow<List<ModerationReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ModerationReportEntity)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_transactions ORDER BY timestamp DESC")
    fun observeTransactions(): Flow<List<SubscriptionTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SubscriptionTransactionEntity)
}

@Dao
interface DiscoveryFilterDao {
    @Query("SELECT * FROM discovery_filters WHERE id = 'default' LIMIT 1")
    fun observeFilter(): Flow<DiscoveryFilterEntity?>

    @Query("SELECT * FROM discovery_filters WHERE id = 'default' LIMIT 1")
    suspend fun getFilterOnce(): DiscoveryFilterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFilter(filter: DiscoveryFilterEntity)
}
