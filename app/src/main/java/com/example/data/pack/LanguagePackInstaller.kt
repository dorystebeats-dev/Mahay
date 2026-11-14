package com.example.data.pack

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.domain.translation.TextNormalizer
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PackDownloadProgress(
  val languageCode: String,
  val progress: Float,
  val stageLabel: String,
)

class LanguagePackInstaller(
  private val context: Context,
  private val database: AppDatabase,
) {
  private val languagePackDao = database.languagePackDao()
  private val dictionaryDao = database.dictionaryDao()

  private val _activeDownloads = MutableStateFlow<Map<String, PackDownloadProgress>>(emptyMap())
  val activeDownloads: StateFlow<Map<String, PackDownloadProgress>> = _activeDownloads.asStateFlow()

  suspend fun ensureManifestSeeded() = withContext(Dispatchers.IO) {
    val existing = languagePackDao.getAllPacksOnce()
    val manifestJson =
      context.assets.open("packs/manifest.json").bufferedReader().use { it.readText() }
    val root = JSONObject(manifestJson)
    val packsArray = root.getJSONArray("packs")
    val manifestPacks = mutableListOf<LanguagePackEntity>()

    for (i in 0 until packsArray.length()) {
      val item = packsArray.getJSONObject(i)
      val code = item.getString("code")
      val assetFile = item.getString("assetFileName")
      val estimatedBytes = runCatching {
        context.assets.open(assetFile).use { it.available().toLong() }
      }.getOrDefault(18_000L)
      val entryCountEstimate = runCatching {
        val packText = context.assets.open(assetFile).bufferedReader().use { it.readText() }
        JSONObject(packText).getJSONArray("entries").length()
      }.getOrDefault(0)

      val existingPack = existing.find { it.code == code }
      if (existingPack == null) {
        manifestPacks.add(
          LanguagePackEntity(
            code = code,
            nameFr = item.getString("nameFr"),
            nativeName = item.getString("nativeName"),
            regionBadge = item.getString("regionBadge"),
            version = item.getString("version"),
            description = item.getString("description"),
            speechLocaleTag = item.getString("speechLocaleTag"),
            assetFileName = assetFile,
            wordCount = entryCountEstimate,
            sizeBytes = estimatedBytes,
            isInstalled = false,
            isCustom = false,
            downloadedAt = null,
          )
        )
      }
    }

    if (manifestPacks.isNotEmpty()) {
      languagePackDao.insertPacksIgnore(manifestPacks)
    }
  }

  suspend fun installPack(languageCode: String): Result<Int> = withContext(Dispatchers.IO) {
    runCatching {
      val pack =
        languagePackDao.getPackByCode(languageCode)
          ?: error("Pack linguistique introuvable : $languageCode")

      if (pack.isCustom) {
        languagePackDao.updateInstallStatus(
          code = pack.code,
          installed = true,
          wordCount = dictionaryDao.getEntriesForLanguage(pack.code).size,
          sizeBytes = pack.sizeBytes,
          downloadedAt = System.currentTimeMillis(),
        )
        return@runCatching dictionaryDao.getEntriesForLanguage(pack.code).size
      }

      emitProgress(languageCode, 0.12f, "Préparation du pack...")
      delay(180)

      val rawJson =
        context.assets.open(pack.assetFileName).bufferedReader().use { it.readText() }
      emitProgress(languageCode, 0.42f, "Extraction du modèle local...")
      delay(180)

      // Persist local offline pack file in app files directory
      val localPackDir = File(context.filesDir, "offline_packs").apply { mkdirs() }
      val localPackFile = File(localPackDir, "${pack.code}.json")
      localPackFile.writeText(rawJson)

      val jsonObj = JSONObject(rawJson)
      val entriesArray = jsonObj.getJSONArray("entries")
      val parsedEntries = ArrayList<DictionaryEntryEntity>(entriesArray.length())

      for (i in 0 until entriesArray.length()) {
        val obj = entriesArray.getJSONObject(i)
        val term = obj.getString("term")
        val aliasesArr = obj.optJSONArray("aliases")
        val aliases = buildList {
          if (aliasesArr != null) {
            for (j in 0 until aliasesArr.length()) {
              add(aliasesArr.getString(j))
            }
          }
        }
        val normalizedAliases = aliases.map { TextNormalizer.normalize(it) }.filter { it.isNotEmpty() }

        parsedEntries.add(
          DictionaryEntryEntity(
            conceptId = obj.getString("conceptId"),
            languageCode = pack.code,
            term = term,
            normalizedTerm = TextNormalizer.normalize(term),
            aliasesPipe = aliases.joinToString("|"),
            aliasesNormalizedPipe = normalizedAliases.joinToString("|"),
            phonetic = obj.optString("phonetic", ""),
            partOfSpeech = obj.optString("partOfSpeech", "Mot"),
            category = obj.optString("category", "Général"),
            definition = obj.optString("definition", ""),
            example = obj.optString("example", ""),
            exampleGloss = obj.optString("exampleGloss", ""),
            isCustom = false,
          )
        )
      }

      emitProgress(languageCode, 0.78f, "Indexation dans la base locale...")
      delay(180)

      database.withTransaction {
        dictionaryDao.deleteEntriesByLanguage(pack.code)
        dictionaryDao.insertEntries(parsedEntries)
        languagePackDao.updateInstallStatus(
          code = pack.code,
          installed = true,
          wordCount = parsedEntries.size,
          sizeBytes = localPackFile.length(),
          downloadedAt = System.currentTimeMillis(),
        )
      }

      emitProgress(languageCode, 1.0f, "Installé hors ligne")
      delay(140)
      clearProgress(languageCode)
      parsedEntries.size
    }.onFailure {
      clearProgress(languageCode)
    }
  }

  suspend fun uninstallPack(languageCode: String) = withContext(Dispatchers.IO) {
    val pack = languagePackDao.getPackByCode(languageCode) ?: return@withContext
    val localPackFile = File(File(context.filesDir, "offline_packs"), "${pack.code}.json")
    if (localPackFile.exists()) {
      localPackFile.delete()
    }
    val remainingEstimate =
      if (!pack.isCustom && pack.assetFileName.isNotBlank()) {
        runCatching {
          val text = context.assets.open(pack.assetFileName).bufferedReader().use { it.readText() }
          JSONObject(text).getJSONArray("entries").length()
        }.getOrDefault(pack.wordCount)
      } else {
        pack.wordCount
      }

    database.withTransaction {
      dictionaryDao.deleteEntriesByLanguage(languageCode)
      if (pack.isCustom) {
        languagePackDao.deleteCustomPack(languageCode)
      } else {
        languagePackDao.updateInstallStatus(
          code = languageCode,
          installed = false,
          wordCount = remainingEstimate,
          sizeBytes = pack.sizeBytes,
          downloadedAt = null,
        )
      }
    }
  }

  suspend fun createCustomLanguagePack(
    codeInput: String,
    nameFr: String,
    nativeName: String,
    description: String,
  ): Result<LanguagePackEntity> = withContext(Dispatchers.IO) {
    runCatching {
      val cleanCode = codeInput.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "")
      require(cleanCode.length in 2..8) { "Code langue invalide (2 à 8 lettres)." }
      require(nameFr.isNotBlank()) { "Le nom de la langue est requis." }
      val existing = languagePackDao.getPackByCode(cleanCode)
      require(existing == null) { "Une langue avec le code '$cleanCode' existe déjà." }

      val pack =
        LanguagePackEntity(
          code = cleanCode,
          nameFr = nameFr.trim(),
          nativeName = nativeName.trim().ifBlank { nameFr.trim() },
          regionBadge = cleanCode.uppercase().take(3),
          version = "1.0-perso",
          description =
            description.trim().ifBlank { "Pack linguistique personnalisé créé localement." },
          speechLocaleTag = cleanCode,
          assetFileName = "",
          wordCount = 0,
          sizeBytes = 0L,
          isInstalled = true,
          isCustom = true,
          downloadedAt = System.currentTimeMillis(),
        )
      languagePackDao.upsertPack(pack)
      pack
    }
  }

  suspend fun addCustomBilingualEntry(
    sourceLangCode: String,
    targetLangCode: String,
    sourceTerm: String,
    targetTerm: String,
    partOfSpeech: String,
    definition: String,
    example: String,
  ): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
      val srcPack = languagePackDao.getPackByCode(sourceLangCode)
      val tgtPack = languagePackDao.getPackByCode(targetLangCode)
      require(srcPack?.isInstalled == true && tgtPack?.isInstalled == true) {
        "Les deux packs linguistiques doivent être installés pour ajouter un mot."
      }
      require(sourceTerm.isNotBlank() && targetTerm.isNotBlank()) {
        "Le mot source et sa traduction sont obligatoires."
      }

      val sharedConceptId = "custom_${System.currentTimeMillis()}"
      val sourceEntry =
        DictionaryEntryEntity(
          conceptId = sharedConceptId,
          languageCode = sourceLangCode,
          term = sourceTerm.trim(),
          normalizedTerm = TextNormalizer.normalize(sourceTerm),
          aliasesPipe = "",
          aliasesNormalizedPipe = "",
          phonetic = "",
          partOfSpeech = partOfSpeech.ifBlank { "Expression" },
          category = "Personnalisé",
          definition = definition.trim().ifBlank { "Entrée ajoutée par l'utilisateur." },
          example = example.trim(),
          exampleGloss = targetTerm.trim(),
          isCustom = true,
        )
      val targetEntry =
        DictionaryEntryEntity(
          conceptId = sharedConceptId,
          languageCode = targetLangCode,
          term = targetTerm.trim(),
          normalizedTerm = TextNormalizer.normalize(targetTerm),
          aliasesPipe = "",
          aliasesNormalizedPipe = "",
          phonetic = "",
          partOfSpeech = partOfSpeech.ifBlank { "Expression" },
          category = "Personnalisé",
          definition = definition.trim().ifBlank { "Traduction personnalisée associée." },
          example = "",
          exampleGloss = sourceTerm.trim(),
          isCustom = true,
        )

      database.withTransaction {
        dictionaryDao.insertEntry(sourceEntry)
        dictionaryDao.insertEntry(targetEntry)
        val srcCount = dictionaryDao.getEntriesForLanguage(sourceLangCode).size
        val tgtCount = dictionaryDao.getEntriesForLanguage(targetLangCode).size
        languagePackDao.updateInstallStatus(
          code = sourceLangCode,
          installed = true,
          wordCount = srcCount,
          sizeBytes = srcPack.sizeBytes + 256L,
          downloadedAt = srcPack.downloadedAt ?: System.currentTimeMillis(),
        )
        languagePackDao.updateInstallStatus(
          code = targetLangCode,
          installed = true,
          wordCount = tgtCount,
          sizeBytes = tgtPack.sizeBytes + 256L,
          downloadedAt = tgtPack.downloadedAt ?: System.currentTimeMillis(),
        )
      }
    }
  }

  private fun emitProgress(code: String, progress: Float, label: String) {
    _activeDownloads.update { current ->
      current + (code to PackDownloadProgress(code, progress, label))
    }
  }

  private fun clearProgress(code: String) {
    _activeDownloads.update { current -> current - code }
  }
}
