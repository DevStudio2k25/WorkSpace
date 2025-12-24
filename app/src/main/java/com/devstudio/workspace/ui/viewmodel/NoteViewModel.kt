package com.devstudio.workspace.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devstudio.workspace.data.database.AppDatabase
import com.devstudio.workspace.data.model.Note
import com.devstudio.workspace.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    init {
        val database = AppDatabase.getInstance(application)
        repository = NoteRepository(database.noteDao())
        loadNotes()
    }
    
    fun loadNotes() {
        viewModelScope.launch {
            repository.getAllNotes().collect { notesList ->
                _notes.value = notesList
            }
        }
    }
    
    fun searchNotes(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadNotes()
            } else {
                repository.searchNotes(query).collect { notesList ->
                    _notes.value = notesList
                }
            }
        }
    }
    
    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                loadNotes()
            } else {
                repository.getNotesByCategory(category).collect { notesList ->
                    _notes.value = notesList
                }
            }
        }
    }
    
    fun insertNote(note: Note) {
        viewModelScope.launch {
            repository.insertNote(note)
        }
    }
    
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }
    
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }
    
    fun getGatewayNote(): Note? {
        return _notes.value.firstOrNull { it.isGateway }
    }
    
    fun createGatewayNote() {
        viewModelScope.launch {
            repository.createGatewayNote()
        }
    }
}
