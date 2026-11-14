package com.example.domain.translation

import com.example.data.local.dao.DictionaryDao
import com.example.data.local.dao.LanguagePackDao
import com.example.data.local.entity.DictionaryEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WordBreakdownItem(
  val sourceSegment: String,
  val translatedSegment: String,
  val matchedEntry: DictionaryEntryEntity?,
  val isTranslated: Boolean,
)

sealed class TranslationResult {
  data class Success(
    val sourceText: String,
    val translatedText: String,
    val sourceLangCode: String,
    val targetLangCode: String,
    val coveragePercent: Int,
    val breakdown: List<WordBreakdownItem>,
    val engineNote: String,
  ) : TranslationResult()

  data class ModelNotInstalled(
    val missingLanguageCodes: List<String>,
  ) : TranslationResult()

  data class EmptyOrError(
    val message: String,
  ) : TranslationResult()
}

interface TranslationEngine {
  suspend fun translate(
    text: String,
    sourceLangCode: String,
    targetLangCode: String,
  ): TranslationResult
}

class OfflineLexiconTranslationEngine(
  private val languagePackDao: LanguagePackDao,
  private val dictionaryDao: DictionaryDao,
) : TranslationEngine {

  private val bridgeStopWords =
    mapOf(
      "fr" to setOf("le", "la", "les", "un", "une", "des", "du", "de", "d", "l", "et", "est", "a", "au", "aux", "en", "dans", "pour", "ce", "cette", "mon", "ma", "mes", "ton", "ta", "tes"),
      "en" to setOf("the", "a", "an", "is", "are", "am", "to", "in", "on", "of", "for", "and", "this", "that", "my", "your"),
      "mg" to setOf("ny", "dia", "sy", "ary", "amin", "ao", "eo", "eny", "aty", "ity", "izany", "no", "fa", "mba", "ve"),
      "es" to setOf("el", "la", "los", "las", "un", "una", "y", "es", "en", "de", "del", "para", "con", "mi", "tu"),
      "de" to setOf("der", "die", "das", "ein", "eine", "und", "ist", "in", "zu", "für", "mit", "mein", "dein"),
      "sw" to setOf("na", "ni", "ya", "wa", "kwa", "katika", "hii", "huyo"),
      "it" to setOf("il", "lo", "la", "i", "gli", "le", "un", "una", "e", "è", "in", "di", "per", "con", "mio"),
      "pt" to setOf("o", "a", "os", "as", "um", "uma", "e", "é", "em", "de", "do", "da", "para", "com", "meu"),
    )

  override suspend fun translate(
    text: String,
    sourceLangCode: String,
    targetLangCode: String,
  ): TranslationResult = withContext(Dispatchers.Default) {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) {
      return@withContext TranslationResult.EmptyOrError("Saisissez un texte à traduire.")
    }

    // Strict verification: NEVER pretend offline translation works if a model is not installed
    val missing = mutableListOf<String>()
    if (languagePackDao.isPackInstalled(sourceLangCode) != true) {
      missing.add(sourceLangCode)
    }
    if (sourceLangCode != targetLangCode && languagePackDao.isPackInstalled(targetLangCode) != true) {
      missing.add(targetLangCode)
    }
    if (missing.isNotEmpty()) {
      return@withContext TranslationResult.ModelNotInstalled(missing)
    }

    if (sourceLangCode == targetLangCode) {
      return@withContext TranslationResult.Success(
        sourceText = trimmed,
        translatedText = trimmed,
        sourceLangCode = sourceLangCode,
        targetLangCode = targetLangCode,
        coveragePercent = 100,
        breakdown = listOf(WordBreakdownItem(trimmed, trimmed, null, true)),
        engineNote = "Langue source et cible identiques.",
      )
    }

    val sourceEntries = dictionaryDao.getEntriesForLanguage(sourceLangCode)
    val targetEntries = dictionaryDao.getEntriesForLanguage(targetLangCode)
    if (sourceEntries.isEmpty() || targetEntries.isEmpty()) {
      val emptyCodes = buildList {
        if (sourceEntries.isEmpty()) add(sourceLangCode)
        if (targetEntries.isEmpty()) add(targetLangCode)
      }
      return@withContext TranslationResult.ModelNotInstalled(emptyCodes)
    }

    val targetByConcept = targetEntries.associateBy { it.conceptId }
    val lookupIndex = buildLookupIndex(sourceEntries)

    // 1. Check full-text exact or normalized phrase match
    val normalizedFull = TextNormalizer.normalize(trimmed)
    val directMatch = lookupIndex[normalizedFull]
    if (directMatch != null) {
      val targetMatch = targetByConcept[directMatch.conceptId]
      if (targetMatch != null) {
        return@withContext TranslationResult.Success(
          sourceText = trimmed,
          translatedText = preserveTerminalPunctuation(trimmed, targetMatch.term),
          sourceLangCode = sourceLangCode,
          targetLangCode = targetLangCode,
          coveragePercent = 100,
          breakdown =
            listOf(
              WordBreakdownItem(
                sourceSegment = trimmed,
                translatedSegment = targetMatch.term,
                matchedEntry = targetMatch,
                isTranslated = true,
              )
            ),
          engineNote = "Correspondance exacte dans le modèle hors ligne.",
        )
      }
    }

    // 2. Clause / n-gram / token greedy offline pipeline
    val tokens = tokenizePreservingWhitespaceAndPunctuation(trimmed)
    val wordIndices = tokens.indices.filter { tokens[it].isWord }
    if (wordIndices.isEmpty()) {
      return@withContext TranslationResult.EmptyOrError("Aucun mot détecté dans la saisie.")
    }

    val outputParts = mutableListOf<String>()
    val breakdown = mutableListOf<WordBreakdownItem>()
    var translatedWords = 0
    var totalMeaningfulWords = 0

    var idx = 0
    while (idx < tokens.size) {
      val current = tokens[idx]
      if (!current.isWord) {
        outputParts.add(current.raw)
        idx++
        continue
      }

      // Try greedy n-gram match up to 5 words ahead
      var matchedSpanWords = 0
      var matchedSourceEntry: DictionaryEntryEntity? = null
      var matchedTargetEntry: DictionaryEntryEntity? = null
      var nextIdxAfterSpan = idx + 1

      val maxWordsWindow = 5
      val candidateIndices = mutableListOf<Int>()
      var scan = idx
      while (scan < tokens.size && candidateIndices.size < maxWordsWindow) {
        if (tokens[scan].isWord) {
          candidateIndices.add(scan)
        } else if (tokens[scan].isHardPunctuation) {
          break
        }
        scan++
      }

      for (spanLen in candidateIndices.size downTo 1) {
        val endTokenIdx = candidateIndices[spanLen - 1]
        val spanRaw = tokens.subList(idx, endTokenIdx + 1).joinToString("") { it.raw }
        val spanNorm = TextNormalizer.normalize(spanRaw)
        val entry =
          lookupIndex[spanNorm]
            ?: if (spanLen == 1) lookupSingleTokenFallback(spanRaw, lookupIndex) else null

        if (entry != null) {
          val tgt = targetByConcept[entry.conceptId]
          if (tgt != null) {
            matchedSpanWords = spanLen
            matchedSourceEntry = entry
            matchedTargetEntry = tgt
            nextIdxAfterSpan = endTokenIdx + 1
            break
          }
        }
      }

      if (matchedTargetEntry != null && matchedSourceEntry != null) {
        val spanRaw = tokens.subList(idx, nextIdxAfterSpan).joinToString("") { it.raw }
        val cleanOut = cleanTemplateEllipsis(matchedTargetEntry.term)
        val cased =
          if (current.raw.firstOrNull()?.isUpperCase() == true && outputParts.none { it.any(Char::isLetter) }) {
            cleanOut.replaceFirstChar { it.uppercase() }
          } else {
            cleanOut.replaceFirstChar { it.lowercase() }
          }
        outputParts.add(cased)
        breakdown.add(
          WordBreakdownItem(
            sourceSegment = spanRaw.trim(),
            translatedSegment = cleanOut,
            matchedEntry = matchedTargetEntry,
            isTranslated = true,
          )
        )
        translatedWords += matchedSpanWords
        totalMeaningfulWords += matchedSpanWords
        idx = nextIdxAfterSpan
      } else {
        val normWord = TextNormalizer.normalize(current.raw)
        val isStopWord = bridgeStopWords[sourceLangCode]?.contains(normWord) == true
        if (isStopWord) {
          // Omit untranslatable pure grammar particle if target uses different syntax,
          // or keep minimal connector where helpful
          idx++
          // Skip following single space to prevent double spaces
          if (idx < tokens.size && tokens[idx].raw.isBlank()) {
            idx++
          }
        } else {
          totalMeaningfulWords++
          outputParts.add(current.raw)
          breakdown.add(
            WordBreakdownItem(
              sourceSegment = current.raw,
              translatedSegment = current.raw,
              matchedEntry = null,
              isTranslated = false,
            )
          )
          idx++
        }
      }
    }

    val assembled =
      outputParts
        .joinToString("")
        .replace(Regex(" {2,}"), " ")
        .replace(Regex(" ([?!.,;:])"), "$1")
        .trim()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val coverage =
      if (totalMeaningfulWords == 0) 100
      else ((translatedWords.toFloat() / totalMeaningfulWords.toFloat()) * 100f).toInt().coerceIn(0, 100)

    val note =
      when {
        coverage == 100 -> "Traduction hors ligne complète via les packs locaux."
        coverage >= 50 -> "Traduction partielle ($coverage%) — certains mots manquent dans le pack local."
        else -> "Couverture limitée ($coverage%) — enrichissez le pack ou consultez le dictionnaire."
      }

    TranslationResult.Success(
      sourceText = trimmed,
      translatedText = assembled.ifBlank { trimmed },
      sourceLangCode = sourceLangCode,
      targetLangCode = targetLangCode,
      coveragePercent = coverage,
      breakdown = breakdown,
      engineNote = note,
    )
  }

  private fun buildLookupIndex(entries: List<DictionaryEntryEntity>): Map<String, DictionaryEntryEntity> {
    val map = LinkedHashMap<String, DictionaryEntryEntity>(entries.size * 2)
    for (entry in entries) {
      if (entry.normalizedTerm.isNotEmpty()) {
        map.putIfAbsent(entry.normalizedTerm, entry)
      }
      if (entry.aliasesNormalizedPipe.isNotEmpty()) {
        for (alias in entry.aliasesNormalizedPipe.split('|')) {
          if (alias.isNotEmpty()) {
            map.putIfAbsent(alias, entry)
          }
        }
      }
    }
    return map
  }

  private fun lookupSingleTokenFallback(
    rawToken: String,
    index: Map<String, DictionaryEntryEntity>,
  ): DictionaryEntryEntity? {
    val stripped = TextNormalizer.stripApostrophePrefix(rawToken)
    if (stripped.isNotEmpty()) {
      index[stripped]?.let { return it }
    }
    val norm = TextNormalizer.normalize(rawToken)
    // Simple plural / verb inflection stem fallback
    val candidates =
      listOfNotNull(
        norm.takeIf { it.length > 3 && it.endsWith("s") }?.dropLast(1),
        norm.takeIf { it.length > 4 && it.endsWith("es") }?.dropLast(2),
        norm.takeIf { it.length > 4 && it.endsWith("er") },
      )
    for (candidate in candidates) {
      index[candidate]?.let { return it }
    }
    return null
  }

  private fun cleanTemplateEllipsis(term: String): String {
    return term.replace("...", "").trim()
  }

  private fun preserveTerminalPunctuation(source: String, translated: String): String {
    val trimmedSource = source.trim()
    val lastSourceChar = trimmedSource.lastOrNull() ?: return translated
    val lastTransChar = translated.lastOrNull()
    return if (lastSourceChar in listOf('?', '!', '.') && lastTransChar !in listOf('?', '!', '.')) {
      "$translated$lastSourceChar"
    } else {
      translated
    }
  }

  private data class Token(
    val raw: String,
    val isWord: Boolean,
    val isHardPunctuation: Boolean,
  )

  private fun tokenizePreservingWhitespaceAndPunctuation(input: String): List<Token> {
    val result = mutableListOf<Token>()
    val regex = Regex("[\\p{L}\\p{N}'’-]+|\\s+|[^\\p{L}\\p{N}\\s'’-]+")
    for (match in regex.findAll(input)) {
      val value = match.value
      val isWord = value.any { it.isLetterOrDigit() }
      val isHardPunct = !isWord && value.any { it in ".!?;:\n" }
      result.add(Token(value, isWord, isHardPunct))
    }
    return result
  }
}
