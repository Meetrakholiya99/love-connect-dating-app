package com.example.ui.viewmodel

import com.example.data.local.*
import com.example.data.repository.AdminDashboardStats
import com.example.data.repository.AiBioSuggestion
import com.example.data.repository.AiCompatibilityAnalysis
import com.example.data.repository.AiIcebreakerResult

data class MatchWithProfile(
    val match: MatchEntity,
    val profile: UserProfileEntity
)

data class DiscoverCandidate(
    val profile: UserProfileEntity,
    val compatibilityScore: Int
)

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data class Success(val message: String) : AuthUiState
}

sealed interface AiPanelState<out T> {
    data object Idle : AiPanelState<Nothing>
    data object Loading : AiPanelState<Nothing>
    data class Ready<T>(val data: T) : AiPanelState<T>
    data class Error(val message: String) : AiPanelState<Nothing>
}

data class LoveConnectAppState(
    val isDarkMode: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val account: UserAccountEntity? = null,
    val currentUserProfile: UserProfileEntity? = null,
    val discoverCandidates: List<DiscoverCandidate> = emptyList(),
    val whoLikedYou: List<UserProfileEntity> = emptyList(),
    val matchesWithProfiles: List<MatchWithProfile> = emptyList(),
    val blockedProfiles: List<UserProfileEntity> = emptyList(),
    val notifications: List<AppNotificationEntity> = emptyList(),
    val unreadNotifications: Int = 0,
    val filter: DiscoveryFilterEntity = DiscoveryFilterEntity(),
    val transactions: List<SubscriptionTransactionEntity> = emptyList(),
    val reports: List<ModerationReportEntity> = emptyList(),
    val adminStats: AdminDashboardStats = AdminDashboardStats(0, 0, 0, 0, 0, 0, 0, 0),
    val aiBioState: AiPanelState<AiBioSuggestion> = AiPanelState.Idle,
    val aiCompatibilityState: AiPanelState<AiCompatibilityAnalysis> = AiPanelState.Idle,
    val aiIcebreakerState: AiPanelState<AiIcebreakerResult> = AiPanelState.Idle
)
