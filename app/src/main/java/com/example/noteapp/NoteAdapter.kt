package com.example.noteapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class NoteAdapter(private val notes: List<Note>) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.text_note_title)
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
        holder.noteDate.text = currentNote.lastModified

        // Ajouter un OnClickListener à l'icône de la poubelle
        holder.trashIcon.setOnClickListener {
            // Modifier l'attribut isDeleted de la note à true
            notes[position].isDeleted = true
            notifyDataSetChanged()

            // Afficher un message de confirmation avec un bouton "Annuler"
            val snackbar = Snackbar.make(
                holder.itemView,
                "Note déplacée vers la corbeille",
                Snackbar.LENGTH_LONG
            )
            snackbar.setAction("Annuler") {
                // Annuler le déplacement de la note
                notes[position].isDeleted = false
                notifyDataSetChanged()
            }
            snackbar.setActionTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            ) // Couleur personnalisée pour le bouton "Annuler"
            snackbar.show()
        }

        // Ajoutez un OnClickListener à la vue de l'élément
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddNoteActivity::class.java)
            intent.putExtra("noteId", currentNote.id)
            intent.putExtra("noteTitle", currentNote.title)
            intent.putExtra("noteContent", currentNote.content)
            (holder.itemView.context as Activity).startActivityForResult(
                intent,
                MainActivity.EDIT_NOTE_REQUEST
            )

        }
    }




    override fun getItemCount(): Int {
        return notes.filter { !it.isDeleted }.size
    }
}
