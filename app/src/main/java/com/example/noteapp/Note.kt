package com.example.noteapp
class Note(
    var id: Int = 0,
    val title: String,
    val content: String,
    val lastModified: String
) {
    companion object {
        private var nextId = 1

        private fun getNextNoteId(): Int {
            return nextId++
        }
    }

    init {
        if (id == 0) {
            id = getNextNoteId()
        }
    }
}

