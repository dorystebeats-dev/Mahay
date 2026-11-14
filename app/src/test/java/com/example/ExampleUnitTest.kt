package com.example

import com.example.domain.translation.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun normalizerStripsDiacriticsAndEdgePunctuation() {
    assertEquals("aujourd'hui", TextNormalizer.normalize("  Aujourd’hui ! "))
    assertEquals("ou est", TextNormalizer.normalize("¿Où est...?"))
    assertEquals("aide", TextNormalizer.stripApostrophePrefix("d'aide"))
  }
}
