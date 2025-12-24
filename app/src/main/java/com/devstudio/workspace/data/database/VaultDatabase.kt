package com.devstudio.workspace.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devstudio.workspace.data.dao.VaultDao
import com.devstudio.workspace.data.model.VaultItem
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Vault Database - Encrypted with SQLCipher
 * This is the hidden layer, never visible in normal usage
 * Password is derived from gesture pattern
 */
@Database(
    entities = [VaultItem::class],
    version = 1,
    exportSchema = false
)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    
    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null
        
        fun getInstance(context: Context, passphrase: String): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphraseBytes = SQLiteDatabase.getBytes(passphrase.toCharArray())
                val factory = SupportFactory(passphraseBytes)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vault_db"
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                    
                INSTANCE = instance
                instance
            }
        }
        
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
