package com.example.nt_clock_app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val alarmName: String = "Alarm"
)

@Serializable
data class WorldClockItem(
    val id: Int,
    val label: String,
    val zoneId: String
)

val Context.dataStore by preferencesDataStore(name = "Alarms_Prefs")
val AlarmKey = stringPreferencesKey(name = "Alarms")
val WorldClockKey = stringPreferencesKey(name = "WorldClocks")

@Suppress("unused")
fun saveAlarms1(context: Context, alarms: List<AlarmItem>) {
    val json = Json.encodeToString(value = alarms)

    CoroutineScope(context = Dispatchers.IO).launch {
        context.dataStore.edit { prefs ->
            prefs[AlarmKey] = json
        }
    }
}

@Suppress("unused")
suspend fun loadAlarms1(context: Context): List<AlarmItem> {
    val prefs = context.dataStore.data.first()
    val json = prefs[AlarmKey] ?: return emptyList()
    return Json.decodeFromString(json)
}

@Suppress("unused")
fun saveWorldClocks1(context: Context, worldClocks: List<WorldClockItem>) {
    val json = Json.encodeToString(value = worldClocks)

    CoroutineScope(context = Dispatchers.IO).launch {
        context.dataStore.edit { prefs ->
            prefs[WorldClockKey] = json
        }
    }
}

@Suppress("unused")
suspend fun loadWorldClocks1(context: Context): List<WorldClockItem> {
    val prefs = context.dataStore.data.first()
    val json = prefs[WorldClockKey] ?: return emptyList()
    return Json.decodeFromString(json)
}
