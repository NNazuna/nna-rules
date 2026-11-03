package io.github.nnazuna.rules.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nnazuna.rules.NnaRulesApp
import io.github.nnazuna.rules.data.Rule
import io.github.nnazuna.rules.data.RuleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuleListViewModel(private val repository: RuleRepository) : ViewModel() {

    val rules = repository.observeRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setEnabled(rule: Rule, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(rule, enabled) }
    }

    fun delete(rule: Rule) {
        viewModelScope.launch { repository.delete(rule) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NnaRulesApp
                RuleListViewModel(app.ruleRepository)
            }
        }
    }
}
