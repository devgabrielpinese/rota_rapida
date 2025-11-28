package com.example.rota_rapida.presentation.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefs {
    private val HAS_CREATED_FIRST_ROUTE = booleanPreferencesKey("has_created_first_route")

    fun isFirstRunFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> !(prefs[HAS_CREATED_FIRST_ROUTE] ?: false) }

    suspend fun markFirstRouteCreated(context: Context) {
        context.dataStore.edit { it[HAS_CREATED_FIRST_ROUTE] = true }
    }
}
