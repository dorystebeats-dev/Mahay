package com.example.domain.translation

import java.text.Normalizer

object TextNormalizer {
  private val diacriticsRegex = Regex("\\p{InCombiningDiacriticalMarks}+")
  private val edgePunctuationRegex = Regex("^[\\p{Punct}¿¡«»\"'()\\[\\]]+|[\\p{Punct}¿¡«»\"'()\\[\\]]+$")
  private val multiSpaceRegex = Regex("\\s+")

  fun normalize(input: String): String {
    val decomposed = Normalizer.normalize(input.trim().lowercase(), Normalizer.Form.NFD)
    val withoutAccents = diacriticsRegex.replace(decomposed, "")
    return withoutAccents
      .replace('’', '\'')
      .replace('`', '\'')
      .replace("...", "")
      .replace(edgePunctuationRegex, "")
      .replace(multiSpaceRegex, " ")
      .trim()
  }

  fun stripApostrophePrefix(token: String): String {
    val norm = normalize(token)
    val apostropheIndex = norm.indexOf('\'')
    return if (apostropheIndex in 1..2 && apostropheIndex < norm.length - 1) {
      norm.substring(apostropheIndex + 1)
    } else {
      norm
    }
  }
}
