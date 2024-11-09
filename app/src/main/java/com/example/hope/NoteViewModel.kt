package com.example.hope

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class NoteViewModel(
    private val dao: NoteDao
): ViewModel() {

    private val _notes = dao.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    @RequiresApi(Build.VERSION_CODES.O)
    private val _state = MutableStateFlow(NoteState())

    @RequiresApi(Build.VERSION_CODES.O)
    val state = combine(_state, _notes) { state, notes ->
        state.copy(
            notes = notes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NoteState())

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: NoteEvent) {
        when(event) {
            is NoteEvent.SetContent -> {
                _state.update { it.copy(
                    content = event.content
                ) }
            }
            is NoteEvent.SetDate -> {
                _state.update { it.copy(
                    date = event.date
                ) }
            }
            NoteEvent.ShowDialog -> {
                _state.update { it.copy(
                    isAddingNote = true
                ) }
            }
            NoteEvent.HideDialog -> {
                _state.update { it.copy(
                    isAddingNote = false
                ) }
            }
            NoteEvent.SaveNote -> {
                val content = state.value.content
                val date = state.value.date
                if(content.isBlank()) {
                    return
                }
                val note = Note(
                    content = content,
                    date = date
                )
                viewModelScope.launch {
                    dao.insertNote(note)
                }
                _state.update { it.copy(
                    isAddingNote = false,
                    content = ""
                ) }
            }
        }
    }

    fun check(date: LocalDate): Boolean {
        // Kiểm tra nếu ngày đã tồn tại trong danh sách _notes
        return _notes.value.any { it.date == date }
    }

}