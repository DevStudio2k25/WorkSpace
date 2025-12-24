package com.devstudio.workspace.data.dao

import androidx.room.*
import com.devstudio.workspace.data.model.VaultItem
import com.devstudio.workspace.data.model.VaultItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>
    
    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getVaultItemById(id: Long): VaultItem?
    
    @Query("SELECT * FROM vault_items WHERE itemType = :type ORDER BY updatedAt DESC")
    fun getVaultItemsByType(type: VaultItemType): Flow<List<VaultItem>>
    
    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%'")
    fun searchVaultItems(query: String): Flow<List<VaultItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long
    
    @Update
    suspend fun updateVaultItem(item: VaultItem)
    
    @Delete
    suspend fun deleteVaultItem(item: VaultItem)
    
    @Query("DELETE FROM vault_items")
    suspend fun deleteAllVaultItems()
}
