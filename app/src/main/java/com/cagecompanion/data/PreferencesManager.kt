package com.cagecompanion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cage_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        // Nightscout settings
        val NIGHTSCOUT_URL = stringPreferencesKey("nightscout_url")
        val NIGHTSCOUT_API_SECRET = stringPreferencesKey("nightscout_api_secret")

        // Threshold settings (in minutes)
        val YELLOW_THRESHOLD_MINUTES = intPreferencesKey("yellow_threshold_minutes")
        val RED_THRESHOLD_MINUTES = intPreferencesKey("red_threshold_minutes")

        // Notification settings
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")

        // Cached CAGE data
        val LAST_CAGE_TIME = longPreferencesKey("last_cage_time")
        val LAST_FETCH_TIME = longPreferencesKey("last_fetch_time")
    }

    // Nightscout Settings
    val nightscoutUrl: Flow<String> = context.dataStore.data.map { it[Keys.NIGHTSCOUT_URL] ?: "" }
    val nightscoutApiSecret: Flow<String> = context.dataStore.data.map { it[Keys.NIGHTSCOUT_API_SECRET] ?: "" }

    suspend fun setNightscoutUrl(url: String) {
        context.dataStore.edit { it[Keys.NIGHTSCOUT_URL] = url.trim().trimEnd('/') }
    }

    suspend fun setNightscoutApiSecret(secret: String) {
        context.dataStore.edit { it[Keys.NIGHTSCOUT_API_SECRET] = secret.trim() }
    }

    // Threshold Settings
    // Default: Yellow at 2 days (2880 minutes), Red at 2.5 days (3600 minutes)
    val yellowThresholdMinutes: Flow<Int> = context.dataStore.data.map {
        it[Keys.YELLOW_THRESHOLD_MINUTES] ?: (2 * 24 * 60)  // 2 days
    }

    val redThresholdMinutes: Flow<Int> = context.dataStore.data.map {
        it[Keys.RED_THRESHOLD_MINUTES] ?: (2 * 24 * 60 + 12 * 60)  // 2 days 12 hours
    }

    suspend fun setYellowThreshold(days: Int, hours: Int, minutes: Int) {
        val totalMinutes = days * 24 * 60 + hours * 60 + minutes
        context.dataStore.edit { it[Keys.YELLOW_THRESHOLD_MINUTES] = totalMinutes }
    }

    suspend fun setRedThreshold(days: Int, hours: Int, minutes: Int) {
        val totalMinutes = days * 24 * 60 + hours * 60 + minutes
        context.dataStore.edit { it[Keys.RED_THRESHOLD_MINUTES] = totalMinutes }
    }

    // Notification Settings
    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_ENABLED] = enabled }
    }

    // Cached CAGE data
    val lastCageTime: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_CAGE_TIME] ?: 0L }
    val lastFetchTime: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_FETCH_TIME] ?: 0L }

    suspend fun setCageTime(timestamp: Long) {
        context.dataStore.edit {
            it[Keys.LAST_CAGE_TIME] = timestamp
            it[Keys.LAST_FETCH_TIME] = System.currentTimeMillis()
        }
    }

    // Sync convenience methods
    suspend fun getNightscoutUrlSync(): String = nightscoutUrl.first()
    suspend fun getNightscoutApiSecretSync(): String = nightscoutApiSecret.first().trim()
    suspend fun getYellowThresholdMinutesSync(): Int = yellowThresholdMinutes.first()
    suspend fun getRedThresholdMinutesSync(): Int = redThresholdMinutes.first()
    suspend fun getLastCageTimeSync(): Long = lastCageTime.first()
    suspend fun isNotificationEnabledSync(): Boolean = notificationEnabled.first()

    // Check if Nightscout is configured
    suspend fun isNightscoutConfigured(): Boolean {
        val url = getNightscoutUrlSync()
        return url.isNotBlank()
    }
}

/**
 * Data class representing CAGE status with color coding
 */
data class CageStatus(
    val cageTimeMs: Long,           // Timestamp when cannula was changed
    val ageMinutes: Int,            // Age in minutes
    val color: CageColor,           // Color based on thresholds
    val formattedAge: String        // Formatted as "Xd Xh Xm"
) {
    companion object {
        fun fromTimestamp(
            cageTimeMs: Long,
            yellowThresholdMinutes: Int,
            redThresholdMinutes: Int
        ): CageStatus {
            val ageMs = System.currentTimeMillis() - cageTimeMs
            val ageMinutes = (ageMs / 1000 / 60).toInt()

            val color = when {
                ageMinutes >= redThresholdMinutes -> CageColor.RED
                ageMinutes >= yellowThresholdMinutes -> CageColor.YELLOW
                else -> CageColor.GREEN
            }

            val days = ageMinutes / (24 * 60)
            val hours = (ageMinutes % (24 * 60)) / 60
            val minutes = ageMinutes % 60

            val formattedAge = when {
                days > 0 -> "${days}d ${hours}h ${minutes}m"
                hours > 0 -> "${hours}h ${minutes}m"
                else -> "${minutes}m"
            }

            return CageStatus(
                cageTimeMs = cageTimeMs,
                ageMinutes = ageMinutes,
                color = color,
                formattedAge = formattedAge
            )
        }
    }
}

enum class CageColor {
    GREEN, YELLOW, RED
}
