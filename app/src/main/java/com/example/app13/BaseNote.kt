package com.example.app13

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(indices = [Index(value = ["id", "folder", "timestamp"])])
data class BaseNote(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: Type,
    val folder: Folder,
    val title: String,
    val timestamp: Long,
    val body: String,
    val spans: List<SpanRepresentation>,
    val items: List<ListItem>,
) : Parcelable {
    fun isEmpty(): Boolean {
        return when (type) {
            Type.NOTE -> title.isBlank() && body.isBlank()
            Type.LIST -> title.isBlank() && items.isEmpty()
        }
    }
    fun getEmptyMessage(): Int {
        return when (type) {
            Type.NOTE -> R.string.empty_note
            Type.LIST -> R.string.empty_list
        }
    }
    fun matchesKeyword(keyword: String): Boolean {
        if (title.contains(keyword, true)) {
            return true
        }
        if (body.contains(keyword, true)) {
            return true
        }
        for (item in items) {
            if (item.body.contains(keyword, true)) {
                return true
            }
        }
        return false
    }
    companion object {
        fun createNote(id: Long, folder: Folder, title: String, timestamp: Long, body: String, spans: List<SpanRepresentation>): BaseNote {
            return BaseNote(id, Type.NOTE, folder, title, timestamp, body, spans, emptyList())
        }
        fun createList(id: Long, folder: Folder, title: String, timestamp: Long, items: List<ListItem>): BaseNote {
            return BaseNote(id, Type.LIST, folder, title, timestamp, String(), emptyList(), items)
        }
    }
}