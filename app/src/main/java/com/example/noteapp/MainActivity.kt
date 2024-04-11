package com.example.noteapp

import Note
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(notes)
        recyclerView.adapter = noteAdapter

        val fab: ImageButton = findViewById(R.id.fab)


        fab.setOnClickListener {
            when (currentView) {
                CurrentView.ALL_NOTES -> {
                    fab.setOnClickListener {
                        startActivityForResult(Intent(this, AddNoteActivity::class.java), ADD_NOTE_REQUEST)
                    }
                }
                // Met automtiquement une note dans les favoris si on l'ajoute depuis la fenetre favoris
                CurrentView.FAVORITES -> {
                    startActivityForResult(Intent(this, AddNoteActivity::class.java).apply {
                        putExtra("isFavorite", true) // Ajout d'un extra pour indiquer que la note est favorite
                    }, ADD_NOTE_REQUEST)
                }
                CurrentView.TRASH -> {
                    fab.setOnClickListener {
                        startActivityForResult(Intent(this, AddNoteActivity::class.java), ADD_NOTE_REQUEST)
                    }
                }
            }
        }

        searchText = findViewById(R.id.search_text)
        val noMatchMessage = "Aucune note ne correspond à votre recherche."

        //
        var isToastDisplayed = false // vérifie si le toast est déja affiché ou pas

        searchText.addTextChangedListener { s ->
            val searchQuery = s.toString()
            val filteredNotes = filterNotesBasedOnCurrentView(searchQuery) // Filtre les notes en fonction de la vue actuellement sélectionnée et de la recherche
            if (searchQuery.isNotEmpty()) {
                if (filteredNotes.isEmpty() && !isToastDisplayed) {
                    Toast.makeText(this, "Aucune note ne correspond à votre recherche.", Toast.LENGTH_SHORT).show() // Affiche un Toast indiquant qu'aucune note ne correspond à la recherche
                    isToastDisplayed = true // Marque que le Toast est affiché
                } else {
                    isToastDisplayed = false // Réinitialise la variable indiquant que le Toast a été affiché
                }
                noteAdapter.updateNotes(filteredNotes)
            } else {
                applyCurrentViewFilter()
            }
        }



        btnAnnuller = findViewById(R.id.btn_annuler)
        btnAnnuller.setOnClickListener {
            searchText.setText("")
            recyclerView.adapter = noteAdapter
        }

        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            currentView = when (menuItem.itemId) {
                R.id.nav_accueil -> CurrentView.ALL_NOTES
                R.id.nav_notes -> CurrentView.FAVORITES
                R.id.nav_corbeille -> CurrentView.TRASH
                else -> currentView
            }
            searchText.setText("") // Réinitialiser la barre de recherche
            applyCurrentViewFilter() // Applique le filtre de vue actuel
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }
    private fun applyCurrentViewFilter() {
        val filteredNotes = filterNotesBasedOnCurrentView(searchText.text.toString())
        noteAdapter.updateNotes(filteredNotes)
    }
    private fun filterNotesBasedOnCurrentView(searchQuery: String): List<Note> {
        return when (currentView) {
            CurrentView.ALL_NOTES -> notes.filter { !it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
            CurrentView.FAVORITES -> notes.filter { it.isFavorite && !it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
            CurrentView.TRASH -> notes.filter { it.isDeleted && (it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)) }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ADD_NOTE_REQUEST -> {
                    val noteTitle = data?.getStringExtra("noteTitle")
                    val noteContent = data?.getStringExtra("noteContent")
                    if (!noteTitle.isNullOrEmpty() || !noteContent.isNullOrEmpty()) {
                        val note = Note(
                            title = noteTitle ?: "",
                            content = noteContent ?: "",
                            lastModified = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        notes.add(note)
                        noteAdapter.notifyDataSetChanged( )
                        saveNotes()
                    }
                }
                EDIT_NOTE_REQUEST -> {
                    val noteId = data?.getIntExtra("noteId", -1)
                    Log.d("Note ID", "Note $noteId")

                    val noteTitle = data?.getStringExtra("noteTitle")
                    val noteContent = data?.getStringExtra("noteContent")
                    if (noteId != null && noteId != -1) {
                        val note = notes.find { it.id == noteId }
                        val n = note?.id
                        Log.d("Note", "Note $n")
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
                            noteAdapter.notifyDataSetChanged()
                            saveNotes()
                        }
                    }
                }

            }
        }
    }


    private fun filterNotesNotDeleted(): List<Note> {
        return notes.filter { !it.isDeleted }
    }

    override fun onStart() {
        super.onStart()
        // Restaurer les notes sauvegardées
        notes.clear()
        notes.addAll(retrieveNotes())

        // Filtrer les notes avec isDeleted à true
        notesNotDeleted = filterNotesNotDeleted()

        noteAdapter.updateNotes(notesNotDeleted)

        // Réinitialiser la recherche
        searchText.setText("")

        // Sélectionner l'onglet actif
        when (currentView) {
            CurrentView.ALL_NOTES -> {
                // Sélectionner l'onglet correspondant dans la navigationView
                navigationView.setCheckedItem(R.id.nav_accueil)
            }
            CurrentView.FAVORITES -> {
                // Sélectionner l'onglet correspondant dans la navigationView
                navigationView.setCheckedItem(R.id.nav_notes)
            }
            CurrentView.TRASH -> {
                // Sélectionner l'onglet correspondant dans la navigationView
                navigationView.setCheckedItem(R.id.nav_corbeille)
            }
        }
    }

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
        Log.d("SharedPreferences", "Contenu de 'notes': $jsonNotes")

        Note.nextId = sharedPreferences.getInt("nextId", 1) // Restaurer le prochain ID
        return gson.fromJson(jsonNotes, object : TypeToken<List<Note>>() {}.type) ?: emptyList()
    }

    private fun clearAllNotes() {
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    enum class CurrentView {
        ALL_NOTES, FAVORITES, TRASH
    }

    companion object {
        private const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

}