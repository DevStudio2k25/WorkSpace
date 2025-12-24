package com.devstudio.workspace.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encryption utility for vault content
 * Uses AES-256 encryption
 */
object EncryptionUtil {
    
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 10000
    private const val IV_LENGTH = 16
    
    /**
     * Encrypt text using AES-256
     */
    fun encrypt(plainText: String, password: String): String {
        try {
            val salt = generateSalt()
            val key = deriveKey(password, salt)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
            
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine salt + iv + encrypted data
            val combined = salt + iv + encryptedBytes
            
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw EncryptionException("Encryption failed: ${e.message}", e)
        }
    }
    
    /**
     * Decrypt text using AES-256
     */
    fun decrypt(encryptedText: String, password: String): String {
        try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            
            // Extract salt, iv, and encrypted data
            val salt = combined.copyOfRange(0, 16)
            val iv = combined.copyOfRange(16, 32)
            val encryptedBytes = combined.copyOfRange(32, combined.size)
            
            val key = deriveKey(password, salt)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw EncryptionException("Decryption failed: ${e.message}", e)
        }
    }
    
    /**
     * Derive encryption key from password using PBKDF2
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
    
    /**
     * Generate random salt
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    /**
     * Generate random IV
     */
    private fun generateIV(): ByteArray {
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        return iv
    }
    
    /**
     * Encrypt file content
     */
    fun encryptFile(fileBytes: ByteArray, password: String): ByteArray {
        try {
            val salt = generateSalt()
            val key = deriveKey(password, salt)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
            
            val encryptedBytes = cipher.doFinal(fileBytes)
            
            // Combine salt + iv + encrypted data
            return salt + iv + encryptedBytes
        } catch (e: Exception) {
            throw EncryptionException("File encryption failed: ${e.message}", e)
        }
    }
    
    /**
     * Decrypt file content
     */
    fun decryptFile(encryptedBytes: ByteArray, password: String): ByteArray {
        try {
            // Extract salt, iv, and encrypted data
            val salt = encryptedBytes.copyOfRange(0, 16)
            val iv = encryptedBytes.copyOfRange(16, 32)
            val encrypted = encryptedBytes.copyOfRange(32, encryptedBytes.size)
            
            val key = deriveKey(password, salt)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            
            return cipher.doFinal(encrypted)
        } catch (e: Exception) {
            throw EncryptionException("File decryption failed: ${e.message}", e)
        }
    }
}

class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
