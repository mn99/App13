package com.example.app13

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app13.databinding.RecyclerListItemBinding
import java.util.*

class TakeListAdapter(private val items: ArrayList<ListItem>, private val listener: ListItemListener) :
    RecyclerView.Adapter<TakeListViewHolder>() {
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: TakeListViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TakeListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerListItemBinding.inflate(inflater, parent, false)
        return TakeListViewHolder(binding, listener)
    }
}