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
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.extraction.TextExtractor
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import io.github.nnazuna.rules.data.RuleRepository
import kotlinx.coroutines.launch

data class RuleEditUiState(
    val id: Long = 0,
    val isLoading: Boolean = false,
    val packageName: String = "",
    val appLabel: String = "",
    val keyword: String = "",
    val action: ActionType = ActionType.NOTIFY,
    val clipboardMode: ClipboardMode = ClipboardMode.FULL,
    val extractionMode: ExtractionMode = ExtractionMode.OTP,
    val customRegex: String = "",
    val clearClipboardSeconds: String = "0",
    val enabled: Boolean = true,
) {
    val isNew: Boolean get() = id == 0L
    val usesClipboard: Boolean get() = action == ActionType.CLIPBOARD
    val extractsCode: Boolean get() = usesClipboard && clipboardMode == ClipboardMode.EXTRACTED
    val usesCustomRegex: Boolean get() = extractsCode && extractionMode == ExtractionMode.CUSTOM_REGEX
    val regexError: Boolean get() = usesCustomRegex && !TextExtractor.isValidPattern(customRegex)
    val clearClipboardValue: Int
        get() = clearClipboardSeconds.toIntOrNull()
            ?.coerceIn(0, MAX_CLEAR_CLIPBOARD_SECONDS)
            ?: 0
    val canSave: Boolean get() = packageName.isNotBlank() && !isLoading && !regexError

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
        uiState = uiState.copy(packageName = packageName, appLabel = appLabel)
    }

    fun onKeywordChange(keyword: String) {
        uiState = uiState.copy(keyword = keyword)
    }

    fun onActionChange(action: ActionType) {
        uiState = uiState.copy(action = action)
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
    val notification = trigger as? NotificationTrigger ?: return RuleEditUiState()
    val primary = actions.firstOrNull() ?: return RuleEditUiState()
    val actionType = when (primary) {
        is NotifyAction -> ActionType.NOTIFY
        is SoundAction -> ActionType.SOUND
        is ClipboardAction -> ActionType.CLIPBOARD
        else -> return RuleEditUiState()
    }
    val clipboard = primary as? ClipboardAction
    return RuleEditUiState(
        id = id,
        isLoading = false,
        packageName = notification.packageName,
        appLabel = appLabel,
        keyword = conditions.filterIsInstance<KeywordCondition>().firstOrNull()?.keyword.orEmpty(),
        action = actionType,
        clipboardMode = clipboard?.mode ?: ClipboardMode.FULL,
        extractionMode = clipboard?.extractionMode ?: ExtractionMode.OTP,
        customRegex = clipboard?.customRegex.orEmpty(),
        clearClipboardSeconds = (clipboard?.clearClipboardSeconds ?: 0).toString(),
        enabled = enabled,
    )
}

fun RuleEditUiState.toDefinition(): RuleDefinition {
    val actions = when (action) {
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
    }
    return RuleDefinition(
        id = id,
        appLabel = appLabel,
        enabled = enabled,
        createdAt = System.currentTimeMillis(),
        trigger = NotificationTrigger(packageName),
        conditions = keyword.trim().takeIf { it.isNotEmpty() }
            ?.let { listOf(KeywordCondition(it)) } ?: emptyList(),
        actions = actions,
    )
}
