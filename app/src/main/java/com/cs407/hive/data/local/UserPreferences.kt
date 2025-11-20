package com.cs407.hive.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("hive_prefs")

object UserPrefsKeys {
    val GROUP_ID = stringPreferencesKey("group_id")
    val USER_NAME = stringPreferencesKey("user_name")
}

suspend fun saveGroupId(context: Context, groupId: String) {
    context.dataStore.edit { prefs ->
        prefs[UserPrefsKeys.GROUP_ID] = groupId
    }
}

suspend fun clearGroupId(context: Context) {
    context.dataStore.edit { prefs ->
        prefs.remove(UserPrefsKeys.GROUP_ID)
    }
}

suspend fun loadGroupId(context: Context): String? {
    val prefs = context.dataStore.data.first()
    return prefs[UserPrefsKeys.GROUP_ID]
}
