package com.example.noteapp

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
        if (requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
            val noteTitle = data?.getStringExtra("noteTitle")
            val noteContent = data?.getStringExtra("noteContent")
            if (!noteTitle.isNullOrEmpty() || !noteContent.isNullOrEmpty()) {
                val note = Note(
                    id = 0,
                    title = noteTitle ?: "",
                    content = noteContent ?: "",
                    lastModified = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )
                )
                notes.add(note)
                noteAdapter.notifyDataSetChanged()
                saveNotes()
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
        val filteredNotes = filterNotes()

        noteAdapter = NoteAdapter(filteredNotes)
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
    }

    private fun retrieveNotes(): List<Note> {
        val gson = Gson()
        val sharedPreferences = getSharedPreferences("notes", Context.MODE_PRIVATE)
        val jsonNotes = sharedPreferences.getString("notes", null)
        return gson.fromJson(jsonNotes, object : TypeToken<List<Note>>() {}.type) ?: emptyList()
    }

    companion object {
        private const val ADD_NOTE_REQUEST = 1
    }
}