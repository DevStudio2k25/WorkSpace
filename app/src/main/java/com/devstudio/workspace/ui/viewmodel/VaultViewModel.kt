package com.devstudio.workspace.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.devstudio.workspace.data.database.AppDatabase
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val vaultItemDao = database.vaultItemDao()
    
    private val _vaultItems = MutableStateFlow<List<VaultItem>>(emptyList())
    val vaultItems: StateFlow<List<VaultItem>> = _vaultItems
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadVaultItems()
    }
    
    private fun loadVaultItems() {
        viewModelScope.launch {
            vaultItemDao.getAllVaultItems().collect { items ->
                _vaultItems.value = items
            }
        }
    }
    
    /**
     * Hide multiple images securely
     * 1. Create encrypted file in vault
     * 2. Encrypt and copy content
     * 3. Delete original from gallery
     */
    fun hideImages(context: Context, uris: List<Uri>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            var successCount = 0
            
            withContext(Dispatchers.IO) {
                // Get or create MasterKey
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                uris.forEach { uri ->
                    try {
                        val fileName = getFileNameFromUri(context, uri)
                        
                        // 1. Create Vault File
                        val vaultDir = File(context.filesDir, "vault")
                        if (!vaultDir.exists()) {
                            vaultDir.mkdirs()
                        }
                        
                        val encryptedFileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.enc"
                        val vaultFile = File(vaultDir, encryptedFileName)
                        
                        // 2. Generate Thumbnail (Open stream #1)
                        var thumbnailPath: String? = null
                        try {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                // Decode bounds only first if needed, but for simplicity just decode
                                // For better performance on large images, one should use inSampleSize
                                val options = android.graphics.BitmapFactory.Options()
                                options.inJustDecodeBounds = true
                                android.graphics.BitmapFactory.decodeStream(input, null, options)
                            }
                            
                            // Re-open to decode with sample size
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                val options = android.graphics.BitmapFactory.Options()
                                options.inSampleSize = 4 // Rough downsampling to save memory
                                val bitmap = android.graphics.BitmapFactory.decodeStream(input, null, options)
                                
                                if (bitmap != null) {
                                    val thumbDir = File(context.filesDir, "thumbnails")
                                    if (!thumbDir.exists()) thumbDir.mkdirs()
                                    
                                    val thumbFile = File(thumbDir, "thumb_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(thumbFile).use { out ->
                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
                                    }
                                    thumbnailPath = thumbFile.absolutePath
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // 3. Setup EncryptedFile
                        val encryptedFile = EncryptedFile.Builder(
                            context,
                            vaultFile,
                            masterKey,
                            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                        ).build()
                        
                        // 4. Encrypt and Save (Open stream #2 or #3)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        
                        if (inputStream != null) {
                            inputStream.use { input ->
                                encryptedFile.openFileOutput().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            
                            // 5. Verify and Delete
                            if (vaultFile.exists() && vaultFile.length() > 0) {
                                // Delete from Gallery
                                try {
                                    var deleted = false
                                    // Try DocumentsContract if it's a document URI
                                    if (android.provider.DocumentsContract.isDocumentUri(context, uri)) {
                                        try {
                                            deleted = android.provider.DocumentsContract.deleteDocument(context.contentResolver, uri)
                                        } catch (e: Exception) {
                                            // Fallback to standard delete if this fails
                                        }
                                    }
                                    
                                    if (!deleted) {
                                        context.contentResolver.delete(uri, null, null)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                // 6. Save Metadata
                                val vaultItem = VaultItem(
                                    title = fileName,
                                    content = "Secure Encrypted Image",
                                    itemType = VaultItemType.IMAGE,
                                    encryptedFilePath = vaultFile.absolutePath,
                                    originalFileName = fileName,
                                    fileSize = vaultFile.length(),
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    thumbnailPath = thumbnailPath
                                )
                                
                                vaultItemDao.insertVaultItem(vaultItem)
                                successCount++
                            } else {
                                vaultFile.delete()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            _isLoading.value = false
            onComplete(successCount)
        }
    }
    
    /**
     * Unhide an image
     * Decrypts to the public Pictures/Workspace directory
     */
    fun unhideImage(context: Context, vaultItem: VaultItem, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val encryptedPath = vaultItem.encryptedFilePath ?: return@withContext Pair(false, "Invalid file path")
                    val encryptedFileObj = File(encryptedPath)
                    
                    if (!encryptedFileObj.exists()) {
                        return@withContext Pair(false, "File not found in vault")
                    }
                    
                    // Get MasterKey
                    val masterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                        
                    // 1. Setup EncryptedFile for reading
                    val encryptedFile = EncryptedFile.Builder(
                        context,
                        encryptedFileObj,
                        masterKey,
                        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                    ).build()
                    
                    // 2. Prepare Output File in Gallery
                     val picturesDir = File(
                        android.os.Environment.getExternalStoragePublicDirectory(
                            android.os.Environment.DIRECTORY_PICTURES
                        ),
                        "Workspace"
                    )
                    if (!picturesDir.exists()) {
                        picturesDir.mkdirs()
                    }
                    
                    val restoredFileName = vaultItem.originalFileName ?: "restored_${System.currentTimeMillis()}.jpg"
                    val restoredFile = File(picturesDir, restoredFileName)
                    
                    // 3. Decrypt and Copy
                    encryptedFile.openFileInput().use { input ->
                        FileOutputStream(restoredFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    // 4. Cleanup Vault
                    if (restoredFile.exists() && restoredFile.length() > 0) {
                        encryptedFileObj.delete()
                        vaultItemDao.deleteVaultItem(vaultItem)
                        
                        // Notify Scanner
                        // Scan the new file
                         android.media.MediaScannerConnection.scanFile(
                            context,
                            arrayOf(restoredFile.absolutePath),
                            null
                        ) { _, _ -> }

                        Pair(true, "Restored to Pictures/Workspace")
                    } else {
                         Pair(false, "Decryption failed")
                    }
                }
                
                onComplete(result.first, result.second)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete a vault item permanently (files + db)
     */
    fun deleteVaultItem(vaultItem: VaultItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                vaultItem.encryptedFilePath?.let { path ->
                    File(path).delete()
                }
                vaultItemDao.deleteVaultItem(vaultItem)
            }
        }
    }
    
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = "image_${System.currentTimeMillis()}.jpg"
        
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    if (name != null) fileName = name
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return fileName
    }
}
