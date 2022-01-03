package com.example.app13

import android.app.Application
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class TakeNoteModel(app: Application) : AndroidViewModel(app) {
    private val database = MainDatabase.getDatabase(app)
    private val baseNoteDao = database.baseNoteDao
    var id = 0L
    var isNewNote = true
    var isFirstInstance = true
    var folder = Folder.NOTES
    var title = String()
    var timestamp = Date().time
    var body = Editable.Factory.getInstance().newEditable(String())
    fun getBaseNote(): BaseNote {
        return BaseNote.createNote(id, folder, title, timestamp, body.toString().trimEnd(), body.getFilteredSpans())
    }
    fun setStateFromBaseNote(baseNote: BaseNote) {
        id = baseNote.id
        folder = baseNote.folder
        title = baseNote.title
        timestamp = baseNote.timestamp
        body = baseNote.body.applySpans(baseNote.spans)
    }
    private fun Spannable.getFilteredSpans(): ArrayList<SpanRepresentation> {
        val representations = LinkedHashSet<SpanRepresentation>()
        val spans = getSpans(0, length, Any::class.java)
        spans.forEach { span ->
            val end = getSpanEnd(span)
            val start = getSpanStart(span)
            val representation = SpanRepresentation(false, false, false, false, false, start, end)
            when (span) {
                is StyleSpan -> {
                    representation.bold = span.style == Typeface.BOLD
                    representation.italic = span.style == Typeface.ITALIC
                }
                is URLSpan -> representation.link = true
                is TypefaceSpan -> representation.monospace = span.family == "monospace"
                is StrikethroughSpan -> representation.strikethrough = true
            }

            if (representation.isNotUseless()) {
                representations.add(representation)
            }
        }
        return getFilteredRepresentations(ArrayList(representations))
    }
    private fun getFilteredRepresentations(representations: ArrayList<SpanRepresentation>): ArrayList<SpanRepresentation> {
        representations.forEachIndexed { index, representation ->
            val match = representations.find { spanRepresentation ->
                spanRepresentation.isEqualInSize(representation)
            }
            if (match != null && representations.indexOf(match) != index) {
                if (match.bold) {
                    representation.bold = true
                }
                if (match.link) {
                    representation.link = true
                }
                if (match.italic) {
                    representation.italic = true
                }
                if (match.monospace) {
                    representation.monospace = true
                }
                if (match.strikethrough) {
                    representation.strikethrough = true
                }
                val copy = ArrayList(representations)
                copy[index] = representation
                copy.remove(match)
                return getFilteredRepresentations(copy)
            }
        }
        return representations
    }
    fun saveNote(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            id = withContext(Dispatchers.IO) { baseNoteDao.insertBaseNote(getBaseNote()) }
            onComplete?.invoke()
        }
    }
}