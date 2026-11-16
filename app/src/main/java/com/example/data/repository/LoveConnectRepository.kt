package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

sealed class SwipeOutcome {
    data class Matched(val match: MatchEntity, val profile: UserProfileEntity) : SwipeOutcome()
    data class Recorded(val action: String) : SwipeOutcome()
    data object DailyLimitReached : SwipeOutcome()
    data object SuperLikeLimitReached : SwipeOutcome()
    data object AlreadySwiped : SwipeOutcome()
    data object RateLimited : SwipeOutcome()
}

data class AdminDashboardStats(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalMatches: Int,
    val totalMessages: Int,
    val premiumSubscribers: Int,
    val reportedAccounts: Int,
    val blockedAccounts: Int,
    val verifiedAccounts: Int
)

class LoveConnectRepository(
    private val db: LoveConnectDatabase,
    private val prefs: SessionPreferences
) {
    private val accountDao = db.accountDao()
    private val profileDao = db.profileDao()
    private val swipeDao = db.swipeDao()
    private val matchDao = db.matchDao()
    private val chatDao = db.chatMessageDao()
    private val notificationDao = db.notificationDao()
    private val moderationDao = db.moderationDao()
    private val subscriptionDao = db.subscriptionDao()
    private val filterDao = db.discoveryFilterDao()

    private var lastSwipeTimestamp = 0L

    val isDarkMode: Flow<Boolean> = prefs.isDarkMode
    val activeSessionToken: Flow<String?> = prefs.activeSessionToken
    val isOnboardingComplete: Flow<Boolean> = prefs.isOnboardingComplete

    val currentAccount: Flow<UserAccountEntity?> = accountDao.observePrimaryAccount()
    val currentUserProfile: Flow<UserProfileEntity?> = profileDao.observeCurrentUserProfile()
    val candidateProfiles: Flow<List<UserProfileEntity>> = profileDao.observeCandidateProfiles()
    val blockedProfiles: Flow<List<UserProfileEntity>> = profileDao.observeBlockedProfiles()
    val allNonSelfProfiles: Flow<List<UserProfileEntity>> = profileDao.observeAllNonSelfProfiles()
    val swipes: Flow<List<SwipeRecordEntity>> = swipeDao.observeSwipes()
    val matches: Flow<List<MatchEntity>> = matchDao.observeMatches()
    val notifications: Flow<List<AppNotificationEntity>> = notificationDao.observeNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.observeUnreadCount()
    val reports: Flow<List<ModerationReportEntity>> = moderationDao.observeReports()
    val transactions: Flow<List<SubscriptionTransactionEntity>> = subscriptionDao.observeTransactions()
    val discoveryFilter: Flow<DiscoveryFilterEntity> = filterDao.observeFilter().map { it ?: DiscoveryFilterEntity() }

    val adminStats: Flow<AdminDashboardStats> = combine(
        accountDao.observePrimaryAccount(),
        profileDao.observeAllNonSelfProfiles(),
        matchDao.observeMatches(),
        chatDao.observeTotalMessageCount(),
        moderationDao.observeReports()
    ) { account, profiles, matchList, msgCount, reportList ->
        val totalProfiles = profiles.size + (if (account != null) 1 else 0)
        val active = profiles.count { it.isOnline && !it.isBlocked } + (if (account != null) 1 else 0)
        val premiumCount = if (account != null && account.subscriptionPlan != "FREE") 3 else 2
        val verifiedCount = profiles.count { it.isVerified } + (if (account != null) 1 else 0)
        AdminDashboardStats(
            totalUsers = totalProfiles,
            activeUsers = active,
            totalMatches = matchList.size,
            totalMessages = msgCount,
            premiumSubscribers = premiumCount,
            reportedAccounts = reportList.size + profiles.count { it.isReported },
            blockedAccounts = profiles.count { it.isBlocked },
            verifiedAccounts = verifiedCount
        )
    }

    suspend fun ensureSeeded() {
        if (profileDao.observeCandidateProfiles().first().isEmpty()) {
            profileDao.upsertProfiles(SeedData.defaultProfiles)
        }
        if (filterDao.getFilterOnce() == null) {
            filterDao.upsertFilter(DiscoveryFilterEntity())
        }
        if (matchDao.observeMatches().first().isEmpty() && swipeDao.observeSwipes().first().isEmpty()) {
            matchDao.upsertMatch(SeedData.initialMatch)
            swipeDao.insertSwipe(
                SwipeRecordEntity(
                    id = "swipe_seed_elena",
                    targetProfileId = SeedData.initialMatch.profileId,
                    action = "SUPER_LIKE",
                    timestamp = SeedData.initialMatch.matchedAt
                )
            )
            chatDao.insertMessages(SeedData.initialMessages)
        }
        if (notificationDao.observeNotifications().first().isEmpty()) {
            notificationDao.insertNotifications(SeedData.initialNotifications)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)

    fun calculateAgeFromDob(dob: String): Int? {
        val parts = dob.trim().split("-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null
        if (month !in 1..12 || day !in 1..31 || year < 1920) return null

        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        if (birth.after(today)) return null

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        dob: String
    ): Result<UserAccountEntity> {
        val age = calculateAgeFromDob(dob)
            ?: return Result.failure(IllegalArgumentException("Enter a valid date of birth (YYYY-MM-DD)."))
        if (age < 18) {
            return Result.failure(IllegalArgumentException("LOVEConnect is strictly 18+. Users under 18 cannot register."))
        }
        if (fullName.trim().length < 2) {
            return Result.failure(IllegalArgumentException("Enter your full name."))
        }
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Enter a valid email address."))
        }
        if (phone.filter { it.isDigit() }.length < 8) {
            return Result.failure(IllegalArgumentException("Enter a valid phone number."))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters."))
        }

        ensureSeeded()
        val token = UUID.randomUUID().toString()
        val account = UserAccountEntity(
            id = "me",
            email = email.trim().lowercase(),
            passwordHash = hashPassword(password),
            phoneNumber = phone.trim(),
            fullName = fullName.trim(),
            dateOfBirth = dob.trim(),
            isAdultVerified = true,
            isOtpVerified = false,
            sessionToken = token
        )
        accountDao.upsertAccount(account)

        val defaultProfile = UserProfileEntity(
            id = "me",
            isCurrentUser = true,
            fullName = fullName.trim(),
            dateOfBirth = dob.trim(),
            age = age,
            gender = "Prefer not to say",
            interestedIn = "Everyone",
            bio = "",
            city = "San Francisco",
            distanceKm = 0,
            education = "",
            profession = "",
            hobbiesCsv = "Coffee|Travel|Music",
            interestsCsv = "Coffee|Travel|Art Galleries|Indie Music",
            relationshipGoal = "Long-term partner",
            languagesCsv = "English",
            heightCm = 172,
            photosCsv = "drawable:sophia",
            isVerified = false,
            isOnline = true
        )
        profileDao.upsertProfile(defaultProfile)
        prefs.setSessionToken(token)
        prefs.setOnboardingComplete(false)
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Account Created",
                body = "Your 18+ account was registered. Complete OTP verification to secure your profile."
            )
        )
        return Result.success(account)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserAccountEntity> {
        val cleanEmail = email.trim().lowercase()
        val existing = accountDao.getAccountByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No local account found for $cleanEmail. Please register first."))
        if (existing.passwordHash != hashPassword(password)) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
        }
        ensureSeeded()
        val newToken = UUID.randomUUID().toString()
        val updated = existing.copy(sessionToken = newToken)
        accountDao.upsertAccount(updated)
        prefs.setSessionToken(newToken)
        if (profileDao.getCurrentUserProfileOnce()?.bio?.isNotBlank() == true) {
            prefs.setOnboardingComplete(true)
        }
        return Result.success(updated)
    }

    suspend fun verifyOtp(code: String): Result<Unit> {
        val account = accountDao.getPrimaryAccountOnce()
            ?: return Result.failure(IllegalStateException("Account not found."))
        if (code.trim() != "482910" && code.trim().length != 6) {
            return Result.failure(IllegalArgumentException("Enter the 6-digit verification code (demo code: 482910)."))
        }
        if (code.trim() != "482910") {
            return Result.failure(IllegalArgumentException("Invalid verification code. Use 482910 to verify locally."))
        }
        accountDao.upsertAccount(account.copy(isOtpVerified = true))
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Phone Verified",
                body = "Your phone number ${account.phoneNumber} has been verified."
            )
        )
        return Result.success(Unit)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val account = accountDao.getAccountByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account registered with $cleanEmail."))
        if (newPassword.length < 8) {
            return Result.failure(IllegalArgumentException("New password must be at least 8 characters."))
        }
        accountDao.upsertAccount(account.copy(passwordHash = hashPassword(newPassword)))
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Password Updated",
                body = "Your password was recently reset."
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateCurrentProfile(profile: UserProfileEntity, markOnboardingDone: Boolean = false) {
        val computedAge = calculateAgeFromDob(profile.dateOfBirth)?.coerceAtLeast(18) ?: profile.age.coerceAtLeast(18)
        profileDao.upsertProfile(profile.copy(id = "me", isCurrentUser = true, age = computedAge))
        val account = accountDao.getPrimaryAccountOnce()
        if (account != null && account.fullName != profile.fullName) {
            accountDao.upsertAccount(account.copy(fullName = profile.fullName))
        }
        if (markOnboardingDone) {
            prefs.setOnboardingComplete(true)
        }
    }

    suspend fun completePhotoOnboarding() {
        prefs.setOnboardingComplete(true)
    }

    suspend fun updateFilters(filter: DiscoveryFilterEntity) {
        filterDao.upsertFilter(filter.copy(id = "default"))
    }

    suspend fun recordSwipe(target: UserProfileEntity, action: String): SwipeOutcome {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTimestamp < 250L) {
            return SwipeOutcome.RateLimited
        }
        lastSwipeTimestamp = now

        if (swipeDao.getSwipeForTarget(target.id) != null) {
            return SwipeOutcome.AlreadySwiped
        }

        val account = accountDao.getPrimaryAccountOnce()
        val isPremium = account?.subscriptionPlan != null && account.subscriptionPlan != "FREE"

        if (action == "LIKE" && !isPremium && (account?.dailyLikesRemaining ?: 0) <= 0) {
            return SwipeOutcome.DailyLimitReached
        }
        if (action == "SUPER_LIKE" && (account?.dailySuperLikesRemaining ?: 0) <= 0) {
            return SwipeOutcome.SuperLikeLimitReached
        }

        val swipe = SwipeRecordEntity(
            id = "swipe_${UUID.randomUUID()}",
            targetProfileId = target.id,
            action = action,
            timestamp = now
        )
        swipeDao.insertSwipe(swipe)

        if (account != null) {
            val nextLikes = if (action == "LIKE" && !isPremium) (account.dailyLikesRemaining - 1).coerceAtLeast(0) else account.dailyLikesRemaining
            val nextSupers = if (action == "SUPER_LIKE") (account.dailySuperLikesRemaining - 1).coerceAtLeast(0) else account.dailySuperLikesRemaining
            accountDao.upsertAccount(account.copy(dailyLikesRemaining = nextLikes, dailySuperLikesRemaining = nextSupers))
        }

        if ((action == "LIKE" || action == "SUPER_LIKE") && (target.willMatchOnLike || target.likesYou)) {
            val existingMatch = matchDao.getMatchByProfileId(target.id)
            if (existingMatch == null) {
                val match = MatchEntity(
                    id = "match_${target.id}",
                    profileId = target.id,
                    matchedAt = now,
                    isSuperLike = action == "SUPER_LIKE",
                    lastMessagePreview = if (action == "SUPER_LIKE") "You Super Liked ${target.fullName.substringBefore(" ")}!" else "It's a Match! Break the ice.",
                    lastMessageTime = now,
                    unreadCount = 1
                )
                matchDao.upsertMatch(match)
                notificationDao.insertNotification(
                    AppNotificationEntity(
                        id = "notif_${UUID.randomUUID()}",
                        type = "MATCH",
                        title = "New Match with ${target.fullName}!",
                        body = "You both liked each other. Say hi or try an AI icebreaker.",
                        relatedProfileId = target.id,
                        timestamp = now
                    )
                )
                return SwipeOutcome.Matched(match, target)
            }
        }
        return SwipeOutcome.Recorded(action)
    }

    suspend fun rewindLastSwipe(): Result<String> {
        val account = accountDao.getPrimaryAccountOnce()
        val isPremium = account?.subscriptionPlan != null && account.subscriptionPlan != "FREE"
        if (!isPremium) {
            return Result.failure(IllegalStateException("Rewind is a Premium benefit. Upgrade to undo your last swipe."))
        }
        val last = swipeDao.getLastSwipe()
            ?: return Result.failure(IllegalStateException("No recent swipe to rewind."))
        swipeDao.deleteSwipeById(last.id)
        return Result.success(last.targetProfileId)
    }

    fun observeMatch(matchId: String): Flow<MatchEntity?> = matchDao.observeMatchById(matchId)

    fun observeProfile(profileId: String): Flow<UserProfileEntity?> = profileDao.observeProfileById(profileId)

    fun observeMessages(matchId: String): Flow<List<ChatMessageEntity>> = chatDao.observeMessagesForMatch(matchId)

    suspend fun markChatRead(matchId: String) {
        matchDao.markMatchRead(matchId)
        chatDao.markIncomingMessagesRead(matchId)
    }

    suspend fun sendMessage(
        matchId: String,
        text: String,
        imageUri: String? = null,
        voiceDurationSec: Int? = null
    ) {
        val match = matchDao.getMatchById(matchId) ?: return
        val now = System.currentTimeMillis()
        val preview = when {
            imageUri != null -> "Sent a photo"
            voiceDurationSec != null -> "Voice note (${voiceDurationSec}s)"
            else -> text.trim()
        }
        val msg = ChatMessageEntity(
            id = "msg_${UUID.randomUUID()}",
            matchId = matchId,
            senderId = "me",
            text = text.trim(),
            imageUri = imageUri,
            voiceDurationSec = voiceDurationSec,
            timestamp = now,
            status = "DELIVERED"
        )
        chatDao.insertMessage(msg)
        matchDao.upsertMatch(
            match.copy(
                lastMessagePreview = preview,
                lastMessageTime = now,
                unreadCount = 0
            )
        )
    }

    suspend fun simulateMatchReply(matchId: String, partnerProfile: UserProfileEntity) {
        delay(900)
        val match = matchDao.getMatchById(matchId) ?: return
        val firstName = partnerProfile.fullName.substringBefore(" ")
        val hobby = partnerProfile.hobbies.firstOrNull() ?: "great coffee"
        val replyText = listOf(
            "Haha I love that! Tell me more — are you into $hobby too?",
            "You just made my day 😊 What are your weekend plans around ${partnerProfile.city}?",
            "Love the energy! When are you free for a quick coffee or walk?"
        ).random()
        val now = System.currentTimeMillis()
        chatDao.insertMessage(
            ChatMessageEntity(
                id = "msg_${UUID.randomUUID()}",
                matchId = matchId,
                senderId = partnerProfile.id,
                text = replyText,
                timestamp = now,
                status = "DELIVERED"
            )
        )
        matchDao.upsertMatch(
            match.copy(
                lastMessagePreview = "$firstName: $replyText",
                lastMessageTime = now
            )
        )
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.softDeleteMessage(messageId)
    }

    suspend fun logCallNotification(profile: UserProfileEntity, isVideo: Boolean, durationSec: Int) {
        val mode = if (isVideo) "Video" else "Audio"
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "CALL",
                title = "$mode Call with ${profile.fullName}",
                body = "Completed call (${durationSec}s).",
                relatedProfileId = profile.id
            )
        )
    }

    suspend fun upgradeSubscription(planId: String, planTitle: String, amountInr: Int): SubscriptionTransactionEntity {
        val account = accountDao.getPrimaryAccountOnce()
        val ref = "rzp_local_${UUID.randomUUID().toString().take(10)}"
        val tx = SubscriptionTransactionEntity(
            id = "tx_${UUID.randomUUID()}",
            planId = planId,
            planTitle = planTitle,
            amountInr = amountInr,
            paymentRef = ref,
            status = "VERIFIED_SANDBOX"
        )
        subscriptionDao.insertTransaction(tx)
        if (account != null) {
            val isPremium = planId != "FREE"
            accountDao.upsertAccount(
                account.copy(
                    subscriptionPlan = planId,
                    dailyLikesRemaining = if (isPremium) 999 else 15,
                    dailySuperLikesRemaining = if (isPremium) 5 else 1,
                    isBoostActive = isPremium
                )
            )
        }
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Subscription Updated: $planTitle",
                body = "Reference $ref confirmed. Enjoy your unlocked LOVEConnect benefits."
            )
        )
        return tx
    }

    suspend fun toggleProfileBoost(): Result<Boolean> {
        val account = accountDao.getPrimaryAccountOnce() ?: return Result.failure(IllegalStateException("No account"))
        if (account.subscriptionPlan == "FREE") {
            return Result.failure(IllegalStateException("Profile Boost requires a Premium plan."))
        }
        val next = !account.isBoostActive
        accountDao.upsertAccount(account.copy(isBoostActive = next))
        return Result.success(next)
    }

    suspend fun toggleCurrentProfileVerification(): Boolean {
        val profile = profileDao.getCurrentUserProfileOnce() ?: return false
        val next = !profile.isVerified
        profileDao.setVerified(profile.id, next)
        return next
    }

    suspend fun toggleDiscoveryPaused(paused: Boolean) {
        val account = accountDao.getPrimaryAccountOnce() ?: return
        accountDao.upsertAccount(account.copy(isDiscoveryPaused = paused))
    }

    suspend fun updateNotificationToggles(
        matches: Boolean,
        messages: Boolean,
        likes: Boolean,
        security: Boolean
    ) {
        val account = accountDao.getPrimaryAccountOnce() ?: return
        accountDao.upsertAccount(
            account.copy(
                pushMatchesEnabled = matches,
                pushMessagesEnabled = messages,
                pushLikesEnabled = likes,
                securityAlertsEnabled = security
            )
        )
    }

    suspend fun reportAndOptionallyBlock(
        targetProfile: UserProfileEntity,
        reason: String,
        details: String,
        blockUser: Boolean
    ) {
        moderationDao.insertReport(
            ModerationReportEntity(
                id = "rep_${UUID.randomUUID()}",
                targetProfileId = targetProfile.id,
                targetName = targetProfile.fullName,
                reason = reason,
                details = details.trim(),
                blockedAlso = blockUser
            )
        )
        profileDao.markReported(targetProfile.id)
        if (blockUser) {
            profileDao.setBlocked(targetProfile.id, true)
            matchDao.deleteMatchByProfileId(targetProfile.id)
        }
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Safety Report Submitted",
                body = if (blockUser) "${targetProfile.fullName} was reported and blocked." else "Report submitted for ${targetProfile.fullName}."
            )
        )
    }

    suspend fun unblockUser(profileId: String) {
        profileDao.setBlocked(profileId, false)
    }

    suspend fun markNotificationRead(id: String) = notificationDao.markRead(id)
    suspend fun markAllNotificationsRead() = notificationDao.markAllRead()

    suspend fun logout() {
        prefs.setSessionToken(null)
    }

    suspend fun logoutAllDevices() {
        val account = accountDao.getPrimaryAccountOnce()
        if (account != null) {
            accountDao.upsertAccount(account.copy(sessionToken = UUID.randomUUID().toString()))
        }
        notificationDao.insertNotification(
            AppNotificationEntity(
                id = "notif_${UUID.randomUUID()}",
                type = "SECURITY",
                title = "Signed Out From All Devices",
                body = "All active refresh tokens were rotated."
            )
        )
        prefs.setSessionToken(null)
    }

    suspend fun deleteAccount() {
        accountDao.clearAllAccounts()
        profileDao.deleteCurrentUserProfile()
        swipeDao.clearSwipes()
        matchDao.clearMatches()
        chatDao.clearMessages()
        notificationDao.clearNotifications()
        prefs.clearSession()
    }

    fun computeCompatibilityScore(me: UserProfileEntity?, candidate: UserProfileEntity): Int {
        if (me == null) return 85
        val myInterests = me.interests.map { it.lowercase() }.toSet()
        val sharedCount = candidate.interests.count { it.lowercase() in myInterests }
        val goalBonus = if (me.relationshipGoal == candidate.relationshipGoal) 8 else 2
        val distanceScore = (15 - (candidate.distanceKm / 10)).coerceIn(0, 15)
        return (68 + sharedCount * 6 + goalBonus + distanceScore).coerceIn(62, 99)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
