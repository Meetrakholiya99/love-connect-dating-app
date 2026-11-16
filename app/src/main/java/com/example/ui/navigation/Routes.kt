package com.example.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val OTP_VERIFICATION = "otp_verification"
    const val PROFILE_SETUP = "profile_setup"
    const val UPLOAD_PHOTOS = "upload_photos/{isOnboarding}"

    // Main Tabs
    const val DISCOVER = "discover"
    const val LIKES = "likes"
    const val MATCHES = "matches"
    const val CHAT_LIST = "chat_list"
    const val USER_PROFILE = "user_profile"

    // Detail & Utility Screens
    const val INDIVIDUAL_CHAT = "chat/{matchId}"
    const val VIDEO_CALL = "video_call/{profileId}/{isVideo}"
    const val EDIT_PROFILE = "edit_profile"
    const val FILTERS = "filters"
    const val NOTIFICATIONS = "notifications"
    const val PREMIUM = "premium"
    const val SETTINGS = "settings"
    const val PRIVACY_SECURITY = "privacy_security"
    const val REPORT_BLOCK = "report_block/{profileId}"
    const val ADMIN_DASHBOARD = "admin_dashboard"

    fun uploadPhotos(isOnboarding: Boolean) = "upload_photos/$isOnboarding"
    fun individualChat(matchId: String) = "chat/$matchId"
    fun videoCall(profileId: String, isVideo: Boolean) = "video_call/$profileId/$isVideo"
    fun reportBlock(profileId: String) = "report_block/$profileId"
}
