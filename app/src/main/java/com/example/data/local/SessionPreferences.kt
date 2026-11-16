package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "loveconnect_prefs")

class SessionPreferences(private val context: Context) {
    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val ACTIVE_SESSION_TOKEN = stringPreferencesKey("active_session_token")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: true // Default luxury dark mode
    }

    val activeSessionToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_SESSION_TOKEN]
    }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setSessionToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token == null) {
                prefs.remove(Keys.ACTIVE_SESSION_TOKEN)
            } else {
                prefs[Keys.ACTIVE_SESSION_TOKEN] = token
            }
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_SESSION_TOKEN)
            prefs.remove(Keys.ONBOARDING_COMPLETE)
        }
    }
}
