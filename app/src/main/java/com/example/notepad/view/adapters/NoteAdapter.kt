package com.example.notepad.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.notepad.databinding.NoteItemBinding
import com.example.notepad.domain.Note

class NoteAdapter(private var notes: List<Note>,
    private val onItemClick: (Note) -> Unit): RecyclerView.Adapter<NoteViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {
        val note = notes[position]
        holder.bind(note)

        holder.itemView.setOnClickListener {
            onItemClick(note)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun updateNotes(newNotes: List<Note>){
        notes = newNotes
        notifyDataSetChanged()
    }

    fun getNoteAt(position: Int) : Note?{
        return if (position in 0 until notes.size ){
            notes[position]
        }
        else {
            null
        }
    }
}

class NoteViewHolder(private val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note){
        binding.taskTitleTextview.text = note.title
    }
}
