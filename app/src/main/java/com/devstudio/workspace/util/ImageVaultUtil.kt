package com.devstudio.workspace.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for handling image operations in vault
 */
object ImageVaultUtil {
    
    /**
     * Copy image from device to app's private storage and encrypt
     */
    fun copyAndEncryptImage(
        context: Context,
        sourceUri: Uri,
        encryptionPassword: String
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            // Encrypt image data
            val encryptedData = EncryptionUtil.encryptFile(imageBytes, encryptionPassword)
            
            // Save to private storage
            val fileName = "img_${System.currentTimeMillis()}.enc"
            val vaultDir = File(context.filesDir, "vault_images")
            if (!vaultDir.exists()) {
                vaultDir.mkdirs()
            }
            
            val encryptedFile = File(vaultDir, fileName)
            encryptedFile.writeBytes(encryptedData)
            
            encryptedFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Decrypt and load image from vault
     */
    fun decryptImage(
        encryptedFilePath: String,
        encryptionPassword: String
    ): ByteArray? {
        return try {
            val encryptedFile = File(encryptedFilePath)
            if (!encryptedFile.exists()) return null
            
            val encryptedData = encryptedFile.readBytes()
            EncryptionUtil.decryptFile(encryptedData, encryptionPassword)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Delete encrypted image file
     */
    fun deleteEncryptedImage(encryptedFilePath: String): Boolean {
        return try {
            val file = File(encryptedFilePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get all encrypted images in vault
     */
    fun getAllEncryptedImages(context: Context): List<File> {
        val vaultDir = File(context.filesDir, "vault_images")
        if (!vaultDir.exists()) return emptyList()
        
        return vaultDir.listFiles()?.filter { it.extension == "enc" } ?: emptyList()
    }
}
