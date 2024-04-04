package com.example.noteapp
class Note(
    var id: Int = 0,
    var title: String,
    var content: String,
    var lastModified: String,
    var isDeleted: Boolean = false
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

