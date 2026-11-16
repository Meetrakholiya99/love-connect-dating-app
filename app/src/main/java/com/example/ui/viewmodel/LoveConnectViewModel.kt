package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoveConnectViewModel(
    private val repository: LoveConnectRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _aiBioState = MutableStateFlow<AiPanelState<AiBioSuggestion>>(AiPanelState.Idle)
    private val _aiCompatibilityState = MutableStateFlow<AiPanelState<AiCompatibilityAnalysis>>(AiPanelState.Idle)
    private val _aiIcebreakerState = MutableStateFlow<AiPanelState<AiIcebreakerResult>>(AiPanelState.Idle)

    private val _matchedPopup = MutableStateFlow<SwipeOutcome.Matched?>(null)
    val matchedPopup: StateFlow<SwipeOutcome.Matched?> = _matchedPopup.asStateFlow()

    private val _feedbackMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val feedbackMessage: SharedFlow<String> = _feedbackMessage.asSharedFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    private val baseSessionFlow = combine(
        repository.isDarkMode,
        repository.activeSessionToken,
        repository.isOnboardingComplete,
        repository.currentAccount,
        repository.currentUserProfile
    ) { darkMode, token, onboardingDone, account, profile ->
        SessionSlice(
            isDarkMode = darkMode,
            isAuthenticated = !token.isNullOrBlank() && account != null,
            isOnboardingComplete = onboardingDone,
            account = account,
            currentUserProfile = profile
        )
    }

    private val discoveryFlow = combine(
        repository.candidateProfiles,
        repository.swipes,
        repository.matches,
        repository.discoveryFilter,
        repository.blockedProfiles
    ) { candidates, swipes, matches, filter, blocked ->
        DiscoverySlice(candidates, swipes, matches, filter, blocked)
    }

    private val metaFlow = combine(
        repository.notifications,
        repository.unreadNotificationCount,
        repository.transactions,
        repository.reports,
        repository.adminStats
    ) { notifs, unread, txs, reports, stats ->
        MetaSlice(notifs, unread, txs, reports, stats)
    }

    private val aiFlow = combine(
        _aiBioState,
        _aiCompatibilityState,
        _aiIcebreakerState
    ) { bio, comp, ice ->
        AiSlice(bio, comp, ice)
    }

    val appState: StateFlow<LoveConnectAppState> = combine(
        baseSessionFlow,
        discoveryFlow,
        metaFlow,
        aiFlow
    ) { session, discovery, meta, ai ->
        val swipedIds = discovery.swipes.map { it.targetProfileId }.toSet()
        val profileMap = discovery.candidates.associateBy { it.id }

        val filteredCandidates = discovery.candidates
            .filter { candidate ->
                val notSwiped = candidate.id !in swipedIds
                val ageOk = candidate.age in discovery.filter.minAge..discovery.filter.maxAge
                val distOk = candidate.distanceKm <= discovery.filter.maxDistanceKm
                val genderOk = when (discovery.filter.genderPreference) {
                    "Everyone", "All" -> true
                    "Women" -> candidate.gender.equals("Woman", ignoreCase = true)
                    "Men" -> candidate.gender.equals("Man", ignoreCase = true)
                    else -> true
                }
                val goalOk = discovery.filter.relationshipGoalFilter == "Any" ||
                    candidate.relationshipGoal.contains(discovery.filter.relationshipGoalFilter, ignoreCase = true)
                val verifiedOk = !discovery.filter.verifiedOnly || candidate.isVerified
                val cityOk = discovery.filter.cityFilter.isBlank() ||
                    candidate.city.contains(discovery.filter.cityFilter.trim(), ignoreCase = true)
                notSwiped && ageOk && distOk && genderOk && goalOk && verifiedOk && cityOk
            }
            .map { candidate ->
                DiscoverCandidate(
                    profile = candidate,
                    compatibilityScore = repository.computeCompatibilityScore(session.currentUserProfile, candidate)
                )
            }
            .sortedWith(
                compareByDescending<DiscoverCandidate> { it.profile.isBoosted }
                    .thenByDescending { it.compatibilityScore }
            )

        val matchedProfileIds = discovery.matches.map { it.profileId }.toSet()
        val whoLiked = discovery.candidates.filter { it.likesYou && it.id !in matchedProfileIds }

        val matchesWithProfiles = discovery.matches.mapNotNull { match ->
            val partner = profileMap[match.profileId] ?: return@mapNotNull null
            MatchWithProfile(match, partner)
        }

        LoveConnectAppState(
            isDarkMode = session.isDarkMode,
            isAuthenticated = session.isAuthenticated,
            isOnboardingComplete = session.isOnboardingComplete,
            account = session.account,
            currentUserProfile = session.currentUserProfile,
            discoverCandidates = filteredCandidates,
            whoLikedYou = whoLiked,
            matchesWithProfiles = matchesWithProfiles,
            blockedProfiles = discovery.blocked,
            notifications = meta.notifications,
            unreadNotifications = meta.unreadCount,
            filter = discovery.filter,
            transactions = meta.transactions,
            reports = meta.reports,
            adminStats = meta.stats,
            aiBioState = ai.bio,
            aiCompatibilityState = ai.compatibility,
            aiIcebreakerState = ai.icebreaker
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LoveConnectAppState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun clearAuthState() {
        _authState.value = AuthUiState.Idle
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun register(fullName: String, email: String, phone: String, password: String, dob: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.registerUser(fullName, email, phone, password, dob)
                .onSuccess {
                    _authState.value = AuthUiState.Success("Account registered!")
                    onSuccess()
                }
                .onFailure {
                    _authState.value = AuthUiState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun login(email: String, password: String, onSuccess: (onboardingComplete: Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.loginWithEmail(email, password)
                .onSuccess {
                    _authState.value = AuthUiState.Success("Welcome back!")
                    val done = appState.value.currentUserProfile?.bio?.isNotBlank() == true
                    onSuccess(done)
                }
                .onFailure {
                    _authState.value = AuthUiState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun resetPassword(email: String, newPassword: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.resetPassword(email, newPassword)
                .onSuccess {
                    _feedbackMessage.tryEmit("Password updated successfully.")
                    onDone()
                }
                .onFailure {
                    _feedbackMessage.tryEmit(it.message ?: "Could not reset password.")
                }
        }
    }

    fun verifyOtp(code: String, onVerified: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.verifyOtp(code)
                .onSuccess {
                    _authState.value = AuthUiState.Success("Phone verified!")
                    onVerified()
                }
                .onFailure {
                    _authState.value = AuthUiState.Error(it.message ?: "Verification failed")
                }
        }
    }

    fun saveProfile(profile: UserProfileEntity, markOnboardingDone: Boolean = false, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCurrentProfile(profile, markOnboardingDone)
            _feedbackMessage.tryEmit("Profile saved")
            onSaved()
        }
    }

    fun completePhotoOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.completePhotoOnboarding()
            onDone()
        }
    }

    fun swipeProfile(candidate: UserProfileEntity, action: String) {
        viewModelScope.launch {
            when (val outcome = repository.recordSwipe(candidate, action)) {
                is SwipeOutcome.Matched -> {
                    _matchedPopup.value = outcome
                }
                is SwipeOutcome.DailyLimitReached -> {
                    _feedbackMessage.tryEmit("Daily free likes limit reached! Upgrade to Premium for unlimited likes.")
                }
                is SwipeOutcome.SuperLikeLimitReached -> {
                    _feedbackMessage.tryEmit("No Super Likes remaining today. Upgrade for 5 daily Super Likes.")
                }
                is SwipeOutcome.RateLimited -> {
                    _feedbackMessage.tryEmit("Slow down — rapid swiping throttled.")
                }
                is SwipeOutcome.AlreadySwiped -> Unit
                is SwipeOutcome.Recorded -> {
                    if (action == "SUPER_LIKE") {
                        _feedbackMessage.tryEmit("Super Like sent to ${candidate.fullName.substringBefore(" ")}!")
                    }
                }
            }
        }
    }

    fun dismissMatchPopup() {
        _matchedPopup.value = null
    }

    fun rewindLastSwipe() {
        viewModelScope.launch {
            repository.rewindLastSwipe()
                .onSuccess { _feedbackMessage.tryEmit("Last swipe rewound!") }
                .onFailure { _feedbackMessage.tryEmit(it.message ?: "Cannot rewind") }
        }
    }

    fun generateAiBio(name: String, profession: String, hobbies: List<String>, goal: String) {
        viewModelScope.launch {
            _aiBioState.value = AiPanelState.Loading
            runCatching {
                aiRepository.generateBio(name, profession, hobbies, goal)
            }.onSuccess {
                _aiBioState.value = AiPanelState.Ready(it)
            }.onFailure {
                _aiBioState.value = AiPanelState.Error(it.message ?: "Bio generation failed")
            }
        }
    }

    fun analyzeCompatibility(candidate: UserProfileEntity) {
        viewModelScope.launch {
            _aiCompatibilityState.value = AiPanelState.Loading
            runCatching {
                aiRepository.analyzeCompatibility(appState.value.currentUserProfile, candidate)
            }.onSuccess {
                _aiCompatibilityState.value = AiPanelState.Ready(it)
            }.onFailure {
                _aiCompatibilityState.value = AiPanelState.Error(it.message ?: "Compatibility analysis failed")
            }
        }
    }

    fun resetCompatibility() {
        _aiCompatibilityState.value = AiPanelState.Idle
    }

    fun loadIcebreakers(matchProfile: UserProfileEntity) {
        viewModelScope.launch {
            _aiIcebreakerState.value = AiPanelState.Loading
            runCatching {
                aiRepository.suggestIcebreakers(appState.value.currentUserProfile, matchProfile)
            }.onSuccess {
                _aiIcebreakerState.value = AiPanelState.Ready(it)
            }.onFailure {
                _aiIcebreakerState.value = AiPanelState.Error(it.message ?: "Could not load icebreakers")
            }
        }
    }

    fun observeMatch(matchId: String) = repository.observeMatch(matchId)
    fun observeProfile(profileId: String) = repository.observeProfile(profileId)
    fun observeMessages(matchId: String) = repository.observeMessages(matchId)

    fun markChatRead(matchId: String) {
        viewModelScope.launch { repository.markChatRead(matchId) }
    }

    fun sendChatMessage(
        matchId: String,
        partnerProfile: UserProfileEntity?,
        text: String,
        imageUri: String? = null,
        voiceDurationSec: Int? = null
    ) {
        if (text.isBlank() && imageUri == null && voiceDurationSec == null) return
        viewModelScope.launch {
            repository.sendMessage(matchId, text, imageUri, voiceDurationSec)
            if (partnerProfile != null) {
                _isPartnerTyping.value = true
                repository.simulateMatchReply(matchId, partnerProfile)
                _isPartnerTyping.value = false
            }
        }
    }

    fun deleteChatMessage(messageId: String) {
        viewModelScope.launch { repository.deleteMessage(messageId) }
    }

    fun logCallCompleted(profile: UserProfileEntity, isVideo: Boolean, durationSec: Int) {
        viewModelScope.launch { repository.logCallNotification(profile, isVideo, durationSec) }
    }

    fun updateFilters(filter: DiscoveryFilterEntity) {
        viewModelScope.launch {
            repository.updateFilters(filter)
            _feedbackMessage.tryEmit("Discovery preferences updated")
        }
    }

    fun upgradeSubscription(planId: String, planTitle: String, amountInr: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val tx = repository.upgradeSubscription(planId, planTitle, amountInr)
            _feedbackMessage.tryEmit("Activated $planTitle (${tx.paymentRef})")
            onComplete()
        }
    }

    fun toggleProfileBoost() {
        viewModelScope.launch {
            repository.toggleProfileBoost()
                .onSuccess { active ->
                    _feedbackMessage.tryEmit(if (active) "Profile Boost activated!" else "Profile Boost paused.")
                }
                .onFailure {
                    _feedbackMessage.tryEmit(it.message ?: "Upgrade to Premium for Profile Boost.")
                }
        }
    }

    fun toggleVerificationBadge() {
        viewModelScope.launch {
            val verified = repository.toggleCurrentProfileVerification()
            _feedbackMessage.tryEmit(if (verified) "Profile Verification Badge active!" else "Verification badge removed.")
        }
    }

    fun toggleDiscoveryPaused(paused: Boolean) {
        viewModelScope.launch { repository.toggleDiscoveryPaused(paused) }
    }

    fun updateNotificationToggles(matches: Boolean, messages: Boolean, likes: Boolean, security: Boolean) {
        viewModelScope.launch { repository.updateNotificationToggles(matches, messages, likes, security) }
    }

    fun reportAndBlock(target: UserProfileEntity, reason: String, details: String, blockAlso: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.reportAndOptionallyBlock(target, reason, details, blockAlso)
            _feedbackMessage.tryEmit(if (blockAlso) "Reported and blocked ${target.fullName}" else "Report submitted")
            onDone()
        }
    }

    fun unblockUser(profileId: String) {
        viewModelScope.launch {
            repository.unblockUser(profileId)
            _feedbackMessage.tryEmit("User unblocked")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onDone()
        }
    }

    fun logoutAllDevices(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logoutAllDevices()
            _feedbackMessage.tryEmit("Signed out from all devices")
            onDone()
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccount()
            _feedbackMessage.tryEmit("Account and local data deleted")
            onDone()
        }
    }

    fun notifyMessage(msg: String) {
        _feedbackMessage.tryEmit(msg)
    }

    private data class SessionSlice(
        val isDarkMode: Boolean,
        val isAuthenticated: Boolean,
        val isOnboardingComplete: Boolean,
        val account: UserAccountEntity?,
        val currentUserProfile: UserProfileEntity?
    )

    private data class DiscoverySlice(
        val candidates: List<UserProfileEntity>,
        val swipes: List<SwipeRecordEntity>,
        val matches: List<MatchEntity>,
        val filter: DiscoveryFilterEntity,
        val blocked: List<UserProfileEntity>
    )

    private data class MetaSlice(
        val notifications: List<AppNotificationEntity>,
        val unreadCount: Int,
        val transactions: List<SubscriptionTransactionEntity>,
        val reports: List<ModerationReportEntity>,
        val stats: AdminDashboardStats
    )

    private data class AiSlice(
        val bio: AiPanelState<AiBioSuggestion>,
        val compatibility: AiPanelState<AiCompatibilityAnalysis>,
        val icebreaker: AiPanelState<AiIcebreakerResult>
    )

    class Factory(
        private val repository: LoveConnectRepository,
        private val aiRepository: AiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoveConnectViewModel(repository, aiRepository) as T
        }
    }
}
