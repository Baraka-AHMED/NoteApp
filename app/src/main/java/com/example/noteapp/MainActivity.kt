package com.example.noteapp

import Note
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notes = ArrayList()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(notes)
        recyclerView.adapter = noteAdapter

        val fab: ImageButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivityForResult(Intent(this, AddNoteActivity::class.java), ADD_NOTE_REQUEST)
            // C'est déprecié, à corriger
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


    private fun filterNotes(): List<Note> {
        return notes.filter { !it.isDeleted }
    }

    override fun onStart() {
        super.onStart()
        // Restaurer les notes sauvegardées
        notes.clear()
        notes.addAll(retrieveNotes())

        // Filtrer les notes avec isDeleted à true
        notesNotDeleted = filterNotes()

        noteAdapter = NoteAdapter(notesNotDeleted)
        recyclerView.adapter = noteAdapter
        Log.d("MainActivity", "Notes restored: $notes")
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
        Note.nextId = sharedPreferences.getInt("nextId", 1) // Restaurer le prochain ID
        return gson.fromJson(jsonNotes, object : TypeToken<List<Note>>() {}.type) ?: emptyList()
    }

    private fun clearAllNotes() {
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }


    companion object {
        private const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

}