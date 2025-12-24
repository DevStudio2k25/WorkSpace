package com.devstudio.workspace.data.repository

import com.devstudio.workspace.data.dao.VaultDao
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import com.devstudio.workspace.util.EncryptionUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for encrypted vault items
 * Handles encryption/decryption automatically
 */
class VaultRepository(
    private val vaultDao: VaultDao,
    private val encryptionPassword: String
) {
    
    fun getAllVaultItems(): Flow<List<VaultItem>> = 
        vaultDao.getAllVaultItems().map { items ->
            items.map { decryptVaultItem(it) }
        }
    
    suspend fun getVaultItemById(id: Long): VaultItem? {
        val item = vaultDao.getVaultItemById(id)
        return item?.let { decryptVaultItem(it) }
    }
    
    fun getVaultItemsByType(type: VaultItemType): Flow<List<VaultItem>> =
        vaultDao.getVaultItemsByType(type).map { items ->
            items.map { decryptVaultItem(it) }
        }
    
    fun searchVaultItems(query: String): Flow<List<VaultItem>> =
        vaultDao.searchVaultItems(query).map { items ->
            items.map { decryptVaultItem(it) }
        }
    
    suspend fun insertVaultItem(item: VaultItem): Long {
        val encryptedItem = encryptVaultItem(item)
        return vaultDao.insertVaultItem(encryptedItem)
    }
    
    suspend fun updateVaultItem(item: VaultItem) {
        val encryptedItem = encryptVaultItem(item)
        vaultDao.updateVaultItem(encryptedItem)
    }
    
    suspend fun deleteVaultItem(item: VaultItem) = vaultDao.deleteVaultItem(item)
    
    suspend fun deleteAllVaultItems() = vaultDao.deleteAllVaultItems()
    
    /**
     * Encrypt vault item content
     */
    private fun encryptVaultItem(item: VaultItem): VaultItem {
        return item.copy(
            title = EncryptionUtil.encrypt(item.title, encryptionPassword),
            content = EncryptionUtil.encrypt(item.content, encryptionPassword),
            metadata = item.metadata?.let { 
                EncryptionUtil.encrypt(it, encryptionPassword) 
            }
        )
    }
    
    /**
     * Decrypt vault item content
     */
    private fun decryptVaultItem(item: VaultItem): VaultItem {
        return try {
            item.copy(
                title = EncryptionUtil.decrypt(item.title, encryptionPassword),
                content = EncryptionUtil.decrypt(item.content, encryptionPassword),
                metadata = item.metadata?.let { 
                    EncryptionUtil.decrypt(it, encryptionPassword) 
                }
            )
        } catch (e: Exception) {
            // If decryption fails, return item as-is (wrong password)
            item
        }
    }
}
