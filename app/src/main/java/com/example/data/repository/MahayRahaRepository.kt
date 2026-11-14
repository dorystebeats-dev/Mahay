package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.local.entity.TranslationHistoryEntity
import com.example.data.pack.LanguagePackInstaller
import com.example.data.pack.PackDownloadProgress
import com.example.domain.translation.TextNormalizer
import com.example.domain.translation.TranslationEngine
import com.example.domain.translation.TranslationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class MahayRahaRepository(
  private val database: AppDatabase,
  private val packInstaller: LanguagePackInstaller,
  private val translationEngine: TranslationEngine,
) {
  private val packDao = database.languagePackDao()
  private val dictionaryDao = database.dictionaryDao()
  private val historyDao = database.historyDao()
  private val favoriteDao = database.favoriteDao()

  val allPacks: Flow<List<LanguagePackEntity>> = packDao.observeAllPacks()
  val installedPacks: Flow<List<LanguagePackEntity>> = packDao.observeInstalledPacks()
  val activeDownloads: StateFlow<Map<String, PackDownloadProgress>> = packInstaller.activeDownloads
  val totalInstalledWords: Flow<Int> = dictionaryDao.observeTotalWordCount()
  val recentHistory: Flow<List<TranslationHistoryEntity>> = historyDao.observeRecentHistory()
  val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.observeAllFavorites()
  val favoriteCount: Flow<Int> = favoriteDao.observeFavoriteCount()

  suspend fun initializeCatalog() {
    packInstaller.ensureManifestSeeded()
  }

  suspend fun downloadPack(code: String): Result<Int> = packInstaller.installPack(code)

  suspend fun deletePack(code: String) = packInstaller.uninstallPack(code)

  suspend fun createCustomLanguage(
    code: String,
    nameFr: String,
    nativeName: String,
    description: String,
  ): Result<LanguagePackEntity> =
    packInstaller.createCustomLanguagePack(code, nameFr, nativeName, description)

  suspend fun addCustomWordPair(
    sourceLangCode: String,
    targetLangCode: String,
    sourceTerm: String,
    targetTerm: String,
    partOfSpeech: String,
    definition: String,
    example: String,
  ): Result<Unit> =
    packInstaller.addCustomBilingualEntry(
      sourceLangCode = sourceLangCode,
      targetLangCode = targetLangCode,
      sourceTerm = sourceTerm,
      targetTerm = targetTerm,
      partOfSpeech = partOfSpeech,
      definition = definition,
      example = example,
    )

  suspend fun translate(
    text: String,
    sourceLangCode: String,
    targetLangCode: String,
    saveToHistory: Boolean = true,
  ): TranslationResult {
    val result = translationEngine.translate(text, sourceLangCode, targetLangCode)
    if (saveToHistory && result is TranslationResult.Success && sourceLangCode != targetLangCode) {
      historyDao.insertHistory(
        TranslationHistoryEntity(
          sourceLangCode = result.sourceLangCode,
          targetLangCode = result.targetLangCode,
          sourceText = result.sourceText,
          translatedText = result.translatedText,
          coveragePercent = result.coveragePercent,
        )
      )
    }
    return result
  }

  fun searchDictionary(
    query: String,
    languageCode: String,
    category: String,
  ): Flow<List<DictionaryEntryEntity>> {
    return dictionaryDao.searchInstalledEntries(
      query = query.trim(),
      normalizedQuery = TextNormalizer.normalize(query),
      languageCode = languageCode,
      category = category,
    )
  }

  suspend fun getConceptEquivalents(conceptId: String): List<DictionaryEntryEntity> {
    return dictionaryDao.getInstalledTranslationsForConcept(conceptId)
  }

  fun observeHistory(query: String): Flow<List<TranslationHistoryEntity>> {
    return historyDao.searchHistory(query.trim())
  }

  suspend fun deleteHistoryItem(id: Long) {
    historyDao.deleteHistoryById(id)
  }

  suspend fun clearHistory() {
    historyDao.clearAllHistory()
  }

  fun observeFavorites(filterType: String?): Flow<List<FavoriteEntity>> {
    return if (filterType == null || filterType == "ALL") {
      favoriteDao.observeAllFavorites()
    } else {
      favoriteDao.observeFavoritesByType(filterType)
    }
  }

  fun observeIsFavorite(
    type: String,
    sourceLang: String,
    targetLang: String,
    titleText: String,
    subtitleText: String,
  ): Flow<Boolean> {
    return favoriteDao.observeIsFavorite(type, sourceLang, targetLang, titleText, subtitleText)
  }

  suspend fun toggleFavorite(
    type: String,
    sourceLang: String,
    targetLang: String,
    titleText: String,
    subtitleText: String,
    detailNote: String = "",
    dictionaryEntryId: Long? = null,
  ): Boolean {
    val existing =
      favoriteDao.findMatchingFavorite(type, sourceLang, targetLang, titleText, subtitleText)
    return if (existing != null) {
      favoriteDao.deleteFavoriteById(existing.id)
      false
    } else {
      favoriteDao.insertFavorite(
        FavoriteEntity(
          type = type,
          sourceLangCode = sourceLang,
          targetLangCode = targetLang,
          titleText = titleText,
          subtitleText = subtitleText,
          detailNote = detailNote,
          dictionaryEntryId = dictionaryEntryId,
        )
      )
      true
    }
  }

  suspend fun removeFavorite(id: Long) {
    favoriteDao.deleteFavoriteById(id)
  }
}
