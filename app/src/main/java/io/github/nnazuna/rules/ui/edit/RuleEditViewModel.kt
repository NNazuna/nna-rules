package io.github.nnazuna.rules.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nnazuna.rules.NnaRulesApp
import io.github.nnazuna.rules.ui.ActionType
import io.github.nnazuna.rules.core.action.BrightnessAction
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.CsvAppendAction
import io.github.nnazuna.rules.core.action.DigestAction
import io.github.nnazuna.rules.core.action.DndAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.action.VolumeAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.extraction.TextExtractor
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import io.github.nnazuna.rules.core.trigger.PowerTrigger
import io.github.nnazuna.rules.core.trigger.TimeTrigger
import io.github.nnazuna.rules.core.trigger.WifiTrigger
import io.github.nnazuna.rules.data.RuleRepository
import kotlinx.coroutines.launch

enum class RulePreset {
    OTP_COPY,
    EXPENSE_LOGGER,
    CHARGER_QUIET,
    SLEEP_SCHEDULE,
    WORK_WIFI,
}

data class RuleEditUiState(
    val id: Long = 0,
    val isLoading: Boolean = false,
    val enabled: Boolean = true,
    val appLabel: String = "",
    // Trigger fields
    val triggerType: String = NotificationTrigger.TYPE,
    val triggerPackage: String = "",
    val triggerHour: Int = 12,
    val triggerMinute: Int = 0,
    val triggerSsid: String = "",
    val triggerConnected: Boolean = true,
    // Action fields
    val actionType: ActionType = ActionType.NOTIFY,
    val keyword: String = "",
    val clipboardMode: ClipboardMode = ClipboardMode.FULL,
    val extractionMode: ExtractionMode = ExtractionMode.OTP,
    val customRegex: String = "",
    val clearClipboardSeconds: String = "0",
    val volumeLevel: Int = 50,
    val volumeStream: String = "RING",
    val brightnessLevel: Int = 100,
    val dndMode: String = "OFF",
    val csvPath: String = "Download/nna_rules.csv",
    val csvPattern: String = "",
    val digestHour: Int = 18,
    val digestMinute: Int = 0,
) {
    val isNew: Boolean get() = id == 0L
    val usesClipboard: Boolean get() = actionType == ActionType.CLIPBOARD
    val extractsCode: Boolean get() = usesClipboard && clipboardMode == ClipboardMode.EXTRACTED
    val usesCustomRegex: Boolean get() = extractsCode && extractionMode == ExtractionMode.CUSTOM_REGEX
    val regexError: Boolean get() = usesCustomRegex && !TextExtractor.isValidPattern(customRegex)
    val clearClipboardValue: Int
        get() = clearClipboardSeconds.toIntOrNull()
            ?.coerceIn(0, MAX_CLEAR_CLIPBOARD_SECONDS)
            ?: 0
    // TODO: Adjust canSave for different trigger types
    val canSave: Boolean
        get() {
            if (isLoading || regexError) return false
            val hasLabel = appLabel.isNotBlank()
            val triggerOk = when (triggerType) {
                NotificationTrigger.TYPE -> hasLabel && triggerPackage.isNotBlank()
                TimeTrigger.TYPE -> hasLabel
                WifiTrigger.TYPE -> hasLabel && triggerSsid.isNotBlank()
                PowerTrigger.TYPE -> hasLabel
                else -> false
            }
            val actionOk = when (actionType) {
                ActionType.CSV -> csvPath.isNotBlank()
                else -> true
            }
            return triggerOk && actionOk
        }

    companion object {
        const val MAX_CLEAR_CLIPBOARD_SECONDS = 86_400
    }
}

class RuleEditViewModel(
    private val repository: RuleRepository,
    private val ruleId: Long,
) : ViewModel() {

    var uiState by mutableStateOf(RuleEditUiState(isLoading = ruleId > 0))
        private set

    init {
        if (ruleId > 0) {
            viewModelScope.launch {
                val definition = repository.getDefinition(ruleId)
                uiState = if (definition == null) {
                    RuleEditUiState(isLoading = false)
                } else {
                    definition.toUiState()
                }
            }
        }
    }

    fun onAppSelected(packageName: String, appLabel: String) {
        uiState = uiState.copy(triggerPackage = packageName, appLabel = appLabel)
    }

    fun applyPreset(preset: RulePreset) {
        uiState = when (preset) {
            RulePreset.OTP_COPY -> uiState.copy(
                appLabel = "Auto-Copy OTP",
                triggerType = NotificationTrigger.TYPE,
                keyword = "",
                actionType = ActionType.CLIPBOARD,
                clipboardMode = ClipboardMode.EXTRACTED,
                extractionMode = ExtractionMode.OTP,
                clearClipboardSeconds = "30",
            )
            RulePreset.EXPENSE_LOGGER -> uiState.copy(
                appLabel = "Expense Logger (Rp)",
                triggerType = NotificationTrigger.TYPE,
                keyword = "Rp",
                actionType = ActionType.CSV,
                csvPath = "Download/nna_expenses.csv",
                csvPattern = "Rp\\s*([0-9.,]+)",
            )
            RulePreset.CHARGER_QUIET -> uiState.copy(
                appLabel = "Quiet on Charger",
                triggerType = PowerTrigger.TYPE,
                triggerConnected = true,
                actionType = ActionType.DND,
                dndMode = "PRIORITY",
            )
            RulePreset.SLEEP_SCHEDULE -> uiState.copy(
                appLabel = "Night Routine (22:00)",
                triggerType = TimeTrigger.TYPE,
                triggerHour = 22,
                triggerMinute = 0,
                actionType = ActionType.VOLUME,
                volumeLevel = 0,
                volumeStream = "RING",
            )
            RulePreset.WORK_WIFI -> uiState.copy(
                appLabel = "Office Wi-Fi Profile",
                triggerType = WifiTrigger.TYPE,
                triggerSsid = "Office",
                actionType = ActionType.VOLUME,
                volumeLevel = 15,
                volumeStream = "RING",
            )
        }
    }

    fun onKeywordChange(keyword: String) {
        uiState = uiState.copy(keyword = keyword)
    }

    fun onClipboardModeChange(mode: ClipboardMode) {
        uiState = uiState.copy(clipboardMode = mode)
    }

    fun onExtractionModeChange(mode: ExtractionMode) {
        uiState = uiState.copy(extractionMode = mode)
    }

    fun onCustomRegexChange(customRegex: String) {
        uiState = uiState.copy(customRegex = customRegex)
    }

    fun onClearClipboardSecondsChange(seconds: String) {
        uiState = uiState.copy(
            clearClipboardSeconds = seconds.filter(Char::isDigit).take(5),
        )
    }

    fun onEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(enabled = enabled)
    }

    fun updateTriggerType(type: String) {
        val defaultLabel = when (type) {
            TimeTrigger.TYPE -> if (uiState.appLabel.isBlank()) "Schedule %02d:%02d".format(uiState.triggerHour, uiState.triggerMinute) else uiState.appLabel
            PowerTrigger.TYPE -> if (uiState.appLabel.isBlank()) "Charger Profile" else uiState.appLabel
            WifiTrigger.TYPE -> if (uiState.appLabel.isBlank()) "Wi-Fi Profile" else uiState.appLabel
            else -> uiState.appLabel
        }
        uiState = uiState.copy(triggerType = type, appLabel = defaultLabel)
    }

    fun onAppLabelChange(label: String) {
        uiState = uiState.copy(appLabel = label)
    }

    fun onTriggerHourChange(hour: Int) {
        uiState = uiState.copy(triggerHour = hour.coerceIn(0, 23))
    }

    fun onTriggerMinuteChange(minute: Int) {
        uiState = uiState.copy(triggerMinute = minute.coerceIn(0, 59))
    }

    fun onTriggerSsidChange(ssid: String) {
        uiState = uiState.copy(triggerSsid = ssid)
    }

    fun onTriggerConnectedChange(connected: Boolean) {
        uiState = uiState.copy(triggerConnected = connected)
    }

    fun onVolumeLevelChange(level: Int) {
        uiState = uiState.copy(volumeLevel = level.coerceIn(0, 100))
    }

    fun onVolumeStreamChange(stream: String) {
        uiState = uiState.copy(volumeStream = stream)
    }

    fun onBrightnessLevelChange(level: Int) {
        uiState = uiState.copy(brightnessLevel = level.coerceIn(0, 255))
    }

    fun onDndModeChange(mode: String) {
        uiState = uiState.copy(dndMode = mode)
    }

    fun onCsvPathChange(path: String) {
        uiState = uiState.copy(csvPath = path)
    }

    fun onCsvPatternChange(pattern: String) {
        uiState = uiState.copy(csvPattern = pattern)
    }

    fun onDigestHourChange(hour: Int) {
        uiState = uiState.copy(digestHour = hour.coerceIn(0, 23))
    }

    fun onDigestMinuteChange(minute: Int) {
        uiState = uiState.copy(digestMinute = minute.coerceIn(0, 59))
    }

    fun updateActionType(type: ActionType) {
        uiState = uiState.copy(actionType = type)
    }

    fun save(onSaved: () -> Unit) {
        val state = uiState
        if (!state.canSave) return
        viewModelScope.launch {
            repository.save(state.toDefinition())
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val state = uiState
        if (state.id == 0L) return
        viewModelScope.launch {
            repository.getDefinition(state.id)?.let { repository.delete(it) }
            onDeleted()
        }
    }

    companion object {
        fun factory(ruleId: Long) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NnaRulesApp
                RuleEditViewModel(app.ruleRepository, ruleId)
            }
        }
    }
}

fun RuleDefinition.toUiState(): RuleEditUiState {
    var state = RuleEditUiState(
        id = id,
        isLoading = false,
        appLabel = appLabel,
        enabled = enabled,
        keyword = conditions.filterIsInstance<KeywordCondition>().firstOrNull()?.keyword.orEmpty(),
    )

    val triggerObj = trigger
    state = when (triggerObj) {
        is NotificationTrigger -> state.copy(triggerType = NotificationTrigger.TYPE, triggerPackage = triggerObj.packageName)
        is TimeTrigger -> state.copy(triggerType = TimeTrigger.TYPE, triggerHour = triggerObj.hour, triggerMinute = triggerObj.minute)
        is WifiTrigger -> state.copy(triggerType = WifiTrigger.TYPE, triggerSsid = triggerObj.ssid)
        is PowerTrigger -> state.copy(triggerType = PowerTrigger.TYPE, triggerConnected = triggerObj.connected)
        else -> state
    }

    val primaryAction = actions.firstOrNull()
    state = when (primaryAction) {
        is NotifyAction -> state.copy(actionType = ActionType.NOTIFY)
        is SoundAction -> state.copy(actionType = ActionType.SOUND)
        is ClipboardAction -> state.copy(
            actionType = ActionType.CLIPBOARD,
            clipboardMode = primaryAction.mode,
            extractionMode = primaryAction.extractionMode,
            customRegex = primaryAction.customRegex.orEmpty(),
            clearClipboardSeconds = primaryAction.clearClipboardSeconds.toString()
        )
        is VolumeAction -> state.copy(actionType = ActionType.VOLUME, volumeLevel = primaryAction.level, volumeStream = primaryAction.streamType)
        is BrightnessAction -> state.copy(actionType = ActionType.BRIGHTNESS, brightnessLevel = primaryAction.level)
        is DndAction -> state.copy(actionType = ActionType.DND, dndMode = primaryAction.mode)
        is CsvAppendAction -> state.copy(actionType = ActionType.CSV, csvPath = primaryAction.filePath, csvPattern = primaryAction.extractionPattern.orEmpty())
        is DigestAction -> state.copy(actionType = ActionType.DIGEST, digestHour = primaryAction.scheduleHour, digestMinute = primaryAction.scheduleMinute)
        else -> state
    }

    return state
}

fun RuleEditUiState.toDefinition(): RuleDefinition {
    val trigger = when (triggerType) {
        NotificationTrigger.TYPE -> NotificationTrigger(triggerPackage)
        TimeTrigger.TYPE -> TimeTrigger(triggerHour, triggerMinute)
        WifiTrigger.TYPE -> WifiTrigger(triggerSsid)
        PowerTrigger.TYPE -> PowerTrigger(triggerConnected)
        else -> NotificationTrigger(triggerPackage)
    }

    val actions = when (actionType) {
        ActionType.NOTIFY -> listOf(NotifyAction)
        ActionType.SOUND -> listOf(SoundAction)
        ActionType.CLIPBOARD -> listOf(
            ClipboardAction(
                mode = clipboardMode,
                extractionMode = extractionMode,
                customRegex = customRegex.trim().ifBlank { null },
                clearClipboardSeconds = clearClipboardValue,
            ),
        )
        ActionType.VOLUME -> listOf(VolumeAction(volumeLevel, volumeStream))
        ActionType.BRIGHTNESS -> listOf(BrightnessAction(brightnessLevel))
        ActionType.DND -> listOf(DndAction(dndMode))
        ActionType.CSV -> listOf(CsvAppendAction(csvPath, csvPattern.trim().ifBlank { null }))
        ActionType.DIGEST -> listOf(DigestAction(digestHour, digestMinute))
    }

    return RuleDefinition(
        id = id,
        appLabel = appLabel,
        enabled = enabled,
        createdAt = System.currentTimeMillis(),
        trigger = trigger,
        conditions = keyword.trim().takeIf { it.isNotEmpty() }
            ?.let { listOf(KeywordCondition(it)) } ?: emptyList(),
        actions = actions,
    )
}
