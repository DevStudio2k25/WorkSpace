package com.devstudio.workspace.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devstudio.workspace.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    
    private object PreferencesKeys {
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
    }
    
    val selectedTheme: Flow<AppTheme> = context.themeDataStore.data
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.SELECTED_THEME] ?: AppTheme.FOREST.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.FOREST
            }
        }
    
    suspend fun setTheme(theme: AppTheme) {
        context.themeDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_THEME] = theme.name
        }
    }
}
