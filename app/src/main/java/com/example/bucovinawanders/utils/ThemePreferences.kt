package com.example.bucovinawanders.utils

import android.content.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.*

val Context.dataStore by preferencesDataStore("settings") //extensie care creeaza datastore de tip preferences

object ThemePreferences {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode") //cheie pentru tema

    //functie care returneaza un flow de tip boolean
    fun getTheme(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] == true // Default: Light mode
        }
    }

    //functie care seteaza tema
    suspend fun setTheme(context: Context, isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
}