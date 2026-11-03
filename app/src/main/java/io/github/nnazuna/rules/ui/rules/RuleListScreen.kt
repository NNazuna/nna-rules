package io.github.nnazuna.rules.ui.rules

import android.Manifest
import android.os.Build
import android.text.format.DateUtils
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.data.ActionType
import io.github.nnazuna.rules.data.Rule
import io.github.nnazuna.rules.ui.labelRes
import io.github.nnazuna.rules.util.MatchStats
import io.github.nnazuna.rules.util.RuleMatchStat
import io.github.nnazuna.rules.util.hasNotificationAccess
import io.github.nnazuna.rules.util.notificationAccessSettingsIntent
import io.github.nnazuna.rules.util.openAppNotificationSettings

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
    val stats = remember(rules, statsRevision) {
        rules.associate { it.id to MatchStats.statFor(context, it.id) }
    }
    var granted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var notificationPermissionRequested by remember { mutableStateOf(false) }

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

    val needsNotifications = rules.any {
        it.action == ActionType.NOTIFY || it.action == ActionType.SOUND
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.rules_title)) })
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
}

@Composable
private fun RuleCard(
    rule: Rule,
    stat: RuleMatchStat,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    text = rule.keyword
                        ?.takeIf { it.isNotBlank() }
                        ?.let { stringResource(R.string.rule_keyword_summary, it) }
                        ?: stringResource(R.string.rule_any_notification),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(rule.action.labelRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
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
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}
