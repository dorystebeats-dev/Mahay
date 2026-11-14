package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TtsUiState(
  val isReady: Boolean = false,
  val isSpeaking: Boolean = false,
  val activeText: String? = null,
  val statusMessage: String? = null,
)

class TtsManager(context: Context) {
  private val appContext = context.applicationContext
  private var tts: TextToSpeech? = null
  private var speechRate: Float = 1.0f

  private val _state = MutableStateFlow(TtsUiState())
  val state: StateFlow<TtsUiState> = _state.asStateFlow()

  init {
    tts =
      TextToSpeech(appContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
              override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = true)
              }

              override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = false, activeText = null)
              }

              @Deprecated("Deprecated in Java")
              override fun onError(utteranceId: String?) {
                _state.value =
                  _state.value.copy(
                    isSpeaking = false,
                    activeText = null,
                    statusMessage = "Impossible de lire ce texte vocalement sur cet appareil.",
                  )
              }
            }
          )
          _state.value = _state.value.copy(isReady = true)
        } else {
          _state.value =
            _state.value.copy(
              isReady = false,
              statusMessage = "Moteur vocal Android indisponible sur cet appareil.",
            )
        }
      }
  }

  fun updateSpeechRate(rate: Float) {
    speechRate = rate.coerceIn(0.6f, 1.5f)
    tts?.setSpeechRate(speechRate)
  }

  fun speak(text: String, languageCode: String): String? {
    val clean = text.trim()
    if (clean.isEmpty()) return null
    val engine = tts
    if (engine == null || !_state.value.isReady) {
      return "Synthèse vocale non prête sur cet appareil."
    }

    val targetLocale = resolveLocale(languageCode)
    val availability = engine.isLanguageAvailable(targetLocale)
    var notice: String? = null

    if (availability < TextToSpeech.LANG_AVAILABLE) {
      // Fallback gracefully when device TTS engine lacks a dedicated voice (e.g. Malagasy)
      val fallbackLocale = Locale.FRENCH
      val fallbackStatus = engine.isLanguageAvailable(fallbackLocale)
      if (fallbackStatus >= TextToSpeech.LANG_AVAILABLE) {
        engine.language = fallbackLocale
      }
      notice =
        if (languageCode == "mg" || languageCode == "sw") {
          "Voix native '$languageCode' absente du système — lecture phonétique approximative."
        } else {
          "Voix système '$languageCode' non installée — lecture par défaut."
        }
    } else {
      engine.language = targetLocale
    }

    engine.setSpeechRate(speechRate)
    _state.value = _state.value.copy(isSpeaking = true, activeText = clean, statusMessage = notice)
    engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "mahay_${System.currentTimeMillis()}")
    return notice
  }

  fun stop() {
    tts?.stop()
    _state.value = _state.value.copy(isSpeaking = false, activeText = null)
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
  }

  private fun resolveLocale(code: String): Locale {
    return when (code.lowercase()) {
      "mg" -> Locale.forLanguageTag("mg-MG")
      "fr" -> Locale.FRENCH
      "en" -> Locale.US
      "es" -> Locale.forLanguageTag("es-ES")
      "de" -> Locale.GERMAN
      "sw" -> Locale.forLanguageTag("sw-KE")
      "it" -> Locale.ITALIAN
      "pt" -> Locale.forLanguageTag("pt-PT")
      else -> Locale.forLanguageTag(code)
    }
  }
}
