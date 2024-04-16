package com.example.noteapp

import Note
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
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
    private val onDeleteNote: (Int) -> Unit  // Ajout d'une fonction pour gérer la suppression
) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.text_note_title)
        val noteContent: TextView = itemView.findViewById(R.id.text_note_content)
        val noteDate: TextView = itemView.findViewById(R.id.text_note_date)
        val trashIcon: ImageView = itemView.findViewById(R.id.trash_icon)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item_layout, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.noteTitle.text = currentNote.title
        holder.noteContent.text = currentNote.content
        holder.noteDate.text = currentNote.lastModified

        // Ajouter un OnClickListener à l'icône de la poubelle
        holder.trashIcon.setOnClickListener {
            // Marquer la note comme supprimée
            if (currentNote.isDeleted) {
                // Afficher une boîte de dialogue de confirmation
                val context = holder.itemView.context
                AlertDialog.Builder(context).apply {
                    setTitle("Confirmation de suppression")
                    setMessage("Êtes-vous sûr de vouloir supprimer cette note définitivement ?")
                    setPositiveButton("Supprimer") { _, _ ->
                        onDeleteNote(currentNote.id)  // Utilise la fonction passée pour supprimer la note
                    }
                    setNegativeButton("Annuler", null)
                    show()
                }
            }
            else {
                currentNote.isDeleted = true
                // Afficher un message de confirmation avec un bouton "Annuler"
                val snackbar = Snackbar.make(holder.itemView, "Note déplacée vers la corbeille", Snackbar.LENGTH_LONG)
                snackbar.setAction("Annuler") {
                    // Utiliser l'ID pour retrouver la note et annuler la suppression
                    currentNote.isDeleted = false
                    onNotesChanged()
                }
                snackbar.show()
            }
            onNotesChanged()


        }

        // Ajoutez un OnClickListener à la vue de l'élément
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddNoteActivity::class.java)
            val n = currentNote.id
            Log.d("NoteAdapter", "Note $n")
            intent.putExtra("noteId", currentNote.id)
            intent.putExtra("noteTitle", currentNote.title)
            intent.putExtra("noteContent", currentNote.content)
            intent.putExtra("noteIsFavorite", currentNote.isFavorite)
            (holder.itemView.context as Activity).startActivityForResult(intent, MainActivity.EDIT_NOTE_REQUEST)
        }

    }

    fun updateNotes(newNotes: ArrayList<Note>) {
        this.notes = newNotes
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return notes.size
    }

}
