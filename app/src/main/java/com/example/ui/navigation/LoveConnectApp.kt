package com.example.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.MatchCelebrationDialog
import com.example.ui.screens.auth.*
import com.example.ui.screens.main.*
import com.example.ui.screens.onboarding.*
import com.example.ui.screens.profile.*
import com.example.ui.theme.LoveConnectTheme
import com.example.ui.viewmodel.LoveConnectViewModel

private data class MainTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun LoveConnectApp(viewModel: LoveConnectViewModel) {
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val matchedPopup by viewModel.matchedPopup.collectAsStateWithLifecycle()
    val isPartnerTyping by viewModel.isPartnerTyping.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        viewModel.feedbackMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val totalUnreadChats = appState.matchesWithProfiles.sumOf { it.match.unreadCount }

    val mainTabs = listOf(
        MainTabItem(Routes.DISCOVER, "Discover", Icons.Rounded.Explore, Icons.Outlined.Explore),
        MainTabItem(Routes.LIKES, "Likes", Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder, appState.whoLikedYou.size),
        MainTabItem(Routes.MATCHES, "Matches", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
        MainTabItem(Routes.CHAT_LIST, "Chat", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline, totalUnreadChats),
        MainTabItem(Routes.USER_PROFILE, "Profile", Icons.Rounded.Person, Icons.Outlined.PersonOutline)
    )

    val showMainNav = currentRoute in mainTabs.map { it.route }
    val isPremium = appState.account?.subscriptionPlan != null && appState.account?.subscriptionPlan != "FREE"

    LoveConnectTheme(darkTheme = appState.isDarkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showMainNav && !useRail) {
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            mainTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(tab.route) {
                                                popUpTo(Routes.DISCOVER) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (tab.badgeCount > 0) {
                                                    Badge { Text(tab.badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showMainNav && useRail) {
                        NavigationRail {
                            mainTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        if (!selected) {
                                            navController.navigate(tab.route) {
                                                popUpTo(Routes.DISCOVER) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (tab.badgeCount > 0) {
                                                    Badge { Text(tab.badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Routes.SPLASH,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(Routes.SPLASH) {
                            SplashScreen(
                                isAuthenticated = appState.isAuthenticated,
                                isOnboardingComplete = appState.isOnboardingComplete,
                                onNavigateNext = { authenticated, onboarded ->
                                    val dest = when {
                                        authenticated && onboarded -> Routes.DISCOVER
                                        authenticated -> Routes.PROFILE_SETUP
                                        else -> Routes.WELCOME
                                    }
                                    navController.navigate(dest) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.WELCOME) {
                            WelcomeScreen(
                                onCreateAccount = {
                                    viewModel.clearAuthState()
                                    navController.navigate(Routes.REGISTER)
                                },
                                onLogin = {
                                    viewModel.clearAuthState()
                                    navController.navigate(Routes.LOGIN)
                                }
                            )
                        }

                        composable(Routes.LOGIN) {
                            LoginScreen(
                                authState = authState,
                                onLogin = { email, password ->
                                    viewModel.login(email, password) { onboarded ->
                                        val dest = if (onboarded) Routes.DISCOVER else Routes.PROFILE_SETUP
                                        navController.navigate(dest) {
                                            popUpTo(Routes.WELCOME) { inclusive = true }
                                        }
                                    }
                                },
                                onForgotPassword = { email, newPass ->
                                    viewModel.resetPassword(email, newPass) {}
                                },
                                onNavigateToRegister = {
                                    viewModel.clearAuthState()
                                    navController.navigate(Routes.REGISTER)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.REGISTER) {
                            RegisterScreen(
                                authState = authState,
                                onRegister = { name, email, phone, password, dob ->
                                    viewModel.register(name, email, phone, password, dob) {
                                        viewModel.clearAuthState()
                                        navController.navigate(Routes.OTP_VERIFICATION)
                                    }
                                },
                                onNavigateToLogin = {
                                    viewModel.clearAuthState()
                                    navController.navigate(Routes.LOGIN)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.OTP_VERIFICATION) {
                            OtpVerificationScreen(
                                phoneNumber = appState.account?.phoneNumber ?: "",
                                authState = authState,
                                onVerifyOtp = { code ->
                                    viewModel.verifyOtp(code) {
                                        viewModel.clearAuthState()
                                        navController.navigate(Routes.PROFILE_SETUP) {
                                            popUpTo(Routes.WELCOME) { inclusive = true }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.PROFILE_SETUP) {
                            ProfileSetupScreen(
                                existingProfile = appState.currentUserProfile,
                                aiBioState = appState.aiBioState,
                                isEditMode = false,
                                onGenerateAiBio = { name, prof, hobbies, goal ->
                                    viewModel.generateAiBio(name, prof, hobbies, goal)
                                },
                                onSaveProfile = { updated ->
                                    viewModel.saveProfile(updated, markOnboardingDone = false) {
                                        navController.navigate(Routes.uploadPhotos(isOnboarding = true))
                                    }
                                }
                            )
                        }

                        composable(
                            route = Routes.UPLOAD_PHOTOS,
                            arguments = listOf(navArgument("isOnboarding") { type = NavType.BoolType })
                        ) { entry ->
                            val isOnboarding = entry.arguments?.getBoolean("isOnboarding") ?: false
                            UploadPhotosScreen(
                                currentUserProfile = appState.currentUserProfile,
                                isOnboarding = isOnboarding,
                                onSavePhotos = { updated -> viewModel.saveProfile(updated) },
                                onFinish = {
                                    if (isOnboarding) {
                                        viewModel.completePhotoOnboarding {
                                            navController.navigate(Routes.DISCOVER) {
                                                popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.popBackStack()
                                    }
                                },
                                onBack = if (isOnboarding) null else ({ navController.popBackStack() })
                            )
                        }

                        composable(Routes.DISCOVER) {
                            DiscoverScreen(
                                candidates = appState.discoverCandidates,
                                dailyLikesRemaining = appState.account?.dailyLikesRemaining ?: 15,
                                dailySuperLikesRemaining = appState.account?.dailySuperLikesRemaining ?: 1,
                                isPremium = isPremium,
                                isDiscoveryPaused = appState.account?.isDiscoveryPaused == true,
                                unreadNotifications = appState.unreadNotifications,
                                aiCompatibilityState = appState.aiCompatibilityState,
                                onSwipe = { profile, action -> viewModel.swipeProfile(profile, action) },
                                onRewind = { viewModel.rewindLastSwipe() },
                                onAnalyzeCompatibility = { viewModel.analyzeCompatibility(it) },
                                onResetCompatibility = { viewModel.resetCompatibility() },
                                onOpenFilters = { navController.navigate(Routes.FILTERS) },
                                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                                onReportBlock = { navController.navigate(Routes.reportBlock(it.id)) }
                            )
                        }

                        composable(Routes.LIKES) {
                            LikesScreen(
                                whoLikedYou = appState.whoLikedYou,
                                isPremium = isPremium,
                                onLikeBack = { viewModel.swipeProfile(it, "LIKE") },
                                onPass = { viewModel.swipeProfile(it, "PASS") },
                                onUpgradePremium = { navController.navigate(Routes.PREMIUM) }
                            )
                        }

                        composable(Routes.MATCHES) {
                            MatchesScreen(
                                matches = appState.matchesWithProfiles,
                                currentUserProfile = appState.currentUserProfile,
                                onOpenChat = { matchId -> navController.navigate(Routes.individualChat(matchId)) }
                            )
                        }

                        composable(Routes.CHAT_LIST) {
                            ChatListScreen(
                                matches = appState.matchesWithProfiles,
                                onOpenChat = { matchId -> navController.navigate(Routes.individualChat(matchId)) }
                            )
                        }

                        composable(
                            route = Routes.INDIVIDUAL_CHAT,
                            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                        ) { entry ->
                            val matchId = entry.arguments?.getString("matchId") ?: return@composable
                            val match by viewModel.observeMatch(matchId).collectAsStateWithLifecycle(initialValue = null)
                            val partnerId = match?.profileId ?: matchId.removePrefix("match_")
                            val partner by viewModel.observeProfile(partnerId).collectAsStateWithLifecycle(initialValue = null)
                            val messages by viewModel.observeMessages(matchId).collectAsStateWithLifecycle(initialValue = emptyList())

                            IndividualChatScreen(
                                matchId = matchId,
                                partner = partner,
                                messages = messages,
                                isPartnerTyping = isPartnerTyping,
                                aiIcebreakerState = appState.aiIcebreakerState,
                                onMarkRead = { viewModel.markChatRead(matchId) },
                                onSendMessage = { text, img, voice ->
                                    viewModel.sendChatMessage(matchId, partner, text, img, voice)
                                },
                                onDeleteMessage = { viewModel.deleteChatMessage(it) },
                                onLoadIcebreakers = { viewModel.loadIcebreakers(it) },
                                onStartCall = { isVideo ->
                                    navController.navigate(Routes.videoCall(partnerId, isVideo))
                                },
                                onReportBlock = { p ->
                                    navController.navigate(Routes.reportBlock(p.id))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Routes.VIDEO_CALL,
                            arguments = listOf(
                                navArgument("profileId") { type = NavType.StringType },
                                navArgument("isVideo") { type = NavType.BoolType }
                            )
                        ) { entry ->
                            val profileId = entry.arguments?.getString("profileId") ?: ""
                            val isVideo = entry.arguments?.getBoolean("isVideo") ?: true
                            val partner by viewModel.observeProfile(profileId).collectAsStateWithLifecycle(initialValue = null)
                            VideoCallScreen(
                                partner = partner,
                                initialVideoEnabled = isVideo,
                                onEndCall = { duration ->
                                    partner?.let { viewModel.logCallCompleted(it, isVideo, duration) }
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.USER_PROFILE) {
                            UserProfileScreen(
                                account = appState.account,
                                profile = appState.currentUserProfile,
                                onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                                onManagePhotos = { navController.navigate(Routes.uploadPhotos(isOnboarding = false)) },
                                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                                onOpenFilters = { navController.navigate(Routes.FILTERS) },
                                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                                onOpenPrivacySecurity = { navController.navigate(Routes.PRIVACY_SECURITY) },
                                onOpenAdminDashboard = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                                onToggleBoost = { viewModel.toggleProfileBoost() }
                            )
                        }

                        composable(Routes.EDIT_PROFILE) {
                            ProfileSetupScreen(
                                existingProfile = appState.currentUserProfile,
                                aiBioState = appState.aiBioState,
                                isEditMode = true,
                                onGenerateAiBio = { name, prof, hobbies, goal ->
                                    viewModel.generateAiBio(name, prof, hobbies, goal)
                                },
                                onSaveProfile = { updated ->
                                    viewModel.saveProfile(updated) {
                                        navController.popBackStack()
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.FILTERS) {
                            FiltersScreen(
                                currentFilter = appState.filter,
                                isPremium = isPremium,
                                onApplyFilters = { viewModel.updateFilters(it) },
                                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.NOTIFICATIONS) {
                            NotificationsScreen(
                                notifications = appState.notifications,
                                onMarkRead = { viewModel.markNotificationRead(it) },
                                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.PREMIUM) {
                            PremiumSubscriptionScreen(
                                currentPlanId = appState.account?.subscriptionPlan ?: "FREE",
                                transactions = appState.transactions,
                                onSubscribe = { id, title, price ->
                                    viewModel.upgradeSubscription(id, title, price) {}
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                account = appState.account,
                                isDarkMode = appState.isDarkMode,
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onToggleDiscoveryPaused = { viewModel.toggleDiscoveryPaused(it) },
                                onUpdateNotificationPrefs = { m, msg, l, s ->
                                    viewModel.updateNotificationToggles(m, msg, l, s)
                                },
                                onOpenPrivacySecurity = { navController.navigate(Routes.PRIVACY_SECURITY) },
                                onOpenAdminDashboard = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                                onLogout = {
                                    viewModel.logout {
                                        navController.navigate(Routes.WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onLogoutAllDevices = {
                                    viewModel.logoutAllDevices {
                                        navController.navigate(Routes.WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onDeleteAccount = {
                                    viewModel.deleteAccount {
                                        navController.navigate(Routes.WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.PRIVACY_SECURITY) {
                            PrivacySecurityScreen(
                                account = appState.account,
                                profile = appState.currentUserProfile,
                                blockedProfiles = appState.blockedProfiles,
                                onToggleVerification = { viewModel.toggleVerificationBadge() },
                                onUnblockUser = { viewModel.unblockUser(it) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Routes.REPORT_BLOCK,
                            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
                        ) { entry ->
                            val profileId = entry.arguments?.getString("profileId") ?: ""
                            val target by viewModel.observeProfile(profileId).collectAsStateWithLifecycle(initialValue = null)
                            ReportBlockScreen(
                                targetProfile = target,
                                onSubmitReport = { p, reason, details, block ->
                                    viewModel.reportAndBlock(p, reason, details, block) {
                                        navController.navigate(Routes.DISCOVER) {
                                            popUpTo(Routes.DISCOVER) { inclusive = true }
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.ADMIN_DASHBOARD) {
                            AdminDashboardScreen(
                                stats = appState.adminStats,
                                reports = appState.reports,
                                transactions = appState.transactions,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }

            matchedPopup?.let { matched ->
                MatchCelebrationDialog(
                    partner = matched.profile,
                    myPhotoKey = appState.currentUserProfile?.photos?.firstOrNull(),
                    isSuperLike = matched.match.isSuperLike,
                    onSendMessage = {
                        viewModel.dismissMatchPopup()
                        navController.navigate(Routes.individualChat(matched.match.id))
                    },
                    onKeepSwiping = { viewModel.dismissMatchPopup() }
                )
            }
        }
    }
}
