package com.example.noteapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddNoteActivity : AppCompatActivity() {

    private lateinit var addNoteTitle: EditText
    private lateinit var addNoteContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addNoteTitle = findViewById(R.id.add_note_title)
        addNoteContent = findViewById(R.id.add_note_content)

        // Écouter les événements de touche pour le bouton retour
        addNoteTitle.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                saveNoteIfNotEmpty()
                true
            } else {
                false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                saveNoteIfNotEmpty()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNoteIfNotEmpty() {
        val noteTitle = addNoteTitle.text.toString().trim()
        val noteContent = addNoteContent.text.toString().trim()

        if (noteTitle.isNotEmpty() || noteContent.isNotEmpty()) {
            // Créer un Intent pour renvoyer les données à MainActivity
            val intent = Intent().apply {
                putExtra("noteTitle", noteTitle)
                putExtra("noteContent", noteContent)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}


