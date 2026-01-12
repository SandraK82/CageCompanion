package com.cagecompanion.network

import android.util.Log
import com.cagecompanion.data.PreferencesManager
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class NightscoutApi(private val preferencesManager: PreferencesManager) {

    companion object {
        private const val TAG = "NightscoutApi"
        private const val DEVICE_NAME = "CageCompanion"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Prepare API secret for Nightscout authentication.
     * - Nightscout tokens (admin-xxx, readable-xxx, etc.) are sent as-is
     * - Already hashed SHA-1 values (40 hex chars) are sent as-is
     * - Raw passwords are hashed with SHA-1
     */
    private fun prepareApiSecret(secret: String): String {
        if (secret.isBlank()) return ""

        // Nightscout tokens (admin-xxx, readable-xxx, etc.) - don't hash
        if (secret.contains("-") && secret.split("-").size == 2) {
            val prefix = secret.split("-")[0].lowercase()
            if (prefix in listOf("admin", "readable", "reader", "denied", "device", "food")) {
                return secret
            }
        }

        // Already a 40-char SHA-1 hash - don't hash again
        if (secret.length == 40 && secret.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            return secret
        }

        // Hash raw password with SHA-1
        val bytes = MessageDigest.getInstance("SHA-1").digest(secret.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Test connection to Nightscout
     */
    suspend fun testConnection(): Result<Boolean> {
        return try {
            val url = preferencesManager.getNightscoutUrlSync()
            val apiSecret = preferencesManager.getNightscoutApiSecretSync()

            if (url.isBlank()) {
                return Result.failure(Exception("URL not configured"))
            }

            val response = client.get("$url/api/v1/status") {
                if (apiSecret.isNotBlank()) {
                    header("api-secret", prepareApiSecret(apiSecret))
                }
            }

            Result.success(response.status.isSuccess())
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch the latest Site Change (CAGE) treatment from Nightscout
     * Returns the timestamp of the last cannula change
     */
    suspend fun getLatestCage(): Result<Long?> {
        return try {
            val url = preferencesManager.getNightscoutUrlSync()
            val apiSecret = preferencesManager.getNightscoutApiSecretSync()

            if (url.isBlank()) {
                return Result.failure(Exception("Nightscout URL not configured"))
            }

            Log.d(TAG, "Fetching CAGE from $url")
            val preparedSecret = prepareApiSecret(apiSecret)
            Log.d(TAG, "API Secret length: ${apiSecret.length}, prepared length: ${preparedSecret.length}, first chars: ${apiSecret.take(10)}...")

            // Calculate date 30 days ago for filter
            val thirtyDaysAgo = java.time.Instant.now().minus(java.time.Duration.ofDays(30)).toString()

            val response = client.get("$url/api/v1/treatments") {
                parameter("find[eventType]", "Site Change")
                parameter("find[created_at][\$gte]", thirtyDaysAgo)
                parameter("count", "1")

                if (preparedSecret.isNotBlank()) {
                    header("api-secret", preparedSecret)
                }
            }

            if (!response.status.isSuccess()) {
                Log.e(TAG, "Failed to fetch CAGE: ${response.status}")
                return Result.failure(Exception("HTTP ${response.status.value}"))
            }

            val responseText = response.bodyAsText()
            Log.d(TAG, "CAGE response: $responseText")

            val treatments = json.decodeFromString<List<NightscoutTreatment>>(responseText)

            if (treatments.isEmpty()) {
                Log.d(TAG, "No CAGE treatments found")
                return Result.success(null)
            }

            val latestCage = treatments.first()
            val timestamp = latestCage.mills ?: parseTimestamp(latestCage.created_at)

            Log.d(TAG, "Latest CAGE: ${latestCage.created_at}, timestamp: $timestamp")

            Result.success(timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching CAGE", e)
            Result.failure(e)
        }
    }

    /**
     * Upload a new Site Change treatment to Nightscout
     */
    suspend fun uploadCannulaChange(): Result<String> {
        return try {
            val url = preferencesManager.getNightscoutUrlSync()
            val apiSecret = preferencesManager.getNightscoutApiSecretSync()

            if (url.isBlank()) {
                return Result.failure(Exception("Nightscout URL not configured"))
            }

            if (apiSecret.isBlank()) {
                return Result.failure(Exception("API Secret not configured"))
            }

            val now = System.currentTimeMillis()
            val treatment = NightscoutTreatment(
                eventType = "Site Change",
                created_at = formatTimestamp(now),
                mills = now,
                enteredBy = DEVICE_NAME
            )

            val treatmentJson = json.encodeToString(NightscoutTreatment.serializer(), treatment)
            Log.d(TAG, "Uploading CAGE treatment: $treatmentJson")

            val response = client.post("$url/api/v1/treatments") {
                contentType(ContentType.Application.Json)
                header("api-secret", prepareApiSecret(apiSecret))
                setBody(treatmentJson)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e(TAG, "Failed to upload CAGE: ${response.status} - $errorBody")
                return Result.failure(Exception("HTTP ${response.status.value}"))
            }

            Log.d(TAG, "CAGE uploaded successfully")
            Result.success("OK")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading CAGE", e)
            Result.failure(e)
        }
    }

    /**
     * Parse ISO timestamp to milliseconds
     */
    private fun parseTimestamp(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        return try {
            java.time.Instant.parse(isoString).toEpochMilli()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse timestamp: $isoString")
            null
        }
    }

    /**
     * Format timestamp to ISO string
     */
    private fun formatTimestamp(millis: Long): String {
        return java.time.Instant.ofEpochMilli(millis).toString()
    }
}

@Serializable
data class NightscoutTreatment(
    val eventType: String? = null,
    val created_at: String? = null,
    val mills: Long? = null,
    val enteredBy: String? = null
)
