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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.data.ActionType
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
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val captured = remember(showAppPicker) {
        if (showAppPicker) LastNotificationStore.load(context) else null
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
