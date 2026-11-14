package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "language_packs")
data class LanguagePackEntity(
  @PrimaryKey val code: String,
  val nameFr: String,
  val nativeName: String,
  val regionBadge: String,
  val version: String,
  val description: String,
  val speechLocaleTag: String,
  val assetFileName: String,
  val wordCount: Int = 0,
  val sizeBytes: Long = 0L,
  val isInstalled: Boolean = false,
  val isCustom: Boolean = false,
  val downloadedAt: Long? = null,
)

@Entity(
  tableName = "dictionary_entries",
  indices =
    [
      Index(value = ["languageCode"]),
      Index(value = ["conceptId"]),
      Index(value = ["normalizedTerm"]),
    ],
)
data class DictionaryEntryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val conceptId: String,
  val languageCode: String,
  val term: String,
  val normalizedTerm: String,
  val aliasesPipe: String,
  val aliasesNormalizedPipe: String,
  val phonetic: String,
  val partOfSpeech: String,
  val category: String,
  val definition: String,
  val example: String,
  val exampleGloss: String,
  val isCustom: Boolean = false,
)

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sourceLangCode: String,
  val targetLangCode: String,
  val sourceText: String,
  val translatedText: String,
  val coveragePercent: Int,
  val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String, // "TRANSLATION" or "WORD"
  val sourceLangCode: String,
  val targetLangCode: String,
  val titleText: String,
  val subtitleText: String,
  val detailNote: String = "",
  val dictionaryEntryId: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
)
