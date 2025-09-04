package com.osamaalek.kiosklauncher.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

object SecurityUtil {
    
    // Obfuscated shared secret - in production, this should be more sophisticated
    // Consider using Android Keystore or ProGuard obfuscation
    private const val SHARED_SECRET = "kiosk-device-validation-secret-2025-v1"
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Generate HMAC-SHA256 signature for the given payload
     */
    fun generateSignature(payload: String): String {
        return try {
            val secretKeySpec = SecretKeySpec(SHARED_SECRET.toByteArray(UTF_8), HMAC_ALGORITHM)
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(secretKeySpec)
            val signature = mac.doFinal(payload.toByteArray(UTF_8))
            Base64.encodeToString(signature, Base64.NO_WRAP)
        } catch (e: Exception) {
            DebugLogger.logError("Failed to generate signature", e)
            ""
        }
    }
    
    /**
     * Verify HMAC-SHA256 signature
     */
    fun verifySignature(payload: String, signature: String): Boolean {
        return try {
            val expectedSignature = generateSignature(payload)
            secureEquals(expectedSignature, signature)
        } catch (e: Exception) {
            DebugLogger.logError("Failed to verify signature", e)
            false
        }
    }
    
    /**
     * Generate secure random nonce (16 bytes)
     */
    fun generateNonce(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    
    /**
     * Get current timestamp in seconds
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis() / 1000
    }
    
    /**
     * Validate timestamp is within acceptable range (prevents replay attacks)
     */
    fun isTimestampValid(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis() / 1000
        val timeDiff = Math.abs(currentTime - timestamp) * 1000
        return timeDiff <= TIMESTAMP_TOLERANCE_MS
    }
    
    /**
     * Create signed request payload
     */
    fun createSignedPayload(deviceId: String, deviceInfo: String, appVersion: String): SignedPayload {
        val timestamp = getCurrentTimestamp()
        val nonce = generateNonce()
        
        // Create payload string for signing
        val payload = "$deviceId|$deviceInfo|$appVersion|$timestamp|$nonce"
        val signature = generateSignature(payload)
        
        return SignedPayload(
            deviceId = deviceId,
            deviceInfo = deviceInfo,
            appVersion = appVersion,
            timestamp = timestamp,
            nonce = nonce,
            signature = signature
        )
    }
    
    /**
     * Verify signed payload from server response
     */
    fun verifyResponsePayload(responseData: String, timestamp: Long, nonce: String, signature: String): Boolean {
        // Verify timestamp is recent
        if (!isTimestampValid(timestamp)) {
            DebugLogger.log("Response timestamp validation failed")
            return false
        }
        
        // Recreate payload and verify signature
        val payload = "$responseData|$timestamp|$nonce"
        val isValid = verifySignature(payload, signature)
        
        if (!isValid) {
            DebugLogger.log("Response signature verification failed")
        }
        
        return isValid
    }
    
    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private fun secureEquals(a: String, b: String): Boolean {
        if (a.length != b.length) {
            return false
        }
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }
}

data class SignedPayload(
    val deviceId: String,
    val deviceInfo: String,
    val appVersion: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String
)