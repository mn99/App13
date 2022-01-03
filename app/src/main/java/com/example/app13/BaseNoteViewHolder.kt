package com.example.app13

import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.app13.databinding.ListItemPreviewBinding
import com.example.app13.databinding.NoteRowItemBinding
import com.google.android.material.textview.MaterialTextView

class BaseNoteViewHolder(
    private val binding: NoteRowItemBinding,
    private val itemListener: ItemListener,
    private val inflater: LayoutInflater,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                itemListener.onClick(adapterPosition)
            }
            binding.root.setOnLongClickListener {
                itemListener.onLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
        fun bind(baseNote: BaseNote) {
            when (baseNote.type) {
                Type.NOTE -> bindNote(baseNote)
                Type.LIST -> bindList(baseNote)
            }
            if (baseNote.isEmpty()) {
                binding.BodyText.setText(baseNote.getEmptyMessage())
                binding.BodyText.isVisible = true
            }
        }
        private fun bindNote(note: BaseNote) {
            binding.TitleText.text = note.title
            binding.BodyText.text = note.body.applySpans(note.spans)
            binding.TitleText.isVisible = note.title.isNotEmpty()
            binding.BodyText.isVisible = note.body.isNotEmpty()
        }
        private fun bindList(list: BaseNote) {
            binding.BodyText.isVisible = false
            binding.ListSpace.isVisible = true
            binding.TitleText.text = list.title
            binding.TitleText.isVisible = list.title.isNotEmpty()
            val maxItems = 10
            val filteredList = list.items.take(maxItems)
            binding.ListSpace.removeAllViews()
            for (item in filteredList) {
                val view = ListItemPreviewBinding.inflate(inflater).root
                view.text = item.body
                view.handleChecked(item.checked)
                binding.ListSpace.addView(view)
            }
            if (list.items.size > 10) {
                val view = ListItemPreviewBinding.inflate(inflater).root
                val itemsRemaining = list.items.size - maxItems
                view.text = if (itemsRemaining == 1) {
                    binding.root.context.getString(R.string.one_more_item)
                } else binding.root.context.getString(R.string.more_items, itemsRemaining)
                binding.ListSpace.addView(view)
            }
            binding.ListSpace.isVisible = list.items.isNotEmpty()
        }
        private fun MaterialTextView.handleChecked(checked: Boolean) {
            if (checked) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_16, 0, 0, 0)
            } else setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.checkbox_outline_16, 0, 0, 0)
            paint.isStrikeThruText = checked
        }
    }