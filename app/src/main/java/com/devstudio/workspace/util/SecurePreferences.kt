package com.devstudio.workspace.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Secure preferences for vault settings
 * Stores gesture pattern hash and vault state
 */
class SecurePreferences(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_prefs")
        
        private val GESTURE_PATTERN_HASH = stringPreferencesKey("gesture_pattern_hash")
        private val VAULT_INITIALIZED = booleanPreferencesKey("vault_initialized")
        private val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        private val LAST_FAILED_TIME = stringPreferencesKey("last_failed_time")
        private val GATEWAY_NOTE_ID = stringPreferencesKey("gateway_note_id")
        private val USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
        private val VAULT_KEYWORD = stringPreferencesKey("vault_keyword")
        private val VAULT_PIN = stringPreferencesKey("vault_pin")
        
        // AI Settings
        private val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        private val AI_API_KEY = stringPreferencesKey("ai_api_key")
        private val AI_MODEL = stringPreferencesKey("ai_model")
        private val AI_LANGUAGE = stringPreferencesKey("ai_language")
    }
    
    // Check if vault is initialized
    val isVaultInitialized: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VAULT_INITIALIZED] ?: false
    }
    
    // Get gesture pattern hash
    val gesturePatternHash: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[GESTURE_PATTERN_HASH]
    }
    
    // Get failed attempts count
    val failedAttempts: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FAILED_ATTEMPTS] ?: 0
    }
    
    // Get gateway note ID
    val gatewayNoteId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[GATEWAY_NOTE_ID]
    }
    
    // Check if biometric is enabled
    val useBiometric: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_BIOMETRIC] ?: false
    }
    
    // Save gesture pattern hash
    suspend fun saveGesturePattern(patternHash: String) {
        context.dataStore.edit { prefs ->
            prefs[GESTURE_PATTERN_HASH] = patternHash
            prefs[VAULT_INITIALIZED] = true
        }
    }
    
    // Save gateway note ID
    suspend fun saveGatewayNoteId(noteId: String) {
        context.dataStore.edit { prefs ->
            prefs[GATEWAY_NOTE_ID] = noteId
        }
    }
    
    // Increment failed attempts
    suspend fun incrementFailedAttempts() {
        context.dataStore.edit { prefs ->
            val current = prefs[FAILED_ATTEMPTS] ?: 0
            prefs[FAILED_ATTEMPTS] = current + 1
            prefs[LAST_FAILED_TIME] = System.currentTimeMillis().toString()
        }
    }
    
    // Reset failed attempts
    suspend fun resetFailedAttempts() {
        context.dataStore.edit { prefs ->
            prefs[FAILED_ATTEMPTS] = 0
        }
    }
    
    // Enable/disable biometric
    suspend fun setBiometric(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USE_BIOMETRIC] = enabled
        }
    }
    
    // Get vault keyword
    val vaultKeyword: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[VAULT_KEYWORD] ?: "OPEN VAULT"
    }
    
    // Set vault keyword
    suspend fun setVaultKeyword(keyword: String) {
        context.dataStore.edit { prefs ->
            prefs[VAULT_KEYWORD] = keyword
        }
    }
    
    // Get vault PIN
    val vaultPin: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[VAULT_PIN]
    }
    
    // Set vault PIN
    suspend fun setVaultPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[VAULT_PIN] = pin
        }
    }
    
    // Check if PIN is set
    suspend fun isPinSet(): Boolean {
        var isSet = false
        context.dataStore.data.collect { prefs ->
            isSet = prefs[VAULT_PIN] != null
        }
        return isSet
    }
    
    // Clear all vault data (emergency wipe)
    suspend fun clearVaultData() {
        context.dataStore.edit { prefs ->
            prefs.remove(GESTURE_PATTERN_HASH)
            prefs.remove(VAULT_INITIALIZED)
            prefs.remove(FAILED_ATTEMPTS)
            prefs.remove(LAST_FAILED_TIME)
            prefs.remove(GATEWAY_NOTE_ID)
            prefs.remove(USE_BIOMETRIC)
            prefs.remove(VAULT_KEYWORD)
            prefs.remove(VAULT_PIN)
            prefs.remove(VAULT_PIN)
        }
    }
    
    // AI Settings Getters
    val aiEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AI_ENABLED] ?: false
    }
    
    val aiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[AI_API_KEY] ?: ""
    }
    
    val aiModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[AI_MODEL] ?: ""
    }
    
    val aiLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[AI_LANGUAGE] ?: "English"
    }
    
    // Save AI Settings
    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AI_ENABLED] = enabled
        }
    }
    
    suspend fun setAiConfig(apiKey: String, model: String, language: String = "English") {
        context.dataStore.edit { prefs ->
            prefs[AI_API_KEY] = apiKey
            prefs[AI_MODEL] = model
            prefs[AI_LANGUAGE] = language
        }
    }
    
    suspend fun setAiLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[AI_LANGUAGE] = language
        }
    }
}
