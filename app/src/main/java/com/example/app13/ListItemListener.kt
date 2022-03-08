package com.example.app13

import androidx.recyclerview.widget.RecyclerView

interface ListItemListener {

    fun onMoveToNext(position: Int)

    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)

    fun afterTextChange(position: Int, text: String)

    fun onCheckedChange(position: Int, checked: Boolean)
}