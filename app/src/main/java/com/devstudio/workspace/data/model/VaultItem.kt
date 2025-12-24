package com.devstudio.workspace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Secure Vault Item - Encrypted storage
 * This is stored in a separate encrypted database
 * Never visible in normal app usage
 */
@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String, // Encrypted content
    val itemType: VaultItemType,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val filePath: String? = null, // For encrypted files
    val fileSize: Long? = null,
    val thumbnailPath: String? = null,
    val metadata: String? = null // JSON metadata
)

enum class VaultItemType {
    NOTE,
    DOCUMENT,
    IMAGE,
    VIDEO,
    AUDIO,
    OTHER
}
