package com.example.app13

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface BaseNoteDao {
    @Delete
    suspend fun deleteBaseNote(baseNote: BaseNote)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaseNote(baseNote: BaseNote): Long
    @Insert
    suspend fun insertBaseNotes(baseNotes: List<BaseNote>)
    @Query("DELETE FROM BaseNote WHERE folder = :folder")
    suspend fun deleteAllBaseNotes(folder: Folder)
    @Query("SELECT * FROM BaseNote WHERE folder = :folder ORDER BY timestamp DESC")
    fun getAllBaseNotes(folder: Folder): LiveData<List<BaseNote>>
    @Query("UPDATE BaseNote SET folder = :folder WHERE id = :id")
    suspend fun moveBaseNote(id: Long, folder: Folder)
    fun getBaseNotesByKeyword(keyword: String): LiveData<List<BaseNote>> {
        val result = getBaseNotesByKeyword(keyword, Folder.NOTES)
        val filtered = result.map { list -> list.filter { baseNote -> baseNote.matchesKeyword(keyword) } }
        return filtered.asLiveData()
    }
    @Query("SELECT * FROM BaseNote WHERE folder = :folder AND (title LIKE '%' || :keyword || '%' OR body LIKE '%' || :keyword || '%' OR items LIKE '%' || :keyword || '%') ORDER BY timestamp DESC")
    fun getBaseNotesByKeyword(keyword: String, folder: Folder): Flow<List<BaseNote>>
}