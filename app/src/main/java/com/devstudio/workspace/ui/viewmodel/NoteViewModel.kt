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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.devstudio.workspace.data.ai.OpenRouterService
import com.devstudio.workspace.util.SecurePreferences

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: NoteRepository
    
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // AI State
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()
    
    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()
    
    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()
    
    private val securePrefs by lazy { SecurePreferences(getApplication()) }
    
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
    
    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note)
            }
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
    
    // AI Functions
    fun clearAiState() {
        _aiResponse.value = null
        _aiError.value = null
        _aiLoading.value = false
    }
    
    fun generateAiContent(userMessage: String, noteContext: String = "") {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            
            try {
                // Get constraints
                val apiKey = securePrefs.aiApiKey.first()
                val model = securePrefs.aiModel.first()
                val language = securePrefs.aiLanguage.first()
                
                if (apiKey.isBlank() || model.isBlank()) {
                    _aiError.value = "AI is not configured. Please check Settings."
                    _aiLoading.value = false
                    return@launch
                }
                
                val contextStructure = """
                    CTX_START
                    CURRENT_NOTE_CONTENT:
                    "$noteContext"
                    IS_EMPTY: ${noteContext.isBlank()}
                    CTX_END
                """.trimIndent()
                
                val systemPrompt = com.devstudio.workspace.data.ai.AiRules.getSystemPrompt(language) + "\n\n" + contextStructure
                
                val result = OpenRouterService.generateCompletion(
                    apiKey = apiKey,
                    model = model,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage
                )
                
                result.fold(
                    onSuccess = { response -> 
                        _aiResponse.value = response
                    },
                    onFailure = { error ->
                        _aiError.value = error.message ?: "Unknown AI Error"
                    }
                )
            } catch (e: Exception) {
                _aiError.value = e.message
            } finally {
                _aiLoading.value = false
            }
        }
    }
}
