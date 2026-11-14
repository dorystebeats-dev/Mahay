package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.local.entity.TranslationHistoryEntity
import com.example.data.pack.PackDownloadProgress
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.MahayRahaRepository
import com.example.domain.translation.TranslationResult
import com.example.service.TtsManager
import com.example.service.TtsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DictionaryDetailState(
  val entry: DictionaryEntryEntity,
  val equivalents: List<DictionaryEntryEntity>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MahayRahaViewModel(
  private val repository: MahayRahaRepository,
  private val preferencesRepository: UserPreferencesRepository,
  private val ttsManager: TtsManager,
) : ViewModel() {

  private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val uiMessages = _uiMessages.asSharedFlow()

  val preferences: StateFlow<UserPreferences> =
    preferencesRepository.preferencesFlow.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      UserPreferences(),
    )

  val allPacks: StateFlow<List<LanguagePackEntity>> =
    repository.allPacks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val installedPacks: StateFlow<List<LanguagePackEntity>> =
    repository.installedPacks.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList(),
    )

  val activeDownloads: StateFlow<Map<String, PackDownloadProgress>> = repository.activeDownloads

  val totalInstalledWords: StateFlow<Int> =
    repository.totalInstalledWords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val recentHistory: StateFlow<List<TranslationHistoryEntity>> =
    repository.recentHistory.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList(),
    )

  val favoriteCount: StateFlow<Int> =
    repository.favoriteCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val ttsState: StateFlow<TtsUiState> = ttsManager.state

  // Translator state
  private val _inputText = MutableStateFlow("")
  val inputText: StateFlow<String> = _inputText.asStateFlow()

  private val _translationResult = MutableStateFlow<TranslationResult?>(null)
  val translationResult: StateFlow<TranslationResult?> = _translationResult.asStateFlow()

  private val _isTranslating = MutableStateFlow(false)
  val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

  val isCurrentTranslationFavorite: StateFlow<Boolean> =
    _translationResult
      .flatMapLatest { res ->
        if (res is TranslationResult.Success) {
          repository.observeIsFavorite(
            type = "TRANSLATION",
            sourceLang = res.sourceLangCode,
            targetLang = res.targetLangCode,
            titleText = res.sourceText,
            subtitleText = res.translatedText,
          )
        } else {
          flowOf(false)
        }
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Dictionary state
  private val _dictQuery = MutableStateFlow("")
  val dictQuery: StateFlow<String> = _dictQuery.asStateFlow()

  private val _dictLangFilter = MutableStateFlow("ALL")
  val dictLangFilter: StateFlow<String> = _dictLangFilter.asStateFlow()

  private val _dictCategoryFilter = MutableStateFlow("ALL")
  val dictCategoryFilter: StateFlow<String> = _dictCategoryFilter.asStateFlow()

  val dictionaryEntries: StateFlow<List<DictionaryEntryEntity>> =
    combine(_dictQuery, _dictLangFilter, _dictCategoryFilter) { q, lang, cat ->
        Triple(q, lang, cat)
      }
      .flatMapLatest { (q, lang, cat) -> repository.searchDictionary(q, lang, cat) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedDictionaryDetail = MutableStateFlow<DictionaryDetailState?>(null)
  val selectedDictionaryDetail: StateFlow<DictionaryDetailState?> =
    _selectedDictionaryDetail.asStateFlow()

  // History state
  private val _historyQuery = MutableStateFlow("")
  val historyQuery: StateFlow<String> = _historyQuery.asStateFlow()

  val historyItems: StateFlow<List<TranslationHistoryEntity>> =
    _historyQuery
      .flatMapLatest { q -> repository.observeHistory(q) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Favorites state
  private val _favoritesFilter = MutableStateFlow("ALL")
  val favoritesFilter: StateFlow<String> = _favoritesFilter.asStateFlow()

  val favoriteItems: StateFlow<List<FavoriteEntity>> =
    _favoritesFilter
      .flatMapLatest { filter -> repository.observeFavorites(filter) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allFavorites: StateFlow<List<FavoriteEntity>> =
    repository.allFavorites.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      repository.initializeCatalog()
    }
    viewModelScope.launch {
      preferences.collect { prefs ->
        ttsManager.updateSpeechRate(prefs.speechRate)
      }
    }
  }

  fun updateInputText(text: String) {
    _inputText.value = text
    if (text.isBlank()) {
      _translationResult.value = null
    }
  }

  fun setLanguagePair(sourceCode: String, targetCode: String) {
    viewModelScope.launch {
      preferencesRepository.setLanguagePair(sourceCode, targetCode)
      if (_inputText.value.isNotBlank()) {
        translateNow(saveToHistory = false)
      }
    }
  }

  fun swapLanguages() {
    val current = preferences.value
    val nextSource = current.targetLangCode
    val nextTarget = current.sourceLangCode
    val previousSuccess = _translationResult.value as? TranslationResult.Success
    viewModelScope.launch {
      preferencesRepository.setLanguagePair(nextSource, nextTarget)
      if (previousSuccess != null) {
        _inputText.value = previousSuccess.translatedText
        translateNow(saveToHistory = false)
      }
    }
  }

  fun translateNow(saveToHistory: Boolean = true) {
    val text = _inputText.value
    val prefs = preferences.value
    viewModelScope.launch {
      _isTranslating.value = true
      val result =
        repository.translate(
          text = text,
          sourceLangCode = prefs.sourceLangCode,
          targetLangCode = prefs.targetLangCode,
          saveToHistory = saveToHistory,
        )
      _translationResult.value = result
      _isTranslating.value = false
    }
  }

  fun loadInTranslator(
    sourceText: String,
    sourceLangCode: String,
    targetLangCode: String,
  ) {
    _inputText.value = sourceText
    viewModelScope.launch {
      preferencesRepository.setLanguagePair(sourceLangCode, targetLangCode)
      translateNow(saveToHistory = false)
    }
  }

  fun toggleCurrentTranslationFavorite() {
    val res = _translationResult.value as? TranslationResult.Success ?: return
    viewModelScope.launch {
      val added =
        repository.toggleFavorite(
          type = "TRANSLATION",
          sourceLang = res.sourceLangCode,
          targetLang = res.targetLangCode,
          titleText = res.sourceText,
          subtitleText = res.translatedText,
          detailNote = "Couverture ${res.coveragePercent}%",
        )
      _uiMessages.tryEmit(if (added) "Ajouté aux favoris" else "Retiré des favoris")
    }
  }

  fun toggleHistoryFavorite(item: TranslationHistoryEntity) {
    viewModelScope.launch {
      val added =
        repository.toggleFavorite(
          type = "TRANSLATION",
          sourceLang = item.sourceLangCode,
          targetLang = item.targetLangCode,
          titleText = item.sourceText,
          subtitleText = item.translatedText,
          detailNote = "Couverture ${item.coveragePercent}%",
        )
      _uiMessages.tryEmit(if (added) "Traduction ajoutée aux favoris" else "Retirée des favoris")
    }
  }

  fun toggleDictionaryFavorite(entry: DictionaryEntryEntity) {
    viewModelScope.launch {
      val added =
        repository.toggleFavorite(
          type = "WORD",
          sourceLang = entry.languageCode,
          targetLang = entry.languageCode,
          titleText = entry.term,
          subtitleText = entry.definition,
          detailNote = entry.example,
          dictionaryEntryId = entry.id,
        )
      _uiMessages.tryEmit(if (added) "Mot enregistré dans les favoris" else "Mot retiré des favoris")
    }
  }

  fun removeFavorite(id: Long) {
    viewModelScope.launch {
      repository.removeFavorite(id)
      _uiMessages.tryEmit("Favori supprimé")
    }
  }

  fun downloadPack(code: String) {
    viewModelScope.launch {
      val res = repository.downloadPack(code)
      res.onSuccess { count ->
        _uiMessages.tryEmit("Pack '$code' installé ($count entrées hors ligne)")
        if (_inputText.value.isNotBlank()) {
          translateNow(saveToHistory = false)
        }
      }.onFailure { err ->
        _uiMessages.tryEmit(err.message ?: "Échec du téléchargement du pack")
      }
    }
  }

  fun deletePack(code: String) {
    viewModelScope.launch {
      repository.deletePack(code)
      _uiMessages.tryEmit("Pack '$code' supprimé du stockage local")
      if (_inputText.value.isNotBlank()) {
        translateNow(saveToHistory = false)
      }
    }
  }

  fun createCustomLanguage(
    code: String,
    nameFr: String,
    nativeName: String,
    description: String,
  ) {
    viewModelScope.launch {
      repository.createCustomLanguage(code, nameFr, nativeName, description)
        .onSuccess { pack ->
          _uiMessages.tryEmit("Langue '${pack.nameFr}' ajoutée au moteur")
        }
        .onFailure { err ->
          _uiMessages.tryEmit(err.message ?: "Impossible de créer la langue")
        }
    }
  }

  fun addCustomWordPair(
    sourceLangCode: String,
    targetLangCode: String,
    sourceTerm: String,
    targetTerm: String,
    partOfSpeech: String,
    definition: String,
    example: String,
  ) {
    viewModelScope.launch {
      repository.addCustomWordPair(
          sourceLangCode = sourceLangCode,
          targetLangCode = targetLangCode,
          sourceTerm = sourceTerm,
          targetTerm = targetTerm,
          partOfSpeech = partOfSpeech,
          definition = definition,
          example = example,
        )
        .onSuccess {
          _uiMessages.tryEmit("Nouveau mot ajouté aux packs locaux")
        }
        .onFailure { err ->
          _uiMessages.tryEmit(err.message ?: "Impossible d'ajouter cette entrée")
        }
    }
  }

  fun updateDictQuery(query: String) {
    _dictQuery.value = query
  }

  fun updateDictLangFilter(langCode: String) {
    _dictLangFilter.value = langCode
  }

  fun updateDictCategoryFilter(category: String) {
    _dictCategoryFilter.value = category
  }

  fun openDictionaryDetail(entry: DictionaryEntryEntity) {
    viewModelScope.launch {
      val equivalents = repository.getConceptEquivalents(entry.conceptId)
      _selectedDictionaryDetail.value = DictionaryDetailState(entry, equivalents)
    }
  }

  fun closeDictionaryDetail() {
    _selectedDictionaryDetail.value = null
  }

  fun updateHistoryQuery(query: String) {
    _historyQuery.value = query
  }

  fun deleteHistoryItem(id: Long) {
    viewModelScope.launch {
      repository.deleteHistoryItem(id)
    }
  }

  fun clearHistory() {
    viewModelScope.launch {
      repository.clearHistory()
      _uiMessages.tryEmit("Historique effacé")
    }
  }

  fun updateFavoritesFilter(filter: String) {
    _favoritesFilter.value = filter
  }

  fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
  }

  fun setSpeechRate(rate: Float) {
    viewModelScope.launch {
      preferencesRepository.setSpeechRate(rate)
      ttsManager.updateSpeechRate(rate)
    }
  }

  fun speak(text: String, languageCode: String) {
    val notice = ttsManager.speak(text, languageCode)
    if (notice != null) {
      _uiMessages.tryEmit(notice)
    }
  }

  fun stopSpeaking() {
    ttsManager.stop()
  }

  fun notifyMessage(msg: String) {
    _uiMessages.tryEmit(msg)
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
  }

  class Factory(
    private val repository: MahayRahaRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val ttsManager: TtsManager,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MahayRahaViewModel(repository, preferencesRepository, ttsManager) as T
    }
  }
}
