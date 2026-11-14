package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DictionaryEntryEntity
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.pack.PackDownloadProgress
import com.example.domain.translation.TranslationResult
import com.example.ui.components.LanguageCodeBadge
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandTurquoise
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning

private enum class SelectorTarget {
  SOURCE,
  TARGET,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslatorScreen(
  allPacks: List<LanguagePackEntity>,
  activeDownloads: Map<String, PackDownloadProgress>,
  sourceLangCode: String,
  targetLangCode: String,
  inputText: String,
  translationResult: TranslationResult?,
  isTranslating: Boolean,
  isCurrentFavorite: Boolean,
  sourceSuggestions: List<DictionaryEntryEntity>,
  onUpdateInput: (String) -> Unit,
  onSelectLanguagePair: (String, String) -> Unit,
  onSwapLanguages: () -> Unit,
  onTranslate: () -> Unit,
  onDownloadPack: (String) -> Unit,
  onToggleFavorite: () -> Unit,
  onSpeak: (String, String) -> Unit,
  onCopy: (String) -> Unit,
  onOpenPackManager: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var activeSelector by remember { mutableStateOf<SelectorTarget?>(null) }
  val clipboardManager = LocalClipboardManager.current

  val sourcePack = allPacks.find { it.code == sourceLangCode }
  val targetPack = allPacks.find { it.code == targetLangCode }
  val missingCodes =
    buildList {
      if (sourcePack?.isInstalled != true) add(sourceLangCode)
      if (targetLangCode != sourceLangCode && targetPack?.isInstalled != true) add(targetLangCode)
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Language Pair Selector Bar
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            LanguageChipButton(
              pack = sourcePack,
              fallbackCode = sourceLangCode,
              onClick = { activeSelector = SelectorTarget.SOURCE },
              modifier = Modifier.weight(1f).testTag("source_lang_button"),
            )
            FilledTonalIconButton(
              onClick = onSwapLanguages,
              modifier = Modifier.padding(horizontal = 6.dp).testTag("swap_languages_button"),
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Inverser les langues",
              )
            }
            LanguageChipButton(
              pack = targetPack,
              fallbackCode = targetLangCode,
              onClick = { activeSelector = SelectorTarget.TARGET },
              modifier = Modifier.weight(1f).testTag("target_lang_button"),
            )
          }
        }
      }

      // Honest Guard Banner if source or target language model is NOT installed
      if (missingCodes.isNotEmpty()) {
        item {
          Card(
            shape = MaterialTheme.shapes.large,
            colors =
              CardDefaults.cardColors(
                containerColor = StatusWarning.copy(alpha = 0.12f),
              ),
            border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth().testTag("missing_model_banner"),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.WarningAmber,
                  contentDescription = null,
                  tint = StatusWarning,
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Modèle hors ligne requis",
                    style = MaterialTheme.typography.titleMedium,
                  )
                  Text(
                    text =
                      "La traduction hors ligne ne peut pas fonctionner tant que les packs sélectionnés ne sont pas installés localement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              missingCodes.forEach { code ->
                val missingPack = allPacks.find { it.code == code }
                val progress = activeDownloads[code]
                Surface(
                  color = MaterialTheme.colorScheme.surface,
                  shape = MaterialTheme.shapes.medium,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      LanguageCodeBadge(badge = code, isInstalled = false)
                      Spacer(modifier = Modifier.width(10.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = missingPack?.nameFr ?: code.uppercase(),
                          style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                          text = "Non installé sur l'appareil",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                      if (progress == null) {
                        Button(
                          onClick = { onDownloadPack(code) },
                          modifier = Modifier.testTag("translator_download_$code"),
                        ) {
                          Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Text("Télécharger")
                        }
                      }
                    }
                    if (progress != null) {
                      Spacer(modifier = Modifier.height(8.dp))
                      LinearProgressIndicator(
                        progress = { progress.progress },
                        modifier = Modifier.fillMaxWidth().clip(CircleShape),
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = progress.stageLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                      )
                    }
                  }
                }
              }

              OutlinedButton(
                onClick = onOpenPackManager,
                modifier = Modifier.align(Alignment.End),
              ) {
                Text("Gérer tous les packs")
              }
            }
          }
        }
      }

      // Source Input Card
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = sourcePack?.nameFr ?: sourceLangCode.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
              Spacer(modifier = Modifier.weight(1f))
              IconButton(
                onClick = {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    onUpdateInput(clip)
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Coller",
                )
              }
              if (inputText.isNotEmpty()) {
                IconButton(onClick = { onSpeak(inputText, sourceLangCode) }) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Écouter le texte source",
                  )
                }
                IconButton(onClick = { onUpdateInput("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Effacer la saisie",
                  )
                }
              }
            }

            OutlinedTextField(
              value = inputText,
              onValueChange = onUpdateInput,
              placeholder = {
                Text("Saisissez un mot, une salutation ou une phrase…")
              },
              minLines = 3,
              maxLines = 6,
              modifier = Modifier.fillMaxWidth().testTag("translator_input"),
              shape = MaterialTheme.shapes.medium,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${inputText.length} caractères",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.weight(1f))
              Button(
                onClick = onTranslate,
                enabled = inputText.isNotBlank() && !isTranslating,
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                  ),
                modifier = Modifier.testTag("translate_button"),
              ) {
                if (isTranslating) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Traduire")
              }
            }
          }
        }
      }

      // Quick Suggestions from installed source pack
      if (sourceSuggestions.isNotEmpty() && missingCodes.isEmpty()) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Suggestions hors ligne (${sourcePack?.nameFr ?: sourceLangCode})",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(sourceSuggestions.take(10), key = { it.id }) { suggestion ->
                SuggestionChip(
                  onClick = {
                    onUpdateInput(suggestion.term)
                    onTranslate()
                  },
                  label = { Text(suggestion.term) },
                )
              }
            }
          }
        }
      }

      // Translation Result Card
      if (translationResult != null) {
        item {
          when (translationResult) {
            is TranslationResult.Success -> {
              Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, BrandTurquoise.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("translation_result_card"),
              ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Box(
                    modifier =
                      Modifier
                        .fillMaxWidth()
                        .background(
                          Brush.linearGradient(
                            colors = listOf(BrandBlue, BrandViolet, BrandTurquoise),
                          )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = targetPack?.nameFr ?: targetLangCode.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                      )
                      Spacer(modifier = Modifier.weight(1f))
                      Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                          )
                          Text(
                            text = "${translationResult.coveragePercent}% hors ligne",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                          )
                        }
                      }
                    }
                  }

                  Column(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                    Text(
                      text = translationResult.translatedText,
                      style = MaterialTheme.typography.headlineSmall,
                      modifier = Modifier.testTag("translated_text_output"),
                    )
                    Text(
                      text = translationResult.engineNote,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      FilledTonalIconButton(
                        onClick = {
                          onSpeak(
                            translationResult.translatedText,
                            translationResult.targetLangCode,
                          )
                        },
                        modifier = Modifier.testTag("speak_translation_button"),
                      ) {
                        Icon(
                          imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                          contentDescription = "Prononcer la traduction",
                        )
                      }
                      FilledTonalIconButton(
                        onClick = { onCopy(translationResult.translatedText) },
                        modifier = Modifier.testTag("copy_translation_button"),
                      ) {
                        Icon(
                          imageVector = Icons.Default.ContentCopy,
                          contentDescription = "Copier la traduction",
                        )
                      }
                      FilledTonalIconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("favorite_translation_button"),
                      ) {
                        Icon(
                          imageVector =
                            if (isCurrentFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                          contentDescription = "Enregistrer en favori",
                          tint =
                            if (isCurrentFavorite) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }

                    if (translationResult.breakdown.isNotEmpty()) {
                      Text(
                        text = "Analyse mot à mot",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                      )
                      FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        translationResult.breakdown.forEach { seg ->
                          Surface(
                            color =
                              if (seg.isTranslated) StatusSuccess.copy(alpha = 0.12f)
                              else MaterialTheme.colorScheme.surfaceVariant,
                            border =
                              BorderStroke(
                                1.dp,
                                if (seg.isTranslated) StatusSuccess.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                              ),
                            shape = MaterialTheme.shapes.small,
                          ) {
                            Text(
                              text =
                                if (seg.isTranslated) "${seg.sourceSegment} → ${seg.translatedSegment}"
                                else "${seg.sourceSegment} (non indexé)",
                              style = MaterialTheme.typography.bodySmall,
                              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }

            is TranslationResult.ModelNotInstalled -> {
              // Handled cleanly by the top missing_model_banner and explicit feedback
              Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text =
                    "Traduction bloquée : installez d'abord le(s) modèle(s) ${translationResult.missingLanguageCodes.joinToString(", ") { it.uppercase() }}.",
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(14.dp),
                )
              }
            }

            is TranslationResult.EmptyOrError -> {
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = translationResult.message,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.padding(14.dp),
                )
              }
            }
          }
        }
      }
    }
  }

  val selectorTarget = activeSelector
  if (selectorTarget != null) {
    LanguageSelectorSheet(
      title =
        if (selectorTarget == SelectorTarget.SOURCE) "Langue source"
        else "Langue cible",
      packs = allPacks,
      selectedCode =
        if (selectorTarget == SelectorTarget.SOURCE) sourceLangCode else targetLangCode,
      activeDownloads = activeDownloads,
      onSelect = { chosen ->
        if (selectorTarget == SelectorTarget.SOURCE) {
          onSelectLanguagePair(chosen, targetLangCode)
        } else {
          onSelectLanguagePair(sourceLangCode, chosen)
        }
      },
      onDownloadPack = onDownloadPack,
      onDismiss = { activeSelector = null },
    )
  }
}

@Composable
private fun LanguageChipButton(
  pack: LanguagePackEntity?,
  fallbackCode: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val installed = pack?.isInstalled == true
  Surface(
    modifier = modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    shape = MaterialTheme.shapes.medium,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      LanguageCodeBadge(badge = pack?.regionBadge ?: fallbackCode, isInstalled = installed)
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = pack?.nameFr ?: fallbackCode.uppercase(),
          style = MaterialTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = if (installed) "Hors ligne" else "À télécharger",
          style = MaterialTheme.typography.labelSmall,
          color = if (installed) StatusSuccess else StatusWarning,
          maxLines = 1,
        )
      }
    }
  }
}
