
// Classe note qui permet de stocker une note
class Note(
    var title: String, // Titre de la note
    var content: String, // Contenu de la note
    var lastModified: String, // Dernière date de modification
    var isDeleted: Boolean = false, // La note est elle dans la corbeille ?
    var isFavorite: Boolean = false // La note est elle dans les favoris

) {
    // Initialisation de l'ID qui s'incrémente à chaque instance
    var id: Int = 0
        private set
    init {
        id = getNextNoteId()
    }
    companion object {
        var nextId = 1
        // Fonction qui permet de reinitialiser la note à chaque instance
        private fun getNextNoteId(): Int {
            val id = nextId
            nextId++
            return id
        }
    }
}
