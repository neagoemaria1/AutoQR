package com.autoqr.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_prefs")

class DataStoreManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USERNAME_KEY = stringPreferencesKey("username")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { it[USERNAME_KEY] = username }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    suspend fun clearUsername() {
        context.dataStore.edit { it.remove(USERNAME_KEY) }
    }

    suspend fun clearAll() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USERNAME_KEY)
        }
    }
}