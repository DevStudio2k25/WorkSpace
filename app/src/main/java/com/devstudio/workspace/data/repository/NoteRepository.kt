package com.devstudio.workspace.data.repository

import com.devstudio.workspace.data.dao.NoteDao
import com.devstudio.workspace.data.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository for public notes
 */
class NoteRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    
    suspend fun getGatewayNote(): Note? = noteDao.getGatewayNote()
    
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    fun getNotesByCategory(category: String): Flow<List<Note>> = 
        noteDao.getNotesByCategory(category)
    
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
    
    suspend fun togglePinStatus(id: Long, isPinned: Boolean) = 
        noteDao.updatePinStatus(id, isPinned)
    
    /**
     * Create the gateway note (empty note that unlocks vault)
     */
    suspend fun createGatewayNote(): Long {
        val gatewayNote = Note(
            title = "",
            content = "",
            isGateway = true,
            category = "Personal"
        )
        return insertNote(gatewayNote)
    }
}
