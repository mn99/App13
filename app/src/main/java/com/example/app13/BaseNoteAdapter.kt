package com.example.app13

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.app13.databinding.NoteRowItemBinding

class BaseNoteAdapter(
    private val itemListener: ItemListener
) : ListAdapter<BaseNote, BaseNoteViewHolder>(DiffCallback()) {
    override fun onBindViewHolder(holder: BaseNoteViewHolder, position: Int) {
        val baseNote = getItem(position)
        holder.bind(baseNote)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseNoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NoteRowItemBinding.inflate(inflater, parent, false)
        return BaseNoteViewHolder(binding, itemListener, inflater)
    }
    private class DiffCallback : DiffUtil.ItemCallback<BaseNote>() {
        override fun areItemsTheSame(oldItem: BaseNote, newItem: BaseNote): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: BaseNote, newItem: BaseNote): Boolean {
            return oldItem == newItem
        }
    }
}