package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.local.entity.TranslationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguagePackDao {
  @Query("SELECT * FROM language_packs ORDER BY isInstalled DESC, nameFr ASC")
  fun observeAllPacks(): Flow<List<LanguagePackEntity>>

  @Query("SELECT * FROM language_packs WHERE isInstalled = 1 ORDER BY nameFr ASC")
  fun observeInstalledPacks(): Flow<List<LanguagePackEntity>>

  @Query("SELECT * FROM language_packs WHERE code = :code LIMIT 1")
  suspend fun getPackByCode(code: String): LanguagePackEntity?

  @Query("SELECT isInstalled FROM language_packs WHERE code = :code LIMIT 1")
  suspend fun isPackInstalled(code: String): Boolean?

  @Query("SELECT * FROM language_packs")
  suspend fun getAllPacksOnce(): List<LanguagePackEntity>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertPacksIgnore(packs: List<LanguagePackEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPack(pack: LanguagePackEntity)

  @Query(
    """
    UPDATE language_packs
    SET isInstalled = :installed,
        wordCount = :wordCount,
        sizeBytes = :sizeBytes,
        downloadedAt = :downloadedAt
    WHERE code = :code
    """
  )
  suspend fun updateInstallStatus(
    code: String,
    installed: Boolean,
    wordCount: Int,
    sizeBytes: Long,
    downloadedAt: Long?,
  )

  @Query("DELETE FROM language_packs WHERE code = :code AND isCustom = 1")
  suspend fun deleteCustomPack(code: String)
}

@Dao
interface DictionaryDao {
  @Query(
    """
    SELECT d.* FROM dictionary_entries d
    INNER JOIN language_packs lp ON d.languageCode = lp.code
    WHERE lp.isInstalled = 1
    ORDER BY d.term ASC
    """
  )
  fun observeAllInstalledEntries(): Flow<List<DictionaryEntryEntity>>

  @Query(
    """
    SELECT d.* FROM dictionary_entries d
    INNER JOIN language_packs lp ON d.languageCode = lp.code
    WHERE lp.isInstalled = 1
      AND (:languageCode = 'ALL' OR d.languageCode = :languageCode)
      AND (:category = 'ALL' OR d.category = :category)
      AND (
        :query = ''
        OR d.term LIKE '%' || :query || '%'
        OR d.normalizedTerm LIKE '%' || :query || '%'
        OR d.aliasesPipe LIKE '%' || :query || '%'
        OR d.definition LIKE '%' || :query || '%'
        OR d.example LIKE '%' || :query || '%'
      )
    ORDER BY
      CASE WHEN d.normalizedTerm = :normalizedQuery THEN 0
           WHEN d.normalizedTerm LIKE :normalizedQuery || '%' THEN 1
           ELSE 2 END,
      d.term ASC
    """
  )
  fun searchInstalledEntries(
    query: String,
    normalizedQuery: String,
    languageCode: String,
    category: String,
  ): Flow<List<DictionaryEntryEntity>>

  @Query("SELECT * FROM dictionary_entries WHERE languageCode = :languageCode")
  suspend fun getEntriesForLanguage(languageCode: String): List<DictionaryEntryEntity>

  @Query(
    """
    SELECT d.* FROM dictionary_entries d
    INNER JOIN language_packs lp ON d.languageCode = lp.code
    WHERE lp.isInstalled = 1 AND d.conceptId = :conceptId
    ORDER BY d.languageCode ASC
    """
  )
  suspend fun getInstalledTranslationsForConcept(conceptId: String): List<DictionaryEntryEntity>

  @Query(
    """
    SELECT d.* FROM dictionary_entries d
    INNER JOIN language_packs lp ON d.languageCode = lp.code
    WHERE lp.isInstalled = 1 AND d.conceptId = :conceptId AND d.languageCode = :targetLanguageCode
    LIMIT 1
    """
  )
  suspend fun getEntryByConceptAndLanguage(
    conceptId: String,
    targetLanguageCode: String,
  ): DictionaryEntryEntity?

  @Query("SELECT COUNT(*) FROM dictionary_entries")
  fun observeTotalWordCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntries(entries: List<DictionaryEntryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEntry(entry: DictionaryEntryEntity): Long

  @Query("DELETE FROM dictionary_entries WHERE languageCode = :languageCode")
  suspend fun deleteEntriesByLanguage(languageCode: String)
}

@Dao
interface HistoryDao {
  @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
  fun observeAllHistory(): Flow<List<TranslationHistoryEntity>>

  @Query(
    """
    SELECT * FROM translation_history
    WHERE :query = ''
       OR sourceText LIKE '%' || :query || '%'
       OR translatedText LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
    """
  )
  fun searchHistory(query: String): Flow<List<TranslationHistoryEntity>>

  @Query("SELECT * FROM translation_history ORDER BY timestamp DESC LIMIT 5")
  fun observeRecentHistory(): Flow<List<TranslationHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHistory(item: TranslationHistoryEntity): Long

  @Query("DELETE FROM translation_history WHERE id = :id")
  suspend fun deleteHistoryById(id: Long)

  @Query("DELETE FROM translation_history")
  suspend fun clearAllHistory()
}

@Dao
interface FavoriteDao {
  @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
  fun observeAllFavorites(): Flow<List<FavoriteEntity>>

  @Query("SELECT * FROM favorites WHERE type = :type ORDER BY createdAt DESC")
  fun observeFavoritesByType(type: String): Flow<List<FavoriteEntity>>

  @Query("SELECT COUNT(*) FROM favorites")
  fun observeFavoriteCount(): Flow<Int>

  @Query(
    """
    SELECT EXISTS(
      SELECT 1 FROM favorites
      WHERE type = :type
        AND sourceLangCode = :sourceLang
        AND targetLangCode = :targetLang
        AND titleText = :titleText
        AND subtitleText = :subtitleText
    )
    """
  )
  fun observeIsFavorite(
    type: String,
    sourceLang: String,
    targetLang: String,
    titleText: String,
    subtitleText: String,
  ): Flow<Boolean>

  @Query(
    """
    SELECT * FROM favorites
    WHERE type = :type
      AND sourceLangCode = :sourceLang
      AND targetLangCode = :targetLang
      AND titleText = :titleText
      AND subtitleText = :subtitleText
    LIMIT 1
    """
  )
  suspend fun findMatchingFavorite(
    type: String,
    sourceLang: String,
    targetLang: String,
    titleText: String,
    subtitleText: String,
  ): FavoriteEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(favorite: FavoriteEntity): Long

  @Query("DELETE FROM favorites WHERE id = :id")
  suspend fun deleteFavoriteById(id: Long)
}
