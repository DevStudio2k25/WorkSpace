package com.devstudio.workspace.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Public Note Entity - Visible to everyone
 * This is what makes the app look like a normal notes app
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val color: Int = 0, // For note colors
    val category: String = "General",
    
    // Hidden flag - this note is the gateway to vault
    // Only one note should have this flag = true
    val isGateway: Boolean = false
)
