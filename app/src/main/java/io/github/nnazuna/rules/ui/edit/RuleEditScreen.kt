package io.github.nnazuna.rules.ui.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.data.ActionType
import io.github.nnazuna.rules.extraction.ClipboardMode
import io.github.nnazuna.rules.extraction.ClipboardOutcome
import io.github.nnazuna.rules.extraction.ExtractionMode
import io.github.nnazuna.rules.extraction.TextExtractor
import io.github.nnazuna.rules.ui.descriptionRes
import io.github.nnazuna.rules.ui.labelRes
import io.github.nnazuna.rules.util.AppInfo
import io.github.nnazuna.rules.util.CapturedNotification
import io.github.nnazuna.rules.util.LastNotificationStore
import io.github.nnazuna.rules.util.loadLaunchableApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    ruleId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RuleEditViewModel = viewModel(factory = RuleEditViewModel.factory(ruleId)),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    var showAppPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var sampleText by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val captured = remember(showAppPicker) {
        if (showAppPicker) LastNotificationStore.load(context) else null
    }
    val lastNotification = remember { LastNotificationStore.load(context) }
    val preview = remember(
        sampleText,
        uiState.action,
        uiState.clipboardMode,
        uiState.extractionMode,
        uiState.customRegex,
    ) {
        if (sampleText.isBlank()) {
            null
        } else {
            TextExtractor.resolveForClipboard(
                text = sampleText,
                mode = uiState.clipboardMode,
                extractionMode = uiState.extractionMode,
                customRegex = uiState.customRegex,
            )
        }
    }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isNew) R.string.rule_add_title else R.string.rule_edit_title,
                        ),
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save(onSaved = onBack) },
                        enabled = uiState.canSave,
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.rule_trigger_section),
                style = MaterialTheme.typography.titleSmall,
            )

            OutlinedButton(
                onClick = { showAppPicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = uiState.appLabel.ifBlank {
                        stringResource(R.string.rule_choose_app)
                    },
                )
            }

            if (uiState.packageName.isNotBlank()) {
                Text(
                    text = uiState.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = uiState.keyword,
                onValueChange = viewModel::onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.rule_keyword_label)) },
                supportingText = { Text(stringResource(R.string.rule_keyword_hint)) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.rule_action_section),
                style = MaterialTheme.typography.titleSmall,
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionType.entries.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.onActionChange(action) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = uiState.action == action,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(action.labelRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = stringResource(action.descriptionRes()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (uiState.usesClipboard) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = stringResource(R.string.clipboard_section),
                    style = MaterialTheme.typography.titleSmall,
                )

                ChoiceRow(
                    title = stringResource(R.string.clipboard_mode_full),
                    description = stringResource(R.string.clipboard_mode_full_description),
                    selected = uiState.clipboardMode == ClipboardMode.FULL,
                    onSelect = { viewModel.onClipboardModeChange(ClipboardMode.FULL) },
                )
                ChoiceRow(
                    title = stringResource(R.string.clipboard_mode_extracted),
                    description = stringResource(R.string.clipboard_mode_extracted_description),
                    selected = uiState.clipboardMode == ClipboardMode.EXTRACTED,
                    onSelect = { viewModel.onClipboardModeChange(ClipboardMode.EXTRACTED) },
                )

                if (uiState.extractsCode) {
                    Text(
                        text = stringResource(R.string.clipboard_extraction_section),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    ChoiceRow(
                        title = stringResource(R.string.clipboard_extraction_otp),
                        description = stringResource(R.string.clipboard_extraction_otp_description),
                        selected = uiState.extractionMode == ExtractionMode.OTP,
                        onSelect = { viewModel.onExtractionModeChange(ExtractionMode.OTP) },
                    )
                    ChoiceRow(
                        title = stringResource(R.string.clipboard_extraction_custom),
                        description = stringResource(R.string.clipboard_extraction_custom_description),
                        selected = uiState.extractionMode == ExtractionMode.CUSTOM_REGEX,
                        onSelect = {
                            viewModel.onExtractionModeChange(ExtractionMode.CUSTOM_REGEX)
                        },
                    )
                    if (uiState.usesCustomRegex) {
                        OutlinedTextField(
                            value = uiState.customRegex,
                            onValueChange = viewModel::onCustomRegexChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = uiState.regexError,
                            label = { Text(stringResource(R.string.clipboard_regex_label)) },
                            supportingText = {
                                Text(
                                    stringResource(
                                        if (uiState.regexError) {
                                            R.string.clipboard_regex_error
                                        } else {
                                            R.string.clipboard_regex_hint
                                        },
                                    ),
                                )
                            },
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.clearClipboardSeconds,
                    onValueChange = viewModel::onClearClipboardSecondsChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(stringResource(R.string.clipboard_clear_label)) },
                    supportingText = { Text(stringResource(R.string.clipboard_clear_hint)) },
                )

                Text(
                    text = stringResource(R.string.clipboard_test_section),
                    style = MaterialTheme.typography.titleSmall,
                )
                OutlinedTextField(
                    value = sampleText,
                    onValueChange = { sampleText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    label = { Text(stringResource(R.string.clipboard_test_sample_label)) },
                )
                if (lastNotification != null) {
                    TextButton(onClick = { sampleText = lastNotification.text }) {
                        Text(stringResource(R.string.clipboard_test_use_last))
                    }
                }
                when {
                    preview == null -> Text(
                        text = stringResource(R.string.clipboard_test_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    preview.outcome == ClipboardOutcome.EXTRACTED -> Text(
                        text = stringResource(
                            R.string.clipboard_test_preview_code,
                            preview.text,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                    )
                    preview.outcome == ClipboardOutcome.FULL -> Text(
                        text = stringResource(R.string.clipboard_test_preview_full),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    preview.outcome == ClipboardOutcome.FALLBACK_INVALID_PATTERN -> Text(
                        text = stringResource(R.string.clipboard_test_preview_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    else -> Text(
                        text = stringResource(R.string.clipboard_test_preview_no_match),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.rule_enabled_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.rule_enabled_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = viewModel::onEnabledChange,
                )
            }

            if (!uiState.isNew) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.rule_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            apps = apps,
            captured = captured,
            onDismiss = { showAppPicker = false },
            onSelect = { app ->
                viewModel.onAppSelected(app.packageName, app.label)
                showAppPicker = false
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.rule_delete_title)) },
            text = { Text(stringResource(R.string.rule_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete(onDeleted = onBack)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.rule_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<AppInfo>,
    captured: CapturedNotification?,
    onDismiss: () -> Unit,
    onSelect: (AppInfo) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rule_choose_app)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (captured != null) {
                    TextButton(
                        onClick = {
                            onSelect(
                                AppInfo(
                                    packageName = captured.packageName,
                                    label = captured.appLabel,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                R.string.rule_use_last_notification,
                                captured.appLabel,
                            ),
                        )
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.rule_app_search)) },
                )
                if (filtered.isEmpty()) {
                    Text(
                        text = stringResource(R.string.rule_no_apps),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(filtered, key = { it.packageName }) { app ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(app) }
                                    .padding(vertical = 10.dp),
                            ) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun ChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
