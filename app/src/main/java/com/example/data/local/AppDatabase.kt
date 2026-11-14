package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DictionaryDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.LanguagePackDao
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.local.entity.TranslationHistoryEntity

@Database(
  entities =
    [
      LanguagePackEntity::class,
      DictionaryEntryEntity::class,
      TranslationHistoryEntity::class,
      FavoriteEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun languagePackDao(): LanguagePackDao

  abstract fun dictionaryDao(): DictionaryDao

  abstract fun historyDao(): HistoryDao

  abstract fun favoriteDao(): FavoriteDao

  companion object {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mahay_raha_offline.db",
              )
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
