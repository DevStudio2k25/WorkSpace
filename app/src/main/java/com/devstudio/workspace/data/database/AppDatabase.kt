package com.devstudio.workspace.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devstudio.workspace.data.dao.DocumentDao
import com.devstudio.workspace.data.dao.NoteDao
import com.devstudio.workspace.data.model.Document
import com.devstudio.workspace.data.model.Note

/**
 * Public Database - Normal, unencrypted
 * This is what everyone sees
 */
@Database(
    entities = [Note::class, Document::class, com.devstudio.workspace.data.model.VaultItem::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun documentDao(): DocumentDao
    abstract fun vaultItemDao(): com.devstudio.workspace.data.dao.VaultItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workspace_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
