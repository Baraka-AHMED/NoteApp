package com.example.noteapp

import Note
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fonctionnalités de base
 * Ajout
 * Suppression
 * Modification
 * SharedPreferences
 *
 * Fonctionnalités ajoutées
 * Trie par date de mise à jour
 * Recherche
 * Favoris
 * Reinitialiser les notes
 * Menu pour afficher les notes par catégorie
 * Adaption du thème de l'application en fonction du theme du téléphone
 * Création du logo de l'application
 */
class MainActivity : AppCompatActivity() {

    private lateinit var notes: ArrayList<Note>
    private lateinit var notesNotDeleted: List<Note>

    private lateinit var recyclerView: RecyclerView
    private lateinit var noteAdapter: NoteAdapter

    private lateinit var searchText: EditText
    private lateinit var btnAnnuller : ImageButton

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private var currentView: CurrentView = CurrentView.ALL_NOTES

    private lateinit var navigationView: NavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configure la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_ouvrir, R.string.navigation_fermer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        notes = ArrayList()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        noteAdapter = NoteAdapter(notes, { applyCurrentViewFilter() }, { noteId -> deleteNotePermanently(noteId) }) // Cette méthode mettra à jour l'affichage basé sur le filtrage actuel

        recyclerView.adapter = noteAdapter

        val fab: ImageButton = findViewById(R.id.fab)

        val btnTrie: ImageButton = findViewById(R.id.btn_trie)

        // Variable pour suivre l'état du tri
        var isSortedDescending = false

        // Définition du gestionnaire d'événements pour le bouton de tri
        btnTrie.setOnClickListener {
            // Tri des notes en fonction de la vue actuelle et de l'ordre de tri
            val sortedNotes = when (currentView) {
                // Si la vue actuelle est "Toutes les notes"
                CurrentView.ALL_NOTES -> {
                    // Si l'ordre de tri est décroissant
                    if (!isSortedDescending) {
                        // Filtrage des notes non supprimées et tri par date de dernière modification décroissante
                        notes.filter { !it.isDeleted }.sortedByDescending { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    } else {
                        // Filtrage des notes non supprimées et tri par date de dernière modification croissante
                        notes.filter { !it.isDeleted }.sortedBy { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    }
                }
                // Si la vue actuelle est "Favoris"
                CurrentView.FAVORITES -> {
                    // Si l'ordre de tri est décroissant
                    if (!isSortedDescending) {
                        // Filtrage des notes favorites non supprimées et tri par date de dernière modification décroissante
                        notes.filter { it.isFavorite && !it.isDeleted }.sortedByDescending { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    } else {
                        // Filtrage des notes favorites non supprimées et tri par date de dernière modification croissante
                        notes.filter { it.isFavorite && !it.isDeleted }.sortedBy { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    }
                }
                // Si la vue actuelle est "Corbeille"
                CurrentView.TRASH -> {
                    // Si l'ordre de tri est décroissant
                    if (!isSortedDescending) {
                        // Filtrage des notes supprimées et tri par date de dernière modification décroissante
                        notes.filter { it.isDeleted }.sortedByDescending { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    } else {
                        // Filtrage des notes supprimées et tri par date de dernière modification croissante
                        notes.filter { it.isDeleted }.sortedBy { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastModified) }
                    }
                }
            }
            // Affichage d'un message à l'utilisateur pour l'informer du changement de tri
            if (isSortedDescending) {
                Toast.makeText(this, "Les notes sont triées par ordre décroissant.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Les notes sont triées par ordre croissant.", Toast.LENGTH_SHORT).show()
            }

            // Change l'icône du bouton en fonction de l'état de tri
            if (isSortedDescending) {
                btnTrie.setImageResource(R.drawable.ic_expand_more)
            } else {
                btnTrie.setImageResource(R.drawable.ic_expand_less)
            }

            // Inversion de l'ordre de tri pour le prochain clic sur le bouton de tri
            isSortedDescending = !isSortedDescending

            // Mise à jour des notes dans l'adaptateur avec les notes triées
            noteAdapter.updateNotes(ArrayList(sortedNotes))
        }

        fab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            when (currentView) {
                CurrentView.ALL_NOTES -> {
                    intent.putExtra("noteIsFavorite", "noteNotFavorite") // La note n'est pas un favori
                }
                CurrentView.FAVORITES -> {
                    intent.putExtra("noteIsFavorite", "noteIsFavorite") // La note est un favori
                }
                CurrentView.TRASH -> {
                    // Vous pouvez ajouter une logique spécifique pour la corbeille si nécessaire
                }
            }
            startActivityForResult(intent, ADD_NOTE_REQUEST)
        }


        searchText = findViewById(R.id.search_text)
        val noMatchMessage = "Aucune note ne correspond à votre recherche."
        var isToastDisplayed = false // vérifie si le toast est déja affiché ou pas

        // Configuration de la barre de recherche pour filtrer les notes.
        searchText.addTextChangedListener { s ->
            val searchQuery = s.toString()
            val filteredNotes = filterNotesBasedOnCurrentView(searchQuery) // Filtre les notes en fonction de la vue actuellement sélectionnée et de la recherche
            if (searchQuery.isNotEmpty()) {
                if (filteredNotes.isEmpty() && !isToastDisplayed) {
                    Toast.makeText(this, noMatchMessage, Toast.LENGTH_SHORT).show() // Affiche un Toast indiquant qu'aucune note ne correspond à la recherche
                    isToastDisplayed = true // Marque que le Toast est affiché
                } else {
                    isToastDisplayed = false // Réinitialise la variable indiquant que le Toast a été affiché
                }
                noteAdapter.updateNotes(filteredNotes as ArrayList<Note>)
            } else {
                applyCurrentViewFilter()
            }
        }
        // Réinitialisation de la barre de recherche lors de l'appui sur le bouton annuler.
        btnAnnuller = findViewById(R.id.btn_annuler)
        btnAnnuller.setOnClickListener {
            searchText.setText("")
            recyclerView.adapter = noteAdapter
        }

        // Configuration de la navigation en fonction de la vue actuelle
        navigationView = findViewById(R.id.nav_view)
        // Définition du gestionnaire d'événements pour les éléments de menu de la vue de navigation
        navigationView.setNavigationItemSelectedListener { menuItem ->
            currentView = when (menuItem.itemId) {
                // Mise à jour de la vue actuelle en fonction de l'élément de menu sélectionné
                R.id.nav_accueil -> { fab.visibility = View.VISIBLE
                    supportActionBar?.title = "NoteApp"  // Mettre à jour le titre de la Toolbar
                    CurrentView.ALL_NOTES
                }
                R.id.nav_favoris -> {
                    fab.visibility = View.VISIBLE
                    supportActionBar?.title = "Favoris"  // Mettre à jour le titre de la Toolbar
                    CurrentView.FAVORITES
                }
                R.id.nav_corbeille -> {
                    fab.visibility = View.INVISIBLE
                    supportActionBar?.title = "Corbeille"  // Mettre à jour le titre de la Toolbar
                    CurrentView.TRASH
                }
                R.id.nav_reinitialiser -> {
                    AlertDialog.Builder(this@MainActivity).apply {
                        setTitle("Confirmer la suppression")
                        setMessage("Êtes-vous sûr de vouloir supprimer toutes les notes ? Cette action est irréversible.")
                        setPositiveButton("Valider") { _, _ ->
                            clearAllNotesAsync()  // Appeler la version asynchrone pour éviter le blocage UI
                        }
                        setNegativeButton("Annuler", null)
                        show()
                    }
                    true // Indique que l'événement de menu est traité
                    currentView
                }
                // Si aucun des éléments de menu ci-dessus n'est sélectionné, conserver la vue actuelle
                else -> currentView
            }
            searchText.setText("") // Réinitialiser la barre de recherche
            applyCurrentViewFilter() // Applique le filtre de vue actuel
            drawerLayout.closeDrawer(GravityCompat.START) // Fermer le menu
            true
        }
    }

    // Applique un filtre basé sur la vue actuellement sélectionnée.
    private fun applyCurrentViewFilter() {
        val filteredNotes = filterNotesBasedOnCurrentView(searchText.text.toString())
        noteAdapter.updateNotes(filteredNotes as ArrayList<Note>)
    }

    // Cette fonction filtre les notes en fonction de la vue actuelle et de la requête de recherche
    private fun filterNotesBasedOnCurrentView(searchQuery: String): List<Note> {
        // Retourne une liste de notes filtrée en fonction de la vue actuelle
        return when (currentView) {
            // Si la vue actuelle est "Toutes les notes", retourne toutes les notes non supprimées qui contiennent la requête de recherche dans le titre ou le contenu
            CurrentView.ALL_NOTES -> notes.filter { !it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
            // Si la vue actuelle est "Favoris", retourne toutes les notes favorites non supprimées qui contiennent la requête de recherche dans le titre ou le contenu
            CurrentView.FAVORITES -> notes.filter { it.isFavorite && !it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
            // Si la vue actuelle est "Corbeille", retourne toutes les notes supprimées qui contiennent la requête de recherche dans le titre ou le contenu
            CurrentView.TRASH -> notes.filter { it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
        }
    }

    //Resultats renvoyées par AddNoteActivty
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_NOTE_REQUEST -> {
                    val noteTitle = data?.getStringExtra("noteTitle")
                    val noteContent = data?.getStringExtra("noteContent")
                    val noteIsFavorite = data?.getBooleanExtra("noteIsFavorite",false)

                    if (!noteTitle.isNullOrEmpty() || !noteContent.isNullOrEmpty()) {
                        val note = Note(
                            title = noteTitle ?: "",
                            content = noteContent ?: "",
                            lastModified = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()) ,
                            isFavorite = noteIsFavorite ?: false
                        )
                        notes.add(note)
                        noteAdapter.notifyDataSetChanged( )
                        saveNotes()
                        Toast.makeText(this, "Note ajoutée avec succès", Toast.LENGTH_SHORT).show()
                    }
                }
                EDIT_NOTE_REQUEST -> {
                    val noteId = data?.getIntExtra("noteId", -1)
                    val noteTitle = data?.getStringExtra("noteTitle")
                    val noteContent = data?.getStringExtra("noteContent")
                    val noteIsFavorite = data?.getBooleanExtra("noteIsFavorite", false)  // Correction de la clé ici
                    if (noteId != null && noteId != -1) {
                        val note = notes.find { it.id == noteId }
                        val n = note?.id
                        if (note != null) {
                            note.title = noteTitle ?: note.title
                            note.content = noteContent ?: note.content
                            //Si le titre ou le contenu a été mofifié on change lastModified
                            if (note.title != noteTitle || note.content != noteContent) {
                                note.lastModified = SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date())
                            }
                            note.isFavorite = noteIsFavorite ?: note.isFavorite
                            noteAdapter.notifyDataSetChanged()
                            saveNotes()
                            Toast.makeText(this, "Note modifiée avec succès", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // Restauration des notes sauvegardées lors du démarrage de l'activité.
    override fun onStart() {
        super.onStart()
        // Restaurer les notes sauvegardées
        notes.clear()
        notes.addAll(retrieveNotes())
        // Filtrer les notes avec isDeleted à true
        notesNotDeleted = notes.filter { !it.isDeleted }
        noteAdapter.updateNotes(notesNotDeleted as ArrayList<Note>)
        // Réinitialiser la recherche
        searchText.setText("")
        // Sélectionner l'onglet actif
        when (currentView) {
            CurrentView.ALL_NOTES -> {
                navigationView.setCheckedItem(R.id.nav_accueil)
            }
            CurrentView.FAVORITES -> {
                navigationView.setCheckedItem(R.id.nav_favoris)
            }
            CurrentView.TRASH -> {
                navigationView.setCheckedItem(R.id.nav_corbeille)
            }
        }
    }

    // Sauvegarde des notes dans les préférences partagées lors de l'arrêt de l'activité.
    override fun onStop() {
        super.onStop()
        // Sauvegarder les notes avant de quitter l'activité
        saveNotes()
    }

    private fun saveNotes() {
        val gson = Gson()
        val jsonNotes = gson.toJson(notes)
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("notes", jsonNotes).apply()
        sharedPreferences.edit().putInt("nextId", Note.nextId).apply() // Sauvegarder le prochain ID
    }
    private fun retrieveNotes(): List<Note> {
        val gson = Gson()
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val jsonNotes = sharedPreferences.getString("notes", null)
        Note.nextId = sharedPreferences.getInt("nextId", 1) // Restaurer le prochain ID
        return gson.fromJson(jsonNotes, object : TypeToken<List<Note>>() {}.type) ?: emptyList()
    }
    private fun clearAllNotes() {
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
    /**
     * Exécute de manière asynchrone le nettoyage des notes.
     * Utilise les coroutines pour effectuer les opérations lourdes en arrière-plan (Dispatchers.IO)
     * et mettre à jour l'interface utilisateur sur le thread principal (Dispatchers.Main).
     */
    private fun clearAllNotesAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            clearAllNotes() // Nettoyage des données persistantes
            withContext(Dispatchers.Main) {
                notes.clear() // Vide la liste des notes
                noteAdapter.updateNotes(notes) // Mise à jour de l'affichage
            }
        }
    }

    //Supprimer une note définitivement
    fun deleteNotePermanently(noteId: Int) {
        val noteIndex = notes.indexOfFirst { it.id == noteId }
        if (noteIndex != -1) {
            notes.removeAt(noteIndex)
            saveNotes() // Sauvegarde les modifications dans les SharedPreferences
            noteAdapter.updateNotes(notes) // Met à jour l'affichage
            applyCurrentViewFilter()
        }
    }
    enum class CurrentView {
        ALL_NOTES, FAVORITES, TRASH
    }
    companion object {
        private const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }
}