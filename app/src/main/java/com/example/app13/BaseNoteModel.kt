package com.example.app13

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BaseNoteModel(private val app: Application) : AndroidViewModel(app) {
    private val database = MainDatabase.getDatabase(app)
    private val baseNoteDao = database.baseNoteDao
    val formatter = getDateFormatter(app.getLocale())
    var currentFile: File? = null
    val baseNotes = Content(baseNoteDao.getAllBaseNotes(Folder.NOTES))
    val deletedNotes = Content(baseNoteDao.getAllBaseNotes(Folder.DELETED))
    val archivedNotes = Content(baseNoteDao.getAllBaseNotes(Folder.ARCHIVED))
    var keyword = String()
        set(value) {
            if (field != value) {
                field = value
                searchResults.fetch(value)
            }
        }
    val searchResults = SearchResult(viewModelScope, baseNoteDao)
    init {
        viewModelScope.launch {
            val previousNotes = getPreviousNotes()
            val delete: (file: File) -> Unit = { file: File -> file.delete() }
            if (previousNotes.isNotEmpty()) {
                database.withTransaction {
                    baseNoteDao.insertBaseNotes(previousNotes)
                    getNotePath().listFiles()?.forEach(delete)
                    getDeletedPath().listFiles()?.forEach(delete)
                    getArchivedPath().listFiles()?.forEach(delete)
                }
            }
        }
    }
    fun writeCurrentFileToUri(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                (app.contentResolver.openOutputStream(uri) as? FileOutputStream)?.use { stream ->
                    stream.channel.truncate(0)
                    stream.write(requireNotNull(currentFile).readBytes())
                }
            }
//            Toast.makeText(app, R.string.saved_to_device, Toast.LENGTH_LONG).show()
        }
    }
    fun restoreBaseNote(id: Long) = executeAsync { baseNoteDao.moveBaseNote(id, Folder.NOTES) }
    fun moveBaseNoteToDeleted(id: Long) = executeAsync { baseNoteDao.moveBaseNote(id, Folder.DELETED) }
    fun moveBaseNoteToArchive(id: Long) = executeAsync { baseNoteDao.moveBaseNote(id, Folder.ARCHIVED) }
    fun deleteAllBaseNotes() = executeAsync { baseNoteDao.deleteAllBaseNotes(Folder.DELETED) }
    fun deleteBaseNoteForever(baseNote: BaseNote) = executeAsync { baseNoteDao.deleteBaseNote(baseNote) }
    private fun getPreviousNotes(): List<BaseNote> {
        val previousNotes = ArrayList<BaseNote>()
        getNotePath().listFiles()?.mapTo(previousNotes) { file ->
            XMLUtils.readBaseNoteFromFile(
                file,
                Folder.NOTES
            )
        }
        getDeletedPath().listFiles()?.mapTo(previousNotes) { file ->
            XMLUtils.readBaseNoteFromFile(
                file,
                Folder.DELETED
            )
        }
        getArchivedPath().listFiles()?.mapTo(previousNotes) { file ->
            XMLUtils.readBaseNoteFromFile(
                file,
                Folder.ARCHIVED
            )
        }
        return previousNotes
    }
    private fun getNotePath() = getFolder("notes")
    private fun getDeletedPath() = getFolder("deleted")
    private fun getArchivedPath() = getFolder("archived")
    private fun getFolder(name: String): File {
        val folder = File(app.filesDir, name)
        if (!folder.exists()) {
            folder.mkdir()
        }
        return folder
    }
    private fun executeAsync(function: suspend () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { function() }
        }
    }
    companion object {
        fun getDateFormatter(locale: Locale): SimpleDateFormat {
            val pattern = when (locale.language) {
                Locale.CHINESE.language,
                Locale.JAPANESE.language -> "yyyy年 MMM d日 (EEE)"
                else -> "EEE d MMM yyyy"
            }
            return SimpleDateFormat(pattern, locale)
        }
    }
}