package com.devstudio.workspace.data.dao

import androidx.room.*
import com.devstudio.workspace.data.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY addedAt DESC")
    fun getAllDocuments(): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?
    
    @Query("SELECT * FROM documents WHERE fileType = :type ORDER BY addedAt DESC")
    fun getDocumentsByType(type: String): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavoriteDocuments(): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE name LIKE '%' || :query || '%'")
    fun searchDocuments(query: String): Flow<List<Document>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long
    
    @Update
    suspend fun updateDocument(document: Document)
    
    @Delete
    suspend fun deleteDocument(document: Document)
    
    @Query("UPDATE documents SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: Long, timestamp: Long)
    
    @Query("UPDATE documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
}
