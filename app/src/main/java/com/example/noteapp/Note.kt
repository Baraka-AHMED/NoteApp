class Note(
    var title: String,
    var content: String,
    var lastModified: String,
    var isDeleted: Boolean = false,
    var isFavorite: Boolean = false

) {
    var id: Int = 0
        private set

    init {
        id = getNextNoteId()
    }

    companion object {
        var nextId = 1

        private fun getNextNoteId(): Int {
            val id = nextId
            nextId++
            return id
        }
    }
}
