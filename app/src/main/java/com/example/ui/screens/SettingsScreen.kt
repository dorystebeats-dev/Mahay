package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LanguagePackEntity
import com.example.data.pack.PackDownloadProgress
import com.example.data.preferences.ThemeMode
import com.example.ui.components.LanguageCodeBadge
import com.example.ui.components.MahayRahaBrandEmblem
import com.example.ui.components.formatBytesLabel
import com.example.ui.theme.StatusSuccess

@Composable
fun SettingsScreen(
  allPacks: List<LanguagePackEntity>,
  installedPacks: List<LanguagePackEntity>,
  activeDownloads: Map<String, PackDownloadProgress>,
  themeMode: ThemeMode,
  speechRate: Float,
  onDownloadPack: (String) -> Unit,
  onDeletePack: (String) -> Unit,
  onCreateCustomLanguage: (code: String, nameFr: String, nativeName: String, desc: String) -> Unit,
  onAddCustomWordPair:
    (
      srcLang: String,
      tgtLang: String,
      srcTerm: String,
      tgtTerm: String,
      pos: String,
      def: String,
      ex: String,
    ) -> Unit,
  onSelectThemeMode: (ThemeMode) -> Unit,
  onChangeSpeechRate: (Float) -> Unit,
  onTestVoice: () -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showAddLanguageDialog by remember { mutableStateOf(false) }
  var showAddWordDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Section: Offline Language Packs
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Packs de langues hors ligne",
            style = MaterialTheme.typography.titleLarge,
          )
          Text(
            text =
              "Téléchargez ou supprimez les modèles locaux. Sans pack installé, aucune traduction hors ligne n'est effectuée.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      items(allPacks, key = { it.code }) { pack ->
        val download = activeDownloads[pack.code]
        Card(
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border =
            BorderStroke(
              1.dp,
              if (pack.isInstalled) StatusSuccess.copy(alpha = 0.45f)
              else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth(),
            ) {
              LanguageCodeBadge(badge = pack.regionBadge, isInstalled = pack.isInstalled)
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${pack.nameFr} (${pack.nativeName})",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text =
                    "v${pack.version} • ${pack.wordCount} entrées • ${formatBytesLabel(pack.sizeBytes)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (pack.isInstalled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Installé",
                    tint = StatusSuccess,
                    modifier = Modifier.size(18.dp),
                  )
                  IconButton(
                    onClick = { onDeletePack(pack.code) },
                    modifier = Modifier.testTag("delete_pack_${pack.code}"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.DeleteOutline,
                      contentDescription = "Supprimer le pack ${pack.nameFr}",
                      tint = MaterialTheme.colorScheme.error,
                    )
                  }
                }
              } else if (download == null) {
                Button(
                  onClick = { onDownloadPack(pack.code) },
                  modifier = Modifier.testTag("settings_download_${pack.code}"),
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
            Text(
              text = pack.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (download != null) {
              LinearProgressIndicator(
                progress = { download.progress },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
              )
              Text(
                text = download.stageLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      // Section: Extensible Engine Actions
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
              )
              Text(
                text = "Moteur linguistique extensible",
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Text(
              text =
                "Ajoutez une nouvelle langue (ou dialecte régional) et enrichissez votre lexique hors ligne avec vos propres mots.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = { showAddLanguageDialog = true },
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nouvelle langue")
              }
              Button(
                onClick = { showAddWordDialog = true },
                enabled = installedPacks.size >= 2,
                modifier = Modifier.weight(1f),
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter un mot")
              }
            }
          }
        }
      }

      // Section: Voice Pronunciation (TTS)
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Prononciation vocale",
                style = MaterialTheme.typography.titleMedium,
              )
              Spacer(modifier = Modifier.weight(1f))
              Text(
                text = String.format("%.1fx", speechRate),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Slider(
              value = speechRate,
              onValueChange = onChangeSpeechRate,
              valueRange = 0.6f..1.5f,
            )
            OutlinedButton(
              onClick = onTestVoice,
              modifier = Modifier.align(Alignment.End),
            ) {
              Text("Tester la voix")
            }
          }
        }
      }

      // Section: Theme & Storage
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
              )
              Text(
                text = "Apparence & Données",
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onSelectThemeMode(ThemeMode.SYSTEM) },
                label = { Text("Système") },
              )
              FilterChip(
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onSelectThemeMode(ThemeMode.LIGHT) },
                label = { Text("Clair") },
              )
              FilterChip(
                selected = themeMode == ThemeMode.DARK,
                onClick = { onSelectThemeMode(ThemeMode.DARK) },
                label = { Text("Sombre") },
              )
            }
            HorizontalDivider()
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Historique local des traductions",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
              )
              TextButton(onClick = onClearHistory) {
                Text("Vider", color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }

      // Section: About MAHAY RAHA
      item {
        Card(
          shape = MaterialTheme.shapes.large,
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            MahayRahaBrandEmblem(size = 52.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "MAHAY RAHA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "Traduire, apprendre et comprendre.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Base locale SQLite (Room) • Modèles extensibles hors ligne",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }

  if (showAddLanguageDialog) {
    AddCustomLanguageDialog(
      onConfirm = { code, nameFr, nativeName, desc ->
        onCreateCustomLanguage(code, nameFr, nativeName, desc)
        showAddLanguageDialog = false
      },
      onDismiss = { showAddLanguageDialog = false },
    )
  }

  if (showAddWordDialog) {
    AddCustomWordDialog(
      installedPacks = installedPacks,
      onConfirm = { src, tgt, srcTerm, tgtTerm, pos, def, ex ->
        onAddCustomWordPair(src, tgt, srcTerm, tgtTerm, pos, def, ex)
        showAddWordDialog = false
      },
      onDismiss = { showAddWordDialog = false },
    )
  }
}

@Composable
private fun AddCustomLanguageDialog(
  onConfirm: (String, String, String, String) -> Unit,
  onDismiss: () -> Unit,
) {
  var code by remember { mutableStateOf("") }
  var nameFr by remember { mutableStateOf("") }
  var nativeName by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Ajouter une langue") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = code,
          onValueChange = { code = it },
          label = { Text("Code (ex: jp, ar, mlg-s)") },
          singleLine = true,
        )
        OutlinedTextField(
          value = nameFr,
          onValueChange = { nameFr = it },
          label = { Text("Nom en français") },
          singleLine = true,
        )
        OutlinedTextField(
          value = nativeName,
          onValueChange = { nativeName = it },
          label = { Text("Nom natif") },
          singleLine = true,
        )
        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          label = { Text("Description (optionnel)") },
          maxLines = 2,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(code, nameFr, nativeName, desc) },
        enabled = code.trim().length >= 2 && nameFr.isNotBlank(),
      ) {
        Text("Créer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    },
  )
}

@Composable
private fun AddCustomWordDialog(
  installedPacks: List<LanguagePackEntity>,
  onConfirm: (String, String, String, String, String, String, String) -> Unit,
  onDismiss: () -> Unit,
) {
  var sourceCode by remember { mutableStateOf(installedPacks.firstOrNull()?.code ?: "mg") }
  var targetCode by remember {
    mutableStateOf(installedPacks.getOrNull(1)?.code ?: installedPacks.firstOrNull()?.code ?: "fr")
  }
  var sourceTerm by remember { mutableStateOf("") }
  var targetTerm by remember { mutableStateOf("") }
  var definition by remember { mutableStateOf("") }
  var example by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Ajouter un mot bilingue") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Paire : ${sourceCode.uppercase()} → ${targetCode.uppercase()}",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          installedPacks.take(4).forEach { pack ->
            FilterChip(
              selected = sourceCode == pack.code,
              onClick = { sourceCode = pack.code },
              label = { Text(pack.regionBadge) },
            )
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          installedPacks.take(4).forEach { pack ->
            FilterChip(
              selected = targetCode == pack.code,
              onClick = { targetCode = pack.code },
              label = { Text(pack.regionBadge) },
            )
          }
        }
        OutlinedTextField(
          value = sourceTerm,
          onValueChange = { sourceTerm = it },
          label = { Text("Mot en ${sourceCode.uppercase()}") },
          singleLine = true,
        )
        OutlinedTextField(
          value = targetTerm,
          onValueChange = { targetTerm = it },
          label = { Text("Traduction en ${targetCode.uppercase()}") },
          singleLine = true,
        )
        OutlinedTextField(
          value = definition,
          onValueChange = { definition = it },
          label = { Text("Définition (optionnel)") },
          maxLines = 2,
        )
        OutlinedTextField(
          value = example,
          onValueChange = { example = it },
          label = { Text("Exemple de phrase (optionnel)") },
          maxLines = 2,
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            sourceCode,
            targetCode,
            sourceTerm,
            targetTerm,
            "Expression",
            definition,
            example,
          )
        },
        enabled = sourceTerm.isNotBlank() && targetTerm.isNotBlank() && sourceCode != targetCode,
      ) {
        Text("Enregistrer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Annuler") }
    },
  )
}
