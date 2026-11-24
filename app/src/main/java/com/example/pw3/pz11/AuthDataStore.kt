package com.example.pw3.pz11

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.evAuthDataStore by preferencesDataStore(name = "ev_charging_auth_prefs")

/**
 * Локальне асинхронне сховище на базі Jetpack DataStore
 * для збереження JWT-токена між перезапусками застосунку.
 */
class AuthDataStore(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    val tokenFlow: Flow<String?> = context.evAuthDataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.evAuthDataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.evAuthDataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}