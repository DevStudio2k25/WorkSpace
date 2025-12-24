package com.devstudio.workspace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Document Entity - For PDFs, TXT, DOCX files
 * Public layer documents
 */
@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val filePath: String, // Internal storage path
    val fileType: String, // pdf, txt, docx, etc.
    val fileSize: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long? = null,
    val isFavorite: Boolean = false,
    val thumbnailPath: String? = null
)
