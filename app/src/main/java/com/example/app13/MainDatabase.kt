package com.example.app13

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(Converters::class)
@Database(entities = [BaseNote::class], version = 1)
abstract class MainDatabase : RoomDatabase() {
    abstract val baseNoteDao: BaseNoteDao
    companion object {
        private const val databaseName = "MainDatabase"
        @Volatile
        internal var instance: MainDatabase? = null
        fun getDatabase(application: Application): MainDatabase {
            val tempInstance = instance
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(application, MainDatabase::class.java, databaseName).build()
                Companion.instance = instance
                return instance
            }
        }
    }
}