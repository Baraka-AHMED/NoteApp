package com.example.noteapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptateurMenu(private val mesDonnees: List<MonElement>) : RecyclerView.Adapter<AdaptateurMenu.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemText: TextView = itemView.findViewById(R.id.text_view)
        val itemIcon: ImageView = itemView.findViewById(R.id.icon_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = mesDonnees[position]
        holder.itemText.text = element.texte
        holder.itemIcon.setImageResource(element.icone)
    }

    override fun getItemCount() = mesDonnees.size
}

