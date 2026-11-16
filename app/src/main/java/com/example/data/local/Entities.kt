package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val phoneNumber: String,
    val fullName: String,
    val dateOfBirth: String,
    val isAdultVerified: Boolean,
    val isOtpVerified: Boolean,
    val sessionToken: String,
    val subscriptionPlan: String = "FREE", // FREE, MONTHLY, QUARTERLY, YEARLY
    val dailyLikesRemaining: Int = 15,
    val dailySuperLikesRemaining: Int = 1,
    val isBoostActive: Boolean = false,
    val isDiscoveryPaused: Boolean = false,
    val pushMatchesEnabled: Boolean = true,
    val pushMessagesEnabled: Boolean = true,
    val pushLikesEnabled: Boolean = true,
    val securityAlertsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val isCurrentUser: Boolean = false,
    val fullName: String,
    val dateOfBirth: String,
    val age: Int,
    val gender: String,
    val interestedIn: String,
    val bio: String,
    val city: String,
    val distanceKm: Int,
    val education: String,
    val profession: String,
    val hobbiesCsv: String,
    val interestsCsv: String,
    val relationshipGoal: String,
    val languagesCsv: String,
    val heightCm: Int,
    val photosCsv: String,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isBoosted: Boolean = false,
    val likesYou: Boolean = false,
    val willMatchOnLike: Boolean = false,
    val isBlocked: Boolean = false,
    val isReported: Boolean = false
) {
    val hobbies: List<String>
        get() = hobbiesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    val interests: List<String>
        get() = interestsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    val languages: List<String>
        get() = languagesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    val photos: List<String>
        get() = photosCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "swipes")
data class SwipeRecordEntity(
    @PrimaryKey val id: String,
    val targetProfileId: String,
    val action: String, // LIKE, PASS, SUPER_LIKE
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val matchedAt: Long = System.currentTimeMillis(),
    val isSuperLike: Boolean = false,
    val lastMessagePreview: String = "You matched! Say hello.",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val senderId: String, // "me" or profileId
    val text: String,
    val imageUri: String? = null,
    val voiceDurationSec: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // SENT, DELIVERED, READ
    val isDeleted: Boolean = false
)

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey val id: String,
    val type: String, // MATCH, MESSAGE, LIKE, SUPER_LIKE, CALL, SECURITY
    val title: String,
    val body: String,
    val relatedProfileId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "moderation_reports")
data class ModerationReportEntity(
    @PrimaryKey val id: String,
    val targetProfileId: String,
    val targetName: String,
    val reason: String,
    val details: String,
    val blockedAlso: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscription_transactions")
data class SubscriptionTransactionEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val planTitle: String,
    val amountInr: Int,
    val paymentRef: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "discovery_filters")
data class DiscoveryFilterEntity(
    @PrimaryKey val id: String = "default",
    val minAge: Int = 20,
    val maxAge: Int = 38,
    val maxDistanceKm: Int = 60,
    val genderPreference: String = "Everyone",
    val relationshipGoalFilter: String = "Any",
    val verifiedOnly: Boolean = false,
    val cityFilter: String = ""
)
