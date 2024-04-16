package com.example.noteapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddNoteActivity : AppCompatActivity() {

    private lateinit var addNoteTitle: EditText
    private lateinit var addNoteContent: EditText
    private lateinit var btnFavorite: ImageButton
    private var favoriteState = ""  // État initial



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addNoteTitle = findViewById(R.id.add_note_title)
        addNoteContent = findViewById(R.id.add_note_content)

        // Écouter les événements de touche pour le bouton retour du téléphone
        addNoteTitle.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                saveNoteIfNotEmpty()
                true
            } else {
                false
            }
        }

        btnFavorite = findViewById(R.id.btn_options)
        btnFavorite.setOnClickListener {
            favoriteState = if (favoriteState == "noteNotFavorite") "noteIsFavorite" else "noteNotFavorite"  // Bascule l'état du favori
            basculeStatFavoriteButton(favoriteState)
            Log.d("Favorite", "Favorite $favoriteState")
        }

        val noteTitle = intent.getStringExtra("noteTitle")
        val noteContent = intent.getStringExtra("noteContent")
        val noteIsFavorite = intent.getStringExtra("noteIsFavorite")

        Log.d("Favorite444", "Favorite444 $noteIsFavorite")
        addNoteTitle.setText(noteTitle)
        addNoteContent.setText(noteContent)
        if (noteIsFavorite != null) {
            favoriteState = noteIsFavorite
        }
        basculeStatFavoriteButton(favoriteState)

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
        val noteId = intent.getIntExtra("noteId", 0)
        val noteTitle = addNoteTitle.text.toString().trim()
        val noteContent = addNoteContent.text.toString().trim()

        if (noteTitle.isNotEmpty() || noteContent.isNotEmpty()) {
            val intent = Intent().apply {
                putExtra("noteId", noteId)
                putExtra("noteTitle", noteTitle)
                putExtra("noteContent", noteContent)
                putExtra("noteIsFavorite", favoriteStateToBoolean())  // Assurez-vous que cette clé est correcte
            }
            Log.d("Favorite22", "Favorite22 $favoriteState")

            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }


    // Permet de basculer l'état du favori en fonction d'un boom
    private fun basculeStatFavoriteButton(state: String) {
        if (state == "noteIsFavorite") {
            btnFavorite.setImageResource(R.drawable.ic_star_solid)
        } else if (state == "noteNotFavorite" ) {
            btnFavorite.setImageResource(R.drawable.ic_star_regular)
        }
    }

    private fun favoriteStateToBoolean(): Boolean {
        if (favoriteState == "noteIsFavorite") {
            return true
        } else if (favoriteState == "noteNotFavorite" ) {
            return false
        }
        return false
    }






}


