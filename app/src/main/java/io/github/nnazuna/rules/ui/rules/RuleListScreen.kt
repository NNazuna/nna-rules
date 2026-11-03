package io.github.nnazuna.rules.ui.rules

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ClipboardOutcome
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import io.github.nnazuna.rules.core.trigger.PowerTrigger
import io.github.nnazuna.rules.core.trigger.TimeTrigger
import io.github.nnazuna.rules.core.trigger.WifiTrigger
import io.github.nnazuna.rules.ui.ActionType
import io.github.nnazuna.rules.ui.toActionType
import io.github.nnazuna.rules.util.ExecutionHistoryStore
import io.github.nnazuna.rules.util.MatchStats
import io.github.nnazuna.rules.util.RuleMatchStat
import io.github.nnazuna.rules.util.hasNotificationAccess
import io.github.nnazuna.rules.util.notificationAccessSettingsIntent
import io.github.nnazuna.rules.util.openAppNotificationSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    onCreateRule: () -> Unit,
    onEditRule: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RuleListViewModel = viewModel(factory = RuleListViewModel.Factory),
) {
    val context = LocalContext.current
    val rules by viewModel.rules.collectAsState()
    val statsRevision by MatchStats.revision.collectAsState()
    val rulesList = rules
    val stats = remember(rulesList, statsRevision) {
        rulesList.associate { it.id to MatchStats.statFor(context, it.id) }
    }
    var granted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var notificationPermissionRequested by remember { mutableStateOf(false) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        granted = hasNotificationAccess(context)
    }

    val needsNotifications = rules.any { rule ->
        rule.actions.any { it is NotifyAction || it is SoundAction }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rules_title)) },
                actions = {
                    TextButton(onClick = { showHistoryDialog = true }) {
                        Text(stringResource(R.string.rules_history))
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                exportedJson = viewModel.exportRules()
                                showExportDialog = true
                            }
                        },
                    ) {
                        Text(stringResource(R.string.rules_export))
                    }
                    TextButton(
                        onClick = {
                            importInputText = ""
                            showImportDialog = true
                        },
                    ) {
                        Text(stringResource(R.string.rules_import))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCreateRule) {
                Text(stringResource(R.string.rules_new))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (needsNotifications && !notificationsEnabled) {
                item(key = "notifications_warning") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.rules_notifications_warning),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                            !notificationPermissionRequested
                                        ) {
                                            notificationPermissionRequested = true
                                            notificationPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS,
                                            )
                                        } else {
                                            openAppNotificationSettings(context)
                                        }
                                    },
                                ) {
                                    Text(stringResource(R.string.rules_allow_notifications))
                                }
                            }
                        }
                    }
                }
            }

            if (!granted) {
                item(key = "access_warning") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.rules_access_warning),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = {
                                        settingsLauncher.launch(
                                            notificationAccessSettingsIntent(),
                                        )
                                    },
                                ) {
                                    Text(stringResource(R.string.rules_open_settings))
                                }
                            }
                        }
                    }
                }
            }

            if (rules.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.rules_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.rules_empty_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(rules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
                        stat = stats[rule.id] ?: RuleMatchStat.Empty,
                        onToggle = { enabled -> viewModel.setEnabled(rule, enabled) },
                        onEdit = { onEditRule(rule.id) },
                    )
                }
            }
        }
    }

    if (showHistoryDialog) {
        val historyEvents by ExecutionHistoryStore.events.collectAsState()
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text(stringResource(R.string.rules_history_title)) },
            text = {
                if (historyEvents.isEmpty()) {
                    Text(
                        text = stringResource(R.string.rules_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(historyEvents) { event ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = event.ruleLabel,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = DateUtils.getRelativeTimeSpanString(event.timestamp).toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = event.detail,
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
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                if (historyEvents.isNotEmpty()) {
                    TextButton(onClick = { ExecutionHistoryStore.clear(context) }) {
                        Text(stringResource(R.string.rules_history_clear))
                    }
                }
            },
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.rules_export_title)) },
            text = {
                OutlinedTextField(
                    value = exportedJson,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    minLines = 6,
                    maxLines = 12,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText("NNA Rules", exportedJson))
                        Toast.makeText(context, context.getString(R.string.rules_export_copied), Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                ) {
                    Text(stringResource(R.string.copy))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.rules_import_title)) },
            text = {
                OutlinedTextField(
                    value = importInputText,
                    onValueChange = { importInputText = it },
                    placeholder = { Text(stringResource(R.string.rules_import_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    minLines = 4,
                    maxLines = 8,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = viewModel.importRules(importInputText)
                            result.onSuccess { count ->
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.rules_import_success, count),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                showImportDialog = false
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.rules_import_error),
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        }
                    },
                    enabled = importInputText.isNotBlank(),
                ) {
                    Text(stringResource(R.string.import_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun RuleCard(
    rule: RuleDefinition,
    stat: RuleMatchStat,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyword = rule.conditions.filterIsInstance<KeywordCondition>().firstOrNull()?.keyword
    val primaryAction = rule.actions.firstOrNull()
    val actionType = primaryAction?.toActionType()
    val clipboardAction = primaryAction as? ClipboardAction

    val trigger = rule.trigger
    val triggerSummary = when (trigger) {
        is NotificationTrigger -> keyword
            ?.takeIf { it.isNotBlank() }
            ?.let { stringResource(R.string.rule_keyword_summary, it) }
            ?: stringResource(R.string.rule_any_notification)
        is TimeTrigger -> stringResource(R.string.trigger_time) + ": %02d:%02d".format(trigger.hour, trigger.minute)
        is WifiTrigger -> stringResource(R.string.trigger_wifi) + ": ${trigger.ssid}"
        is PowerTrigger -> if (trigger.connected) stringResource(R.string.trigger_power_connected) else stringResource(R.string.trigger_power_disconnected)
        else -> stringResource(R.string.rule_any_notification)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = rule.appLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = triggerSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (actionType != null) {
                    Text(
                        text = stringResource(actionType.labelRes()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (clipboardAction != null && clipboardAction.mode == ClipboardMode.EXTRACTED) {
                    Text(
                        text = stringResource(
                            if (clipboardAction.extractionMode == ExtractionMode.CUSTOM_REGEX) {
                                R.string.rule_extract_custom_summary
                            } else {
                                R.string.rule_extract_otp_summary
                            },
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val lastMatchedAt = stat.lastMatchedAt
                if (stat.count > 0 && lastMatchedAt != null) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.rules_match_count,
                            stat.count,
                            stat.count,
                            DateUtils.getRelativeTimeSpanString(lastMatchedAt).toString(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (clipboardAction != null) {
                    stat.clipboardOutcome?.let { outcome ->
                        Text(
                            text = stringResource(
                                when (outcome) {
                                    ClipboardOutcome.EXTRACTED ->
                                        R.string.rules_last_copy_extracted
                                    ClipboardOutcome.FULL ->
                                        R.string.rules_last_copy_full
                                    ClipboardOutcome.FALLBACK_NO_MATCH ->
                                        R.string.rules_last_copy_no_match
                                    ClipboardOutcome.FALLBACK_INVALID_PATTERN ->
                                        R.string.rules_last_copy_invalid
                                },
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}
