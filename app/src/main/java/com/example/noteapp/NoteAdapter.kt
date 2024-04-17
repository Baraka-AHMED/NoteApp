package com.example.noteapp

import Note
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class NoteAdapter(
    private var notes: ArrayList<Note>,
    private val onNotesChanged: () -> Unit,
    private val onDeleteNote: (Int) -> Unit  // Fonction pour gérer la suppression d'une note
) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialisation des variables de l'adapter
        val noteTitle: TextView = itemView.findViewById(R.id.text_note_title)
        val noteContent: TextView = itemView.findViewById(R.id.text_note_content)
        val noteDate: TextView = itemView.findViewById(R.id.text_note_date)
        val trashIcon: ImageView = itemView.findViewById(R.id.trash_icon)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        return NoteViewHolder(view)
    }
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        // Remplissage de text de la note
        val currentNote = notes[position]
        holder.noteTitle.text = currentNote.title
        holder.noteContent.text = currentNote.content
        holder.noteDate.text = currentNote.lastModified

        // Configuration de l'icône de la poubelle pour gérer la suppression
        holder.trashIcon.setOnClickListener {
            if (currentNote.isDeleted) {
                // Confirmer la suppression si la note est déjà marquée
                val context = holder.itemView.context
                AlertDialog.Builder(context).apply {
                    setTitle("Confirmation de suppression")
                    setMessage("Êtes-vous sûr de vouloir supprimer cette note définitivement ?")
                    setPositiveButton("Supprimer") { _, _ ->
                        onDeleteNote(currentNote.id)  // Suppression effective de la note
                    }
                    setNegativeButton("Annuler", null)
                    show()
                }
            } else {
                // Marquer la note comme supprimée
                currentNote.isDeleted = true
                val snackbar = Snackbar.make(holder.itemView, "Note déplacée vers la corbeille", Snackbar.LENGTH_LONG)
                snackbar.setAction("Annuler") {
                    currentNote.isDeleted = false
                    onNotesChanged()  // Annuler la suppression
                }
                snackbar.show()
            }
            onNotesChanged()  // Rafraîchir l'affichage après une modification
        }
        // Gérer le clic sur l'élément pour ouvrir l'activité d'édition
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddNoteActivity::class.java).apply {
                putExtra("noteId", currentNote.id)
                putExtra("noteTitle", currentNote.title)
                putExtra("noteContent", currentNote.content)
                putExtra("noteIsFavorite", if (currentNote.isFavorite == true) "noteIsFavorite" else "noteNotFavorite")
            }
            (holder.itemView.context as Activity).startActivityForResult(intent, MainActivity.EDIT_NOTE_REQUEST)
        }
    }
    // Mettre à jour la liste de notes et rafraîchir l'affichage
    fun updateNotes(newNotes: ArrayList<Note>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}
