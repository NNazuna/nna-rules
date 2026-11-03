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
import io.github.nnazuna.rules.data.ActionType
import io.github.nnazuna.rules.data.Rule
import io.github.nnazuna.rules.data.RuleRepository
import io.github.nnazuna.rules.extraction.ClipboardMode
import io.github.nnazuna.rules.extraction.ExtractionMode
import io.github.nnazuna.rules.extraction.TextExtractor
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
                val rule = repository.getRule(ruleId)
                uiState = if (rule == null) {
                    RuleEditUiState(isLoading = false)
                } else {
                    RuleEditUiState(
                        id = rule.id,
                        isLoading = false,
                        packageName = rule.packageName,
                        appLabel = rule.appLabel,
                        keyword = rule.keyword.orEmpty(),
                        action = rule.action,
                        clipboardMode = rule.clipboardMode,
                        extractionMode = rule.extractionMode,
                        customRegex = rule.customRegex.orEmpty(),
                        clearClipboardSeconds = rule.clearClipboardSeconds.toString(),
                        enabled = rule.enabled,
                    )
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
            repository.save(
                Rule(
                    id = state.id,
                    packageName = state.packageName,
                    appLabel = state.appLabel,
                    keyword = state.keyword.trim().ifBlank { null },
                    action = state.action,
                    clipboardMode = state.clipboardMode,
                    extractionMode = state.extractionMode,
                    customRegex = state.customRegex.trim().ifBlank { null },
                    clearClipboardSeconds = state.clearClipboardValue,
                    enabled = state.enabled,
                ),
            )
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val state = uiState
        if (state.id == 0L) return
        viewModelScope.launch {
            repository.getRule(state.id)?.let { repository.delete(it) }
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
